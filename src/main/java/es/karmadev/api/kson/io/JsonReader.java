package es.karmadev.api.kson.io;

import es.karmadev.api.kson.*;
import es.karmadev.api.kson.processor.construct.CollectionParam;
import es.karmadev.api.kson.processor.construct.JsonConstructor;
import es.karmadev.api.kson.processor.construct.JsonParameter;
import es.karmadev.api.kson.object.JsonNull;
import es.karmadev.api.kson.processor.conversor.FieldTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Represents a json reader
 */
public final class JsonReader {

    private final byte[] raw;

    /**
     * Initialize the json reader
     *
     * @param rawData the raw json data
     */
    private JsonReader(final byte[] rawData) {
        this.raw = rawData;
    }

    /**
     * Parses the data
     *
     * @return the parsed data
     * @throws KsonException if the data is invalid
     */
    private JsonInstance parse() throws KsonException {
        JsonInstance instance;
        String json = new String(raw);

        if (json.startsWith("{")) {
            instance = JsonObject.newObject("", "");
        } else if (json.startsWith("[")) {
            instance = JsonArray.newArray("", "");
        } else {
            char firstChar = '\0';
            if (!json.isEmpty()) firstChar = json.charAt(0);

            throw new KsonException("Malformed json at index 0. Expected { or [ but got " + firstChar);
        }

        readObject(json, instance, 1);
        return instance;
    }

    /**
     * Parses the data
     *
     * @param expectedType the expected type
     * @return the un serialized element
     * @throws KsonException if the element is not serialized
     * or fails to un serialize
     * @param <T> the type
     */
    @Nullable
    private <T> T load(final Class<T> expectedType) throws KsonException {
        JsonInstance raw = parse();
        if (raw.isNull() || !raw.isObjectType())
            throw new KsonException("Not serialized data json!");

        JsonObject object = raw.asObject();
        return expectedType.cast(resolveElement(expectedType, object));
    }

    private static Object resolveElement(final Class<?> expectedType, final JsonObject object) {
        if (!object.hasChild("--serialized"))
            throw new KsonException("Not serialized json!");

        String className = object.asString("--serialized");
        if (className == null || className.trim().isEmpty())
            throw new KsonException("No serialized json class provided!");

        try {
            Class<?> clazz = Class.forName(className);
            if (!expectedType.equals(clazz)) {
                throw new KsonException("Unexpected serialized data. Expected it to be " +
                        expectedType.getSimpleName() + " but got " + clazz.getSimpleName());
            }

            Constructor<?>[] constructors = clazz.getDeclaredConstructors();

            for (Constructor<?> constructor : constructors) {
                if (!constructor.isAnnotationPresent(JsonConstructor.class)) continue;
                if (!Modifier.isPublic(constructor.getModifiers())) {
                    constructor.setAccessible(true);
                }

                List<Object> constructorParams = new ArrayList<>();

                Parameter[] parameters = constructor.getParameters();
                for (Parameter parameter : parameters) {
                    if (!parameter.isAnnotationPresent(JsonParameter.class))
                        throw new KsonException("Missing JsonParameter annotation from parameter " +
                                parameter.getName() + " at json constructor " + constructor.getName());

                    Class<?> type = parameter.getType();
                    JsonParameter jsonParameter = parameter.getAnnotation(JsonParameter.class);
                    String targetKey = jsonParameter.readFrom();
                    if (targetKey.equals("--serialized") || targetKey.endsWith("--transformer")) {
                        targetKey = parameter.getName();
                    }

                    JsonInstance instance = object.getChild(targetKey);
                    JsonInstance transformerClass = object.getChild(String.format("%s--transformer", targetKey));
                    if (!transformerClass.isNull()) {
                        try {
                            Class<?> transformer = Class.forName(transformerClass.asString());
                            FieldTransformer<?, ?> transformerElement = (FieldTransformer<?, ?>) transformer.getConstructor().newInstance();

                            Object val = transformerElement.fromElement(instance);
                            Object expectedElement = transformerElement.transformFromValue(val);

                            constructorParams.add(expectedElement);
                            continue;
                        } catch (NoClassDefFoundError | ReflectiveOperationException ignored) {}
                    }

                    handleInstanceLoad(instance, parameter, type, constructorParams);
                }

                try {
                    return constructor.newInstance(constructorParams.toArray());
                } catch (ReflectiveOperationException | IllegalArgumentException ignored) {}
            }
        } catch (NoClassDefFoundError | ReflectiveOperationException ex) {
            throw new KsonException(ex);
        }

        return null;
    }

    private static void handleInstanceLoad(final JsonInstance instance, final Parameter parameter,
                                           final Class<?> type, final List<Object> constructorParams) {
        if (instance.isNativeType()) {
            if (JsonNative.class.isAssignableFrom(type)) {
                constructorParams.add(instance);
                return;
            }
            JsonNative jsonNative = instance.asNative();

            if (!type.isPrimitive() && !CharSequence.class.isAssignableFrom(type) &&
                    !Boolean.class.isAssignableFrom(type) && !Number.class.isAssignableFrom(type)) {
                throw new KsonException("Unexpected constructor parameter " + parameter.getName() + " type");
            }

            if (jsonNative.isString()) {
                if (!CharSequence.class.isAssignableFrom(type)) {
                    throw new KsonException("Unexpected constructor parameter " +
                            parameter.getName() + " type. Expected " + type.getSimpleName() + "but got varchar sequence");
                }

                constructorParams.add(jsonNative.getString());
                return;
            }

            if (jsonNative.isBoolean()) {
                if (!Boolean.class.isAssignableFrom(type)) {
                    throw new KsonException("Unexpected constructor parameter " +
                            parameter.getName() + " type. Expected " + type.getSimpleName() + "but got boolean");
                }

                constructorParams.add(jsonNative.getBoolean());
                return;
            }

            if (jsonNative.isNumber()) {
                if (!Number.class.isAssignableFrom(type)) {
                    throw new KsonException("Unexpected constructor parameter " +
                            parameter.getName() + " type. Expected " + type.getSimpleName() + "but got number");
                }

                constructorParams.add(jsonNative.getNumber());
                return;
            }

            constructorParams.add(null);
        } else if (instance.isObjectType()) {
            if (JsonObject.class.isAssignableFrom(type)) {
                constructorParams.add(instance);
                return;
            }

            Object resolved = resolveElement(type, instance.asObject());
            if (resolved == null) {
                throw new KsonException("Unresolved parameter in json data: " + parameter.getName());
            }

            constructorParams.add(resolved);
        } else if (instance.isArrayType()) {
            if (JsonArray.class.isAssignableFrom(type)) {
                constructorParams.add(instance);
                return;
            }

            if (!type.isArray() && !Collection.class.isAssignableFrom(type)) {
                throw new KsonException("Unexpected constructor parameter " +
                        parameter.getName() + " type. Expected " + type.getSimpleName() + "but got array");
            }

            JsonArray array = instance.asArray();

            if (type.isArray()) {
                Class<?> compType = type.getComponentType();
                Object refArray = Array.newInstance(compType, array.size());

                List<Object> values = new ArrayList<>();
                for (int i = 0; i < Array.getLength(refArray); i++) {
                    JsonInstance child = array.get(i);
                    handleInstanceLoad(child, parameter, compType, values);
                }

                for (int i = 0; i < Array.getLength(refArray); i++) {
                    Array.set(refArray, i, values.get(i));
                }

                constructorParams.add(refArray);
            } else {
                if (!parameter.isAnnotationPresent(CollectionParam.class))
                    throw new KsonException("Cannot parse collection without a CollectionParam annotation for " + parameter.getName());

                CollectionParam collectionParam = parameter.getAnnotation(CollectionParam.class);
                Class<?> arrType = collectionParam.collectionType();

                List<Object> values = new ArrayList<>();
                for (JsonInstance child : array)
                    handleInstanceLoad(child, parameter, arrType, values);

                constructorParams.add(values);
            }
        }
    }

    private int readObject(final String rawString, final JsonInstance element, final int from) {
        byte[] raw = rawString.getBytes();
        StringBuilder valueBuilder = new StringBuilder();

        boolean buildingValue = false;

        String currentKey = null;
        for (int i = from; i < raw.length; i++) {
            char character = (char) raw[i];

            if (Character.isSpaceChar(character)) {
                if (buildingValue && valueBuilder.length() > 0) {
                    buildingValue = false;
                    append(currentKey, valueBuilder, element.getPath(), element);
                    currentKey = null;

                    valueBuilder.setLength(0);
                }

                continue;
            }

            if (character == '"') {
                int end = getNextOccurrence(raw, i, '"');
                if (end == -1) {
                    throw new KsonException("Malformed json at " + rawString.substring(0, i + 1));
                }

                String value = new String(raw, i + 1, (end - 1) - i);
                i = end;

                if (currentKey != null && valueBuilder.length() > 0 && buildingValue) {
                    buildingValue = false;
                    append(currentKey, valueBuilder, element.getPath(), element);

                    valueBuilder.setLength(0);
                    currentKey = null;
                }

                if (currentKey == null) {
                    currentKey = value;
                } else {
                    if (!buildingValue) {
                        if (element instanceof JsonArray) {
                            ((JsonArray) element).add(currentKey);
                            ((JsonArray) element).add(value);
                        } else {
                            throw new KsonException("Malformed json at " + rawString.substring(0, i + 1));
                        }
                    }

                    if (element instanceof JsonObject) {
                        ((JsonObject) element).put(currentKey, value);
                    }

                    currentKey = null;
                    buildingValue = false;
                }

                continue;
            }

            if (character == ':') {
                buildingValue = true;
                continue;
            }

            if (character == '{') {
                if (currentKey == null) {
                    currentKey = "";
                }

                String p = element.getPath();
                if (element.getPath().isEmpty()) {
                    p = element.getKey();
                } else {
                    p = element.getPath() + '.' + element.getKey();
                }

                JsonObject object = JsonObject.newObject(p, currentKey, element.getPathSeparator());
                i = readObject(rawString, object, i + 1);

                if (element instanceof JsonArray) {
                    ((JsonArray) element).add(object);
                } else {
                    ((JsonObject) element).put(currentKey, object);
                }

                currentKey = null;
                buildingValue = false;
                continue;
            }

            if (character == '[') {
                if (currentKey == null) {
                    currentKey = element.getKey();
                }

                String p;
                if (element.getPath().isEmpty()) {
                    p = element.getKey();
                } else {
                    p = element.getPath() + '.' + element.getKey();
                }

                JsonArray array = JsonArray.newArray(p, currentKey, '.');
                i = readObject(rawString, array, i + 1);

                if (element instanceof JsonArray) {
                    ((JsonArray) element).add(array);
                } else {
                    ((JsonObject) element).put(currentKey, array);
                }

                currentKey = null;
                buildingValue = false;
                continue;
            }

            if (character == ',') {
                if (currentKey != null) {
                    if (element instanceof JsonArray) {
                        ((JsonArray) element).add(currentKey);
                    } else {
                        buildingValue = false;

                        append(currentKey, valueBuilder, element.getPath(), element);
                        valueBuilder.setLength(0);
                    }


                    currentKey = null;
                }

                continue;
            }

            if (character == ']' && element instanceof JsonArray) {
                JsonArray array = (JsonArray) element;
                if (valueBuilder.length() > 0) {
                    JsonInstance instance = buildNative(array.getPath(), array.getKey(), valueBuilder.toString());
                    array.add(instance);
                }

                if (currentKey != null) {
                    array.add(currentKey);
                }

                return i;
            }

            if (character == '}' && element instanceof JsonObject) {
                if (currentKey != null) {
                    append(currentKey, valueBuilder, element.getPath(), element);
                    valueBuilder.setLength(0);
                }

                return i;
            }

            if (Character.isLetterOrDigit(character) && buildingValue)
                valueBuilder.append(character);
        }

        return raw.length;
    }

    private void append(final String key, final StringBuilder valueBuilder,
                        final String path, final JsonInstance target) {

        String value = valueBuilder.toString();
        JsonNative result = buildNative(path, key, value);

        if (target instanceof JsonObject) {
            ((JsonObject) target).put(key, result);
        } else {
            ((JsonArray) target).add(result);
        }
    }

    private static JsonNative buildNative(final String path, final String key, final String raw) {
        if (raw.equals("true") || raw.equals("false")) {
            return JsonNative.forBoolean(path, key, '.', Boolean.parseBoolean(raw));
        }

        if (raw.contains(",") || raw.contains(".") || raw.contains("e")) {
            try {
                float fl = Float.parseFloat(raw.replace(",", "").replace("_", ""));
                return JsonNative.forNumber(path, key, '.', fl);
            } catch (NumberFormatException flException) {
                try {
                    double db = Double.parseDouble(raw.replace(",", "").replace("_", ""));
                    return JsonNative.forNumber(path, key, '.', db);
                } catch (NumberFormatException ex) {
                    return JsonNative.forSequence(path, key, '.', raw);
                }
            }
        } else {
            try {
                byte b = Byte.parseByte(raw);
                return JsonNative.forNumber(path, key, '.', b);
            } catch (NumberFormatException ignored) {}

            try {
                short s = Short.parseShort(raw);
                return JsonNative.forNumber(path, key, '.', s);
            } catch (NumberFormatException ignored) {}

            try {
                int i = Integer.parseInt(raw);
                return JsonNative.forNumber(path, key, '.', i);
            } catch (NumberFormatException ignored) {}

            try {
                long l = Long.parseLong(raw);
                return JsonNative.forNumber(path, key, '.', l);
            } catch (NumberFormatException ignored) {}
        }

        return JsonNull.get(path, key, '.');
    }

    private int getNextOccurrence(final byte[] raw, final int from, final char of) {
        boolean escape = false;

        for (int i = from + 1; i < raw.length; i++) {
            char character = (char) raw[i];
            if (character == '\\') {
                escape = !escape;
                continue;
            }

            if (character == of && !escape) {
                return i;
            }
            escape = false;
        }

        return -1;
    }

    /**
     * Read a json
     *
     * @param data the raw json data to read
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    public static JsonInstance parse(final byte[] data) throws KsonException {
        JsonReader reader = new JsonReader(data);
        return reader.parse();
    }

    /**
     * Load an object from json
     *
     * @param type the json real type
     * @param data the raw json data to read
     * @return the loaded element
     * @param <T> the element type
     * @throws KsonException if the data fails to parse or
     * the element fails to load
     */
    public static <T> T load(final Class<T> type, final byte[] data) throws KsonException {
        JsonReader reader = new JsonReader(data);
        return reader.load(type);
    }

    /**
     * Map a java map into a json object
     *
     * @param map the map
     * @return the json value
     */
    public static JsonObject readTree(final Map<String, Object> map) {
        return readTree("", "", '.', map);
    }

    /**
     * Map a java map into a json object
     *
     * @param path the object path
     * @param objKey the object key
     * @param pathSeparator the object path separator
     * @param map the map
     * @return the json value
     */
    public static JsonObject readTree(final @NotNull String path, final @NotNull String objKey, final char pathSeparator, final Map<String, Object> map) {
        JsonObject object = JsonObject.newObject(path, objKey, pathSeparator);

        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value == null) {
                object.put(key, JsonNull.get(path, key, pathSeparator));
                continue;
            }

            if (value instanceof Map) {
                Map<?, ?> instance = (Map<?, ?>) value;
                Map<String, Object> m = unknownMapToResolved(instance);

                JsonObject resolved = readTree(path, key, pathSeparator, m);
                object.put(key, resolved);
                continue;
            }

            if (value instanceof Collection || value.getClass().isArray()) {
                List<Object> collect = new ArrayList<>();
                if (value.getClass().isArray()) {
                    for (int i = 0; i < Array.getLength(value); i++) {
                        collect.add(Array.get(value, i));
                    }
                } else {
                    assert value instanceof Collection;

                    Collection<?> collection = (Collection<?>) value;
                    collect.addAll(collection);
                }

                JsonArray array = JsonArray.newArray(path, pathSeparator);
                for (Object element : collect) {
                    if (element == null) continue;
                    if (element instanceof Map) {
                        Map<?, ?> instance = (Map<?, ?>) element;
                        Map<String, Object> m = unknownMapToResolved(instance);

                        JsonObject resolved = readTree(m);
                        array.add(resolved);
                        continue;
                    }

                    JsonNative jsonNative;
                    if (element instanceof CharSequence) {
                        jsonNative = JsonNative.forSequence(path, key, pathSeparator, (CharSequence) element);
                    } else {
                        String raw = String.valueOf(element);
                        jsonNative = buildNative(path, key, raw);
                    }

                    if (jsonNative.isNull()) continue;
                    array.add(jsonNative);
                }

                object.put(key, array);
                continue;
            }

            JsonNative jsonNative;
            if (value instanceof CharSequence) {
                jsonNative = JsonNative.forSequence(path, key, pathSeparator, (CharSequence) value);
            } else {
                String raw = String.valueOf(value);
                jsonNative = buildNative(path, key, raw);
            }

            object.put(key, jsonNative);
        }

        return object;
    }

    private static Map<String, Object> unknownMapToResolved(final Map<?, ?> unknown) {
        Map<String, Object> m = new HashMap<>();
        for (Object iK : unknown.keySet()) {
            Object iV = unknown.get(iK);
            if (iV == null || iK == null) continue;

            m.put(String.valueOf(iK), iV);
        }

        return m;
    }

    /**
     * Read a json
     *
     * @param json the raw json to read
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    public static JsonInstance read(final String json) throws KsonException {
        byte[] data = json.getBytes();

        JsonReader reader = new JsonReader(data);
        return reader.parse();
    }

    /**
     * Load an object from json
     *
     * @param type the json real type
     * @param json the raw json to read
     * @return the loaded element
     * @param <T> the element type
     * @throws KsonException if the data fails to parse or
     * the element fails to load
     */
    public static <T> T load(final Class<T> type, final String json) throws KsonException {
        byte[] data = json.getBytes();

        JsonReader reader = new JsonReader(data);
        return reader.load(type);
    }

    /**
     * Read a json
     *
     * @param stream the stream to read from
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    @Nullable
    public static JsonInstance read(final InputStream stream) throws KsonException {
        try {
            byte[] bytes = new byte[stream.available()];

            DataInputStream dataInputStream = new DataInputStream(stream);
            dataInputStream.readFully(bytes);

            JsonReader reader = new JsonReader(bytes);
            return reader.parse();
        } catch (IOException ex) {
            throw new KsonException(ex);
        }
    }

    /**
     * Load an object from json
     *
     * @param type the json real type
     * @param stream the stream to read from
     * @return the loaded element
     * @param <T> the element type
     * @throws KsonException if the data fails to parse or
     * the element fails to load
     */
    public static <T> T load(final Class<T> type, final InputStream stream) throws KsonException {
        try {
            byte[] bytes = new byte[stream.available()];

            DataInputStream dataInputStream = new DataInputStream(stream);
            dataInputStream.readFully(bytes);

            JsonReader reader = new JsonReader(bytes);
            return reader.load(type);
        } catch (IOException ex) {
            throw new KsonException(ex);
        }
    }

    /**
     * Read a json
     *
     * @param reader the reader that is reading
     *               the json
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    @Nullable
    public static JsonInstance read(final Reader reader) throws KsonException {
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[1024];

        int readChars;
        try {
            while ((readChars = reader.read(buffer)) != -1) {
                result.append(buffer, 0, readChars);
            }

            byte[] data = result.toString().getBytes();

            JsonReader rd = new JsonReader(data);
            return rd.parse();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Load an object from json
     *
     * @param type the json real type
     * @param reader the reader that is reading
     *               the json
     * @return the loaded element
     * @param <T> the element type
     * @throws KsonException if the data fails to parse or
     * the element fails to load
     */
    public static <T> T load(final Class<T> type, final Reader reader) throws KsonException {
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[1024];

        int readChars;
        try {
            while ((readChars = reader.read(buffer)) != -1) {
                result.append(buffer, 0, readChars);
            }

            byte[] data = result.toString().getBytes();

            JsonReader rd = new JsonReader(data);
            return rd.load(type);
        } catch (IOException ex) {
            return null;
        }
    }
}
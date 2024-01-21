package es.karmadev.api.kson.io;

import es.karmadev.api.kson.*;
import es.karmadev.api.kson.processor.*;
import es.karmadev.api.kson.processor.conversor.FieldTransformer;
import es.karmadev.api.kson.processor.field.JsonElement;
import es.karmadev.api.kson.processor.field.JsonExclude;
import es.karmadev.api.kson.processor.field.JsonNamed;
import es.karmadev.api.kson.object.JsonNull;
import es.karmadev.api.kson.processor.field.Transformer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

class AnnotatedWriter {

    /**
     * Process the object
     *
     * @param instance the object instance
     * @param expectedKey the object expected key
     * @param parent the parent instance
     * @return the processed object
     */
    static JsonInstance processObject(final Object instance, final String expectedKey, final JsonInstance parent) throws KsonException {
        if (instance instanceof JsonInstance) return (JsonInstance) instance;

        if (instance == null) throw new NullPointerException();
        Class<?> objectClass = instance.getClass();
        if (!objectClass.isAnnotationPresent(JsonSerializable.class) && !objectClass.isArray()) {
            if (instance instanceof CharSequence || instance instanceof Boolean || instance instanceof Number) {
                return treatElement(instance, "", "", '.', false, null);
            }

            throw new KsonException("Cannot serialize " + objectClass.getSimpleName() + " because it's not json serializable");
        }

        String path = "";
        String name = "";
        char separator = '.';

        JsonSerializable serializable = objectClass.getAnnotation(JsonSerializable.class);
        if (objectClass.isAnnotationPresent(JsonSerializable.class)) {
            path = serializable.path();
            name = serializable.name();
            separator = serializable.pathSeparator();
        }

        if (name.isEmpty()) {
            name = (expectedKey != null ? expectedKey : objectClass.getSimpleName().toLowerCase());
        }
        if (path.isEmpty()) {
            if (parent != null) {
                String parentPath = parent.getPath();
                if (!parentPath.isEmpty()) {
                    path = parentPath + separator + name;
                } else {
                    path = name;
                }
            }
        }

        JsonObject object;
        if ((instance instanceof Collection && objectClass.isAnnotationPresent(JsonCollection.class)) || objectClass.isArray()) {
            Collection<Object> collection = readCollection(instance);
            JsonArray target = new JsonArray(path, name, separator);

            for (Object colElement : collection) {
                JsonInstance colInstance = treatElement(colElement, path, name,
                        separator, objectClass.isAnnotationPresent(JsonCollection.class), target);
                if (colInstance.isNull()) continue;

                target.add(colInstance);
            }

            return target;
        } else {
            object = new JsonObject(path, name, separator);
        }

        object.put("--serialized", objectClass.getCanonicalName());
        Field[] fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(JsonExclude.class)) continue;

            int modifiers = field.getModifiers();

            String fieldPath = name;
            String fieldName = field.getName();
            char fieldSeparator = separator;
            boolean treatCollection = field.isAnnotationPresent(JsonCollection.class);

            if (field.isAnnotationPresent(JsonElement.class)) {
                JsonElement element = field.getAnnotation(JsonElement.class);
                fieldPath = element.path();
                fieldName = element.name();
                fieldSeparator = element.pathSeparator();

                if (fieldPath.isEmpty()) {
                    fieldPath = name;
                }
            } else if (field.isAnnotationPresent(JsonNamed.class)) {
                JsonNamed named = field.getAnnotation(JsonNamed.class);
                fieldName = named.name();
            } else if (serializable != null && !serializable.allPublic() && Modifier.isPublic(modifiers)) {
                continue;
            }

            if (fieldName.isEmpty() || fieldName.equalsIgnoreCase("--serialized") || fieldName.endsWith("--transformer")) {
                fieldName = field.getName();
            }

            Object value = null;
            try {
                if (Modifier.isStatic(modifiers)) {
                    value = field.get(objectClass);
                } else {
                    field.setAccessible(true);
                    value = field.get(instance);
                }
            } catch (ReflectiveOperationException ignored) {}

            if (value != null && field.isAnnotationPresent(Transformer.class)) {
                Transformer transformer = field.getAnnotation(Transformer.class);
                Class<? extends FieldTransformer<?, ?>> fTransformer = transformer.transformer();

                try {
                    FieldTransformer<?, ?> fieldTransformer = fTransformer.getConstructor().newInstance();
                    value = fieldTransformer.transformToValue(value);

                    object.put(String.format("%s--transformer", fieldName), fTransformer.getCanonicalName());
                } catch (ReflectiveOperationException ex) {
                    throw new KsonException(ex);
                }
            }

            JsonInstance element = treatElement(value, fieldPath, fieldName,
                    fieldSeparator, treatCollection, object);

            object.put(fieldName, element);
        }

        return object;
    }

    private static JsonInstance treatElement(final Object value, final String fieldPath,
                                      final String fieldName, final char fieldSeparator,
                                      final boolean treatCollection, final JsonInstance target) {
        JsonInstance element = JsonNull.get(fieldPath, fieldName, fieldSeparator);
        if (value == null) return element;

        if (value instanceof CharSequence) {
            element = JsonNative.forSequence(fieldPath, fieldName, fieldSeparator, String.valueOf(value));
        } else if (value instanceof Boolean) {
            element = JsonNative.forBoolean(fieldPath, fieldName, fieldSeparator, (Boolean) value);
        } else if (value instanceof Number) {
            element = JsonNative.forNumber(fieldPath, fieldName, fieldSeparator, (Number) value);
        } else if ((value instanceof Collection || value.getClass().isArray()) && treatCollection) {
            Collection<Object> collection = readCollection(value);
            JsonArray array = new JsonArray(fieldPath, fieldName, fieldSeparator);
            for (Object colElement : collection) {
                JsonInstance colInstance = treatElement(colElement, fieldPath, fieldName, fieldSeparator, true, array);
                if (colInstance.isNull()) continue;

                array.add(colInstance);
            }

            element = array;
        } else {
            element = processObject(value, fieldName, target);
        }

        return element;
    }

    private static Collection<Object> readCollection(final Object from) {
        Collection<Object> collection = new ArrayList<>();
        if (from.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(from); i++) {
                Object arrayEl = Array.get(from, i);
                if (arrayEl == null) continue;

                collection.add(arrayEl);
            }
        } else {
            Collection<?> valueCollection = (Collection<?>) from;
            valueCollection.forEach((el) -> {
                if (el == null) return;
                collection.add(el);
            });
        }

        return collection;
    }
}

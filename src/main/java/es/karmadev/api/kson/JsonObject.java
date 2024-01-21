package es.karmadev.api.kson;

import es.karmadev.api.kson.object.JsonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a JSON object instance, which is a key-value map.
 * This interface extends {@link Iterable} to provide convenient
 * iteration over the elements of the JSON object. This can be
 * particularly useful for scenarios where you want to iterate
 * through the key-value pairs without explicitly calling
 * {@link #getKeys(boolean)} and then using {@link #getChild(String)}
 * for each key.
 *
 * <p>Note: While this interface extends {@link Iterable},
 * the semantics of the iteration may differ from traditional
 * collections. The iteration includes both the direct key-value
 * pairs of this object and the key-value pairs of its child objects
 * in a recursive manner, providing a comprehensive traversal
 * of the JSON structure. For a real collection json, refer to {@link JsonArray}</p>
 */
@SuppressWarnings("unused")
public class JsonObject extends JsonInstance implements Iterable<JsonInstance> {

    /**
     * Create a new json object
     *
     * @param path the object path
     * @param key the object key
     * @return the new object
     */
    public static JsonObject newObject(final String path, final String key) {
        return newObject(path, key, '.');
    }

    /**
     * Create a new json object
     *
     * @param pathSeparator the path separator
     * @param key the object key
     * @return the new object
     */
    public static JsonObject newObject(final String key, final char pathSeparator) {
        return newObject("", "", '.');
    }

    /**
     * Create a new json object
     *
     * @param path the object path
     * @param key the object key
     * @param pathSeparator the object path separator
     * @return the new object
     */
    public static JsonObject newObject(final String path, final String key, final char pathSeparator) {
        return new JsonObject(path, key, pathSeparator);
    }

    private final Map<String, JsonInstance> instances = new LinkedHashMap<>();

    /**
     * Create a new simple json
     * object
     *
     * @param path the objet current path
     * @param key the object key
     * @param pathSeparator the object path separator
     */
    public JsonObject(final @NotNull String path, final @NotNull String key, final char pathSeparator) {
        this(path, key, pathSeparator, new HashMap<>());
    }

    /**
     * Create a new simple json
     * object
     *
     * @param path the object current path
     * @param key the object current key
     * @param pathSeparator the object path separator
     * @param values the object values
     */
    public JsonObject(final @NotNull String path, final @NotNull String key, final char pathSeparator, final Map<String, JsonInstance> values) {
        super(path, key, pathSeparator);
        if (values != null && !values.isEmpty())
            this.instances.putAll(values);
    }

    /**
     * Get the element size
     *
     * @return the element size
     */
    @Override
    public int size() {
        return instances.size();
    }

    /**
     * Clone the element on the new path and
     * the new path separator
     *
     * @param newPath       the path
     * @param newKey        the new key
     * @param pathSeparator the path separator
     * @return the new instance
     */
    @Override
    public JsonInstance clone(final String newPath, final String newKey, final char pathSeparator) {
        return new JsonObject(newPath, newKey, pathSeparator, instances);
    }

    /**
     * Get a child element on the object. The element
     * shouldn't be null. Instead, if not set
     * a {@link JsonNull null} instance should be returned
     *
     * @param path the path to the element
     * @return the child element
     */
    @NotNull
    public JsonInstance getChild(final String path) {
        return getChild(path, JsonNull.get());
    }

    /**
     * Get a child element
     *
     * @param path           the path to the element
     * @param defaultElement the default element if there's no
     *                       one set
     * @return the instance
     */
    public JsonInstance getChild(final String path, final JsonInstance defaultElement) {
        JsonInstance instance = instances.get(path);
        if (instance == null) {
            Map<String, JsonInstance> tree = buildJsonTree(this);
            return tree.getOrDefault(path, defaultElement);
        }

        return instance;
    }

    /**
     * Get if the object has a child
     * element
     *
     * @param path the path
     * @return if the element has the
     * child element
     */
    public boolean hasChild(final String path) {
        if (instances.containsKey(path)) return true;
        Map<String, JsonInstance> tree = buildJsonTree(this);

        return tree.containsKey(path);
    }

    /**
     * Remove an element from the object
     *
     * @param path the object path
     * @return if the object was removed
     */
    public boolean removeChild(final String path) {
        if (instances.remove(path) != null) return true;

        Map<String, JsonInstance> tree = buildJsonTree(this);
        JsonInstance instance = tree.get(path);
        if (instance == null) return false;

        String instancePath = path.replace(pathSeparator + instance.getKey(), "");
        JsonInstance parent = getChild(instancePath);

        if (!parent.isObjectType()) return false;
        return parent.asObject().removeChild(instance.getKey());
    }

    /**
     * Get all the object keys. When deep
     * is true, this will also iterate through
     * the children's key on cascade. What does
     * cascade implies? It means the recursion
     * is performed exclusively down, so only child
     * keys are recurse, no parent key will be returned
     * by the call of this method, never.
     *
     * @param deep if the search should be recursive
     * @return the object keys
     */
    public Collection<String> getKeys(final boolean deep) {
        List<String> keys = new ArrayList<>();

        for (String key : instances.keySet()) {
            JsonInstance value = instances.get(key);
            if (value instanceof JsonObject && deep) {
                keys.addAll(getKeysOf("", (JsonObject) value));
                continue;
            }

            String k = value.getKey();
            keys.add(k);
        }

        return keys;
    }

    /**
     * Get the json object as a map object.
     * The map returned by this method, contains,
     * exclusively, the keys hold in the current path,
     * meaning no recursion is performed during this
     * call.
     *
     * @return the json object as a map
     * object.
     */
    public Map<String, JsonInstance> getAsMap() {
        return Collections.unmodifiableMap(instances);
    }

    /**
     * Put a string into the object element
     *
     * @param path the path to set the string at
     * @param value the string
     */
    public void put(final String path, final String value) {
        put(path, JsonNative.forSequence(path, value));
    }

    /**
     * Put a number into the object element
     *
     * @param path the path to set the number at
     * @param number the number
     */
    public void put(final String path, final Number number) {
        put(path, JsonNative.forNumber(path, number));
    }

    /**
     * Put a boolean into the object element
     *
     * @param path the path to set the boolean at
     * @param bool the boolean
     */
    public void put(final String path, final Boolean bool) {
        put(path, JsonNative.forBoolean(path, bool));
    }

    /**
     * Put an element into the object.
     * The path of the element will be ignored and
     * replaced with the one specified in the path
     * parameter. In order to keep the instance element
     * path, refer to the {@link #insert(JsonInstance) insertion}
     * method.
     *
     * @param path    the element path
     * @param element the element to write
     */
    public void put(final String path, final JsonInstance element) {
        instances.put(element.getKey(), element);
    }

    /**
     * Insert an element into this element. The
     * expected behaviour of this element is that
     * the element to write path is keep under the
     * current path. Writing an instance on this
     * element won't make the other element (if any)
     * that holds the element to lose the element.
     *
     * @param instance the instance to write
     */
    public void insert(final JsonInstance instance) {
        String instancePath = instance.getPath();
        if (instancePath.isEmpty()) {
            instancePath = instance.getKey();
            if (instancePath.isEmpty()) {
                instancePath = UUID.randomUUID()
                        .toString().replace("_", "");
            }
        }

        put(instancePath, instance);
    }

    /**
     * Get the object json tree
     *
     * @return the object json tree
     */
    public Map<String, JsonInstance> getJsonTree() {
        return buildJsonTree(this);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<JsonInstance> iterator() {
        return instances.values().iterator();
    }

    /**
     * Get if the object is empty. The expected
     * behaviours are the following:
     * <ul>
     *     <li>{@link JsonObject objects} - Return true if the object has no keys defined</li>
     *     <li>{@link JsonArray arrays} - Return true if the array has no elements</li>
     *     <li>{@link JsonNative natives} - Returns true if the native type is string, and is empty, or if the native type is null</li>
     * </ul>
     *
     * @return if the object is empty
     */
    @Override
    public boolean isEmpty() {
        return getKeys(false).isEmpty();
    }

    /**
     * Execute an operation for each element of
     * this object and their child elements
     *
     * @param consumer the element consumer
     */
    public void deepForEach(final Consumer<JsonInstance> consumer) {
        Collection<String> keys = getKeys(false);

        List<JsonObject> childObjects = new ArrayList<>();
        for (String key : keys) {
            JsonInstance instance = getChild(key);
            if (instance.isNull()) continue;

            if (instance instanceof JsonObject) {
                JsonObject object = (JsonObject) instance;
                childObjects.add(object);
                continue;
            }

            consumer.accept(instance);
        }

        for (JsonObject object : childObjects) {
            object.deepForEach(consumer);
        }
    }

    private Collection<String> getKeysOf(final String path, final JsonObject object) {
        List<String> values = new ArrayList<>();
        for (String key : object.getKeys(false)) {
            JsonInstance element = object.getChild(key);
            if (!path.isEmpty()) {
                key = path + pathSeparator + key;
            } else {
                key = object.getKey() + pathSeparator + key;
            }

            if (element instanceof JsonObject) {
                JsonObject obj = (JsonObject) element;
                values.addAll(getKeysOf(key, obj));
                continue;
            }

            values.add(key);
        }

        return values;
    }

    private Map<String, JsonInstance> buildJsonTree(final JsonObject object) {
        Map<String, JsonInstance> map = new HashMap<>();

        for (String key : object.getKeys(false)) {
            JsonInstance value = instances.get(key);
            if (value.isObjectType()) {
                JsonObject child = value.asObject();

                Map<String, JsonInstance> sub = child.getJsonTree();
                for (String str : sub.keySet()) {
                    JsonInstance childValue = sub.get(str);

                    String path = key + pathSeparator + str;
                    map.put(path, childValue);
                }
            }

            map.put(key, value);
        }

        return map;
    }
}

package es.karmadev.api.kson;

import es.karmadev.api.kson.object.JsonNull;
import es.karmadev.api.kson.io.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.util.Map;

/**
 * Represents a json element.
 * This element can be of any type
 */
@SuppressWarnings("unused")
public abstract class JsonInstance {

    protected final String path;
    protected final String key;
    protected final char pathSeparator;

    /**
     * Create a new json instance
     *
     * @param path the instance path
     * @param key the instance key
     * @param pathSeparator the instance path separator
     */
    public JsonInstance(final String path, final String key, final char pathSeparator) {
        this.path = path;
        this.key = key;
        this.pathSeparator = pathSeparator;
    }

    /**
     * Get the instance path separator. A path
     * separator is a special character which
     * splits the path into a tree of paths.
     * For instance "this.is.a.path" would split
     * into:
     * <code>
     *     "this": {
     *         "is": {
     *             "a": {
     *                 "path: #Element here
     *             }
     *         }
     *     }
     * </code>
     * A path separator can be specified during the
     * initialization of a {@link JsonInstance parent}
     * element. In order to include the char on the path,
     * for example, if we have our value in "this.is.a.path"
     * instead of on a tree-map, we can escape the special
     * character, so it will be treated as a path character instead
     * of a separator
     *
     * @return the object path separator.
     */
    public final char getPathSeparator() {
        return pathSeparator;
    }

    /**
     * Get the key this instance
     * pertains to
     *
     * @return the key associated with
     * that instance
     */
    @NotNull
    public final String getKey() {
        return key;
    }

    /**
     * Get the element path
     *
     * @return the element path
     */
    public final String getPath() {
        return path;
    }

    /**
     * Get the element size
     *
     * @return the element size
     */
    public abstract int size();

    /**
     * Get if the instance is a native
     * json type. A native json type is
     * a json type which holds a native
     * java type (including string, which
     * in java are not natives)
     *
     * @return if the instance is a native type
     */
    public final boolean isNativeType() {
        return this instanceof JsonNative;
    }

    /**
     * Get if the instance is an array
     * json type. A json array type is
     * a json type which holds data
     * like a collections does.
     *
     * @return if the instance is an array
     */
    public final boolean isArrayType() { return this instanceof JsonArray; }

    /**
     * Get if the instance is an object
     * json type. An object json type
     * is a json type which holds data
     * like a map does, with a key-value
     * data schema, in where the key is always
     * a string, and the value is always a
     * native type or another object
     *
     * @return if the instance is an object
     */
    public final boolean isObjectType() {
        return this instanceof JsonObject;
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
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * Get if the object is a {@link es.karmadev.api.kson.object.JsonNull null}
     * json instance
     *
     * @return if the element is null
     */
    public boolean isNull() {
        return this.equals(JsonNull.get());
    }

    /**
     * Get the current element as a
     * json object
     *
     * @return the element as a json object.
     * @throws UnsupportedOperationException if the element
     * cannot be converted into the requested element.
     */
    @NotNull
    public JsonObject asObject() throws UnsupportedOperationException {
        if (this instanceof JsonObject) {
            return (JsonObject) this;
        }

        String type = getClass().getSimpleName();
        throw new UnsupportedOperationException(String.format(
                "Cannot converse from %s to object",
                type
        ));
    }

    /**
     * Get the current element as a
     * json array
     *
     * @return the element as a json array.
     * @throws UnsupportedOperationException if the element
     * cannot be converted into the requested element.
     */
    @NotNull
    public JsonArray asArray() throws UnsupportedOperationException {
        if (this instanceof JsonArray) {
            return (JsonArray) this;
        }

        String type = getClass().getSimpleName();
        throw new UnsupportedOperationException(String.format(
                "Cannot converse from %s to array",
                type
        ));
    }

    /**
     * Get the current element as a
     * json native
     *
     * @return the element as a json native
     * @throws UnsupportedOperationException if the element
     * cannot be converted into the requested element
     */
    @NotNull
    public JsonNative asNative() throws UnsupportedOperationException {
        if (this instanceof JsonNative) {
            return (JsonNative) this;
        }

        String type = getClass().getSimpleName();
        throw new UnsupportedOperationException(String.format(
                "Cannot converse from %s to native",
                type
        ));
    }

    /**
     * Get the current element as a
     * string
     *
     * @return the element as a string
     */
    @Nullable
    public String asString() {
        if (this instanceof JsonNative) {
            JsonNative nat = (JsonNative) this;
            return nat.getAsString();
        }

        return null;
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     */
    public byte asByte() {
        Number number = asNumber();
        if (number == null) return 0;

        return number.byteValue();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     */
    public short asShort() {
        Number number = asNumber();
        if (number == null) return 0;

        return number.shortValue();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     */
    public int asInteger() {
        Number number = asNumber();
        if (number == null) return 0;

        return number.intValue();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     */
    public long asLong() {
        Number number = asNumber();
        if (number == null) return 0L;

        return number.longValue();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     */
    public float asFloat() {
        Number number = asNumber();
        if (number == null) return 0f;

        return number.floatValue();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     */
    public double asDouble() {
        Number number = asNumber();
        if (number == null) return 0d;

        return number.doubleValue();
    }

    /**
     * Get the current element as a
     * boolean
     *
     * @return the element as a boolean
     */
    public boolean asBoolean() {
        if (this instanceof JsonNative) {
            JsonNative nat = (JsonNative) this;
            return Boolean.TRUE.equals(nat.getAsBoolean());
        }

        return false;
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     */
    public Number asNumber() {
        if (this instanceof JsonNative) {
            JsonNative nat = (JsonNative) this;
            return nat.getAsNumber();
        }

        return 0;
    }

    /**
     * Get the current element as a
     * json object
     *
     * @return the element as a json object.
     * @throws KsonException if the element is not an object-type
     */
    @NotNull
    public JsonObject asObject(final String path) throws KsonException {
        if (this instanceof JsonObject) {
            JsonObject object = (JsonObject) this;
            JsonInstance instance = object.getChild(path);

            return instance.asObject();
        }

        throw new KsonException(String.format("Cannot get \"%s\" from %s",
                path, getClass().getSimpleName()));
    }

    /**
     * Get the current element as a
     * json array
     *
     * @return the element as a json array.
     * @throws KsonException if the element is not an object-type
     */
    @NotNull
    public JsonArray asArray(final String path) throws KsonException {
        if (this instanceof JsonObject) {
            JsonObject object = (JsonObject) this;
            JsonInstance instance = object.getChild(path);

            return instance.asArray();
        }

        throw new KsonException(String.format("Cannot get \"%s\" from %s",
                path, getClass().getSimpleName()));
    }

    /**
     * Get the current element as a
     * json native
     *
     * @return the element as a json native
     * @throws KsonException if the element is not an object-type
     */
    @NotNull
    public JsonNative asNative(final String path) throws KsonException {
        if (this instanceof JsonObject) {
            JsonObject object = (JsonObject) this;
            JsonInstance instance = object.getChild(path);

            return instance.asNative();
        }

        throw new KsonException(String.format("Cannot get \"%s\" from %s",
                path, getClass().getSimpleName()));
    }

    /**
     * Get the current element as a
     * string
     *
     * @return the element as a string
     * @throws KsonException if the element is not an object-type
     */
    @Nullable
    public String asString(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asString();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     * @throws KsonException if the element is not an object-type
     */
    public byte asByte(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asByte();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     * @throws KsonException if the element is not an object-type
     */
    public short asShort(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asShort();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     * @throws KsonException if the element is not an object-type
     */
    public int asInteger(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asInteger();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     * @throws KsonException if the element is not an object-type
     */
    public long asLong(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asLong();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     * @throws KsonException if the element is not an object-type
     */
    public float asFloat(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asFloat();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     * @throws KsonException if the element is not an object-type
     */
    public double asDouble(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asDouble();
    }

    /**
     * Get the current element as a
     * boolean
     *
     * @return the element as a boolean
     * @throws KsonException if the element is not an object-type
     */
    public boolean asBoolean(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asBoolean();
    }

    /**
     * Get the current element as a
     * number
     *
     * @return the element as a number
     * @throws KsonException if the element is not an object-type
     */
    public Number asNumber(final String path) throws KsonException {
        JsonNative jsonNative = asNative(path);
        return jsonNative.asNumber();
    }

    /**
     * Get the string representation of
     * the element
     *
     * @param pretty if the value should be pretty
     * @return the string representation
     */
    public final String toString(final boolean pretty) {
        return toString(pretty, 0);
    }

    /**
     * Get the string representation of
     * the element
     *
     * @param indentation the indentation level
     * @return the string representation
     */
    public final String toString(final int indentation) {
        return toString(true, indentation);
    }

    /**
     * Get the string representation of
     * the element
     *
     * @param pretty if the value should be pretty
     * @param indentation the indentation level
     * @return the string representation
     */
    public String toString(final boolean pretty, final int indentation) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(this);
        jsonWriter.setPrettyPrinting(pretty);
        jsonWriter.setIndentation(indentation);

        jsonWriter.export(writer);
        return writer.toString();
    }

    /**
     * Get the tree of the json object. The tree
     * is represented as a json object, pointing to
     * key=value
     *
     * @return the object tree
     */
    public final Map<String, Object> getTree() {
        JsonWriter jsonWriter = new JsonWriter(this);
        return jsonWriter.toTree();
    }

    /**
     * Clone the element on the new path and
     * the new path separator
     *
     * @param newPath the path
     * @param newKey the new key
     * @param pathSeparator the path separator
     * @return the new instance
     */
    public abstract JsonInstance clone(final String newPath, final String newKey, final char pathSeparator);

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(this);
        jsonWriter.setPrettyPrinting(false);
        jsonWriter.export(writer);

        return writer.toString();
    }
}

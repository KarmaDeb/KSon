package es.karmadev.api.kson.object;

import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Represents a null instance
 * of a json object
 */
public final class JsonNull extends JsonNative {

    private final static JsonNull INSTANCE = new JsonNull();

    private JsonNull() {
        this("", "", '.');
    }

    private JsonNull(final String path, final String key, final char pathSeparator) {
        super(path, key, pathSeparator);
    }

    /**
     * Get the element size
     *
     * @return the element size
     */
    @Override
    public int size() {
        return 0;
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
        return true;
    }

    /**
     * Clone the element on the new path and
     * the new path separator
     *
     * @param newPath       the path
     * @param newKey        the key
     * @param pathSeparator the path separator
     * @return the new instance
     */
    @Override
    public JsonInstance clone(final String newPath, final String newKey, final char pathSeparator) {
        return get(newPath, newKey, pathSeparator);
    }

    /**
     * Returns whether the element
     * is a string
     *
     * @return if the element is a string
     */
    @Override
    public boolean isString() {
        return false;
    }

    /**
     * Returns whether the element
     * is a number
     *
     * @return if the element is a number
     */
    @Override
    public boolean isNumber() {
        return false;
    }

    /**
     * Returns whether the element is a
     * boolean
     *
     * @return if the element is a boolean
     */
    @Override
    public boolean isBoolean() {
        return false;
    }

    /**
     * Get the string value of the element
     *
     * @return the string value
     * @throws UnsupportedOperationException if the element
     *                                       is not a string
     */
    @Override
    public @NotNull String getString() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Json null cannot be casted to string");
    }

    /**
     * Get the number value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     *                                       is not a number
     */
    @Override
    public @NotNull Number getNumber() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Json null cannot be casted to number");
    }

    /**
     * Get the boolean value of the element
     *
     * @return the boolean element
     * @throws UnsupportedOperationException if the element
     *                                       is not a number
     */
    @Override
    public boolean getBoolean() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Json null cannot be casted to boolean");
    }

    /**
     * Get the string value of the element. The
     * main difference between this method and
     * {@link #getString()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the string value
     */
    @Override
    public @Nullable String getAsString() {
        return null;
    }

    /**
     * Get the number value of the element. The
     * main difference between this method and
     * {@link #getNumber()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the number element
     */
    @Override
    public @Nullable Number getAsNumber() {
        return null;
    }

    /**
     * Get the boolean value of the element. The
     * main difference between this method and
     * {@link #getBoolean()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the boolean element
     */
    @Override
    public @Nullable Boolean getAsBoolean() {
        return null;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof JsonNull;
    }

    /**
     * Get the json null instance
     *
     * @return the json null instance
     */
    public static JsonNull get() {
        return INSTANCE;
    }

    /**
     * Get the json null instance
     *
     * @param path the null path
     * @param key the null key
     * @param pathSeparator the null path
     *                      separator
     * @return the null instance
     */
    public static JsonNull get(final String path, final String key, final char pathSeparator) {
        return new JsonNull(path, key, pathSeparator);
    }
}

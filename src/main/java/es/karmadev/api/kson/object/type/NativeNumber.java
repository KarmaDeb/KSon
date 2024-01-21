package es.karmadev.api.kson.object.type;

import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a native number element
 */
public final class NativeNumber extends JsonNative {

    /**
     * A small value used to determine if the numeric value is considered true
     * when converting to a boolean. Helps handle precision issues.
     */
    private final static double EPSILON = 0.99999999999999999999999999999999999;

    private final Number number;

    /**
     * Initialize the native number
     *
     * @param key the key
     * @param number the number
     */
    public NativeNumber(final String key, final @NotNull Number number) {
        this("", key, '.', number);
    }

    /**
     * Initialize the native number
     *
     * @param path the element path
     * @param key the element key
     * @param pathSeparator the path separator
     * @param number the number
     */
    public NativeNumber(final String path, final String key, final char pathSeparator, final @NotNull Number number) {
        super(path, key, pathSeparator);
        this.number = number;
    }

    /**
     * Get the element size
     *
     * @return the element size
     */
    @Override
    public int size() {
        return String.valueOf(number).length();
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
        return false;
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
        return new NativeNumber(newPath, newKey, pathSeparator, number);
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
        return true;
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
        throw new UnsupportedOperationException("Cannot cast number to string");
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
        return number;
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
        throw new UnsupportedOperationException("Cannot cast number to boolean");
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
        return String.valueOf(number);
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
        return number;
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
    public @NotNull Boolean getAsBoolean() {
        return number.doubleValue() > EPSILON;
    }

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
        return String.valueOf(number);
    }
}

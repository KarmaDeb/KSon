package es.karmadev.api.kson;

import es.karmadev.api.kson.object.JsonNull;
import es.karmadev.api.kson.object.type.NativeBoolean;
import es.karmadev.api.kson.object.type.NativeNumber;
import es.karmadev.api.kson.object.type.NativeString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a json instances which
 * holds a java native element
 */
@SuppressWarnings("unused")
public abstract class JsonNative extends JsonInstance {

    /**
     * Get the json native instance of
     * a character sequence object. If the
     * sequence is null, an instance of {@link JsonNull null}
     * will be returned
     *
     * @param sequence the sequence
     * @param key the native element key
     * @return the sequence as native json element
     */
    public static JsonNative forSequence(final String key, final CharSequence sequence) {
        return forSequence("", key, '.', sequence);
    }

    /**
     * Get the json native instance of
     * the number. If the number is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param number the number
     * @param key the native element key
     * @return the number as a native element
     */
    public static JsonNative forNumber(final String key, final Number number) {
        return forNumber("", key, '.', number);
    }

    /**
     * Get the json native instance of
     * the boolean. If the boolean is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param bool the boolean
     * @param key the native element key
     * @return the boolean as a native element
     */
    public static JsonNative forBoolean(final String key, final Boolean bool) {
        return forBoolean("", key, '.', bool);
    }

    /**
     * Get the json native instance of
     * a character sequence object. If the
     * sequence is null, an instance of {@link JsonNull null}
     * will be returned
     *
     * @param path the native element path
     * @param key the native element key
     * @param sequence the sequence
     * @return the sequence as native json element
     */
    public static JsonNative forSequence(final String path, final String key, final CharSequence sequence) {
        return forSequence(path, key, '.', sequence);
    }

    /**
     * Get the json native instance of
     * the number. If the number is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param key the native element key
     * @param number the number
     * @return the number as a native element
     */
    public static JsonNative forNumber(final String path, final String key, final Number number) {
        return forNumber(path, key, '.', number);
    }

    /**
     * Get the json native instance of
     * the boolean. If the boolean is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param key the native element key
     * @param bool the boolean
     * @return the boolean as a native element
     */
    public static JsonNative forBoolean(final String path, final String key, final Boolean bool) {
        return forBoolean(path, key, '.', bool);
    }

    /**
     * Get the json native instance of
     * a character sequence object. If the
     * sequence is null, an instance of {@link JsonNull null}
     * will be returned
     *
     * @param path the native element path
     * @param key the native element key
     * @param pathSeparator the native element path separator
     * @param sequence the sequence
     * @return the sequence as native json element
     */
    public static JsonNative forSequence(final String path, final String key, final char pathSeparator, final CharSequence sequence) {
        if (sequence == null) return JsonNull.get(path, key, pathSeparator);
        return new NativeString(key, sequence.toString());
    }

    /**
     * Get the json native instance of
     * the number. If the number is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param key the native element key
     * @param pathSeparator the native element path separator
     * @param number the number
     * @return the number as a native element
     */
    public static JsonNative forNumber(final String path, final String key, final char pathSeparator, final Number number) {
        if (number == null) return JsonNull.get(path, key, pathSeparator);
        return new NativeNumber(key, number);
    }

    /**
     * Get the json native instance of
     * the boolean. If the boolean is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param key the native element key
     * @param pathSeparator the native element path separator
     * @param bool the boolean
     * @return the boolean as a native element
     */
    public static JsonNative forBoolean(final String path, final String key, final char pathSeparator, final Boolean bool) {
        if (bool == null) return JsonNull.get(path, key, pathSeparator);
        return new NativeBoolean(key, bool);
    }

    /**
     * Create a new json instance
     *
     * @param path          the instance path
     * @param key           the instance key
     * @param pathSeparator the instance path separator
     */
    public JsonNative(final String path, final String key, final char pathSeparator) {
        super(path, key, pathSeparator);
    }

    /**
     * Returns whether the element
     * is a string
     *
     * @return if the element is a string
     */
    public abstract boolean isString();

    /**
     * Returns whether the element
     * is a number
     *
     * @return if the element is a number
     */
    public abstract boolean isNumber();

    /**
     * Returns whether the element is a
     * boolean
     *
     * @return if the element is a boolean
     */
    public abstract boolean isBoolean();

    /**
     * Get the string value of the element
     *
     * @return the string value
     * @throws UnsupportedOperationException if the element
     * is not a string
     */
    @NotNull
    public abstract String getString() throws UnsupportedOperationException;

    /**
     * Get the number value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    @NotNull
    public abstract Number getNumber() throws UnsupportedOperationException;

    /**
     * Get the boolean value of the element
     *
     * @return the boolean element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    public abstract boolean getBoolean() throws UnsupportedOperationException;

    /**
     * Get the string value of the element. The
     * main difference between this method and
     * {@link #getString()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the string value
     */
    @Nullable
    public abstract String getAsString();

    /**
     * Get the number value of the element. The
     * main difference between this method and
     * {@link #getNumber()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the number element
     */
    @Nullable
    public abstract Number getAsNumber();

    /**
     * Get the boolean value of the element. The
     * main difference between this method and
     * {@link #getBoolean()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the boolean element
     */
    @Nullable
    public abstract Boolean getAsBoolean();

    /**
     * Get the byte value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    public byte getByte() throws UnsupportedOperationException {
        return getNumber().byteValue();
    }

    /**
     * Get the short value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    public short getShort() throws UnsupportedOperationException {
        return getNumber().shortValue();
    }

    /**
     * Get the int value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    public int getInteger() throws UnsupportedOperationException {
        return getNumber().intValue();
    }

    /**
     * Get the long value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    public long getLong() throws UnsupportedOperationException {
        return getNumber().longValue();
    }

    /**
     * Get the float value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    public float getFloat() throws UnsupportedOperationException {
        return getNumber().floatValue();
    }

    /**
     * Get the double value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    public double getDouble() throws UnsupportedOperationException {
        return getNumber().doubleValue();
    }
}

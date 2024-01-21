package es.karmadev.api.kson.processor.conversor;

import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;

import java.util.UUID;

public class UUIDTransformer extends FieldTransformer<UUID, String> {

    /**
     * Transform the element into a
     * string
     *
     * @param element the element
     * @return the element string element
     */
    @Override
    public String transformToValue(final Object element) {
        if (!(element instanceof UUID)) return null;
        return element.toString();
    }

    /**
     * Transform the string into the
     * element
     *
     * @param element the element
     * @return the transformed element
     */
    @Override
    public UUID transformFromValue(final Object element) {
        if (element == null) return null;

        try {
            return UUID.fromString(String.valueOf(element));
        } catch (IllegalArgumentException ignored) {}

        return null;
    }

    /**
     * Build a value out of a json instance
     *
     * @param instance the instance
     * @return the element
     */
    @Override
    public String fromElement(final JsonInstance instance) {
        if (!instance.isNativeType()) return null;
        JsonNative jsonNative = instance.asNative();
        return jsonNative.asString();
    }
}

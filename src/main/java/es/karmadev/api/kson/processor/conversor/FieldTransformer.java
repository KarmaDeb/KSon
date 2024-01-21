package es.karmadev.api.kson.processor.conversor;

import es.karmadev.api.kson.JsonInstance;

/**
 * Represents a field transformer. A
 * transformer is able to transform a
 * field into a value and then return
 * the original field value type
 */
public abstract class FieldTransformer<T, V> {

    /**
     * Initializes the transformer
     */
    public FieldTransformer() {}

    /**
     * Transform the element into a
     * string
     *
     * @param element the element
     * @return the element string element
     */
    public abstract V transformToValue(final Object element);

    /**
     * Transform the string into the
     * element
     *
     * @param element the element
     * @return the transformed element
     */
    public abstract T transformFromValue(final Object element);

    /**
     * Build a value out of a json instance
     *
     * @param instance the instance
     * @return the element
     */
    public abstract V fromElement(final JsonInstance instance);
}

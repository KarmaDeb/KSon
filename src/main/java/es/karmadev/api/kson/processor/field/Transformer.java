package es.karmadev.api.kson.processor.field;

import es.karmadev.api.kson.processor.conversor.FieldTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a transformer element.
 * A transformer field is nothing but
 * a field of a type which is intended to be
 * processed as another type when serializing
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transformer {

    /**
     * Get the transformer
     *
     * @return the transformer type
     */
    Class<? extends FieldTransformer<?, ?>> transformer();
}

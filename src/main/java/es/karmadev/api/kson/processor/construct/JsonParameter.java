package es.karmadev.api.kson.processor.construct;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a {@link JsonConstructor constructor}
 * parameter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonParameter {

    /**
     * Get the element key to
     * read from when filling the
     * constructor parameters
     *
     * @return the key to read from
     */
    String readFrom();
}

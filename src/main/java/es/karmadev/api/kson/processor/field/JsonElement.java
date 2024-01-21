package es.karmadev.api.kson.processor.field;

import es.karmadev.api.kson.processor.JsonSerializable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a json element in
 * a {@link JsonSerializable}. Annotate a
 * field with this method in order
 * to include it in the serialization
 * process
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonElement {

    /**
     * The object path in the
     * json file
     *
     * @return the object path
     */
    String path() default "";

    /**
     * The object key in the
     * json file
     *
     * @return the object key
     */
    String name() default "";

    /**
     * Get object path separator
     * in the json file
     *
     * @return the object path separator
     */
    char pathSeparator() default '.';
}

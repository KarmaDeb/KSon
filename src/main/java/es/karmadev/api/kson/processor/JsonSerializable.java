package es.karmadev.api.kson.processor;

import es.karmadev.api.kson.processor.field.JsonExclude;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a json serializable
 * class. Annotate a class with this
 * to make {@link es.karmadev.api.kson.io.JsonWriter}
 * able to serialize a class into
 * a json element
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerializable {

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

    /**
     * Get if the serialization should
     * read all the public fields (including
     * static) which are not marked with
     * {@link JsonExclude}
     *
     * @return if all the public fields
     * should be automatically added
     */
    boolean allPublic() default false;
}

package es.karmadev.api.kson.processor.field;

import es.karmadev.api.kson.processor.JsonSerializable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a named field on
 * a {@link JsonSerializable}.
 * Avoid using this field unless you
 * have an {@link JsonSerializable} with
 * {@link JsonSerializable#allPublic()} enabled.
 * This field will give the field a different
 * name without making it a {@link JsonSerializable}.
 * This field only applies for public (static too) fields.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonNamed {

    /**
     * The object key in the
     * json file
     *
     * @return the object key
     */
    String name() default "";
}

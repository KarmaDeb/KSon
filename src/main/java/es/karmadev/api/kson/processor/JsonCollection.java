package es.karmadev.api.kson.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a collection field on
 * a {@link JsonSerializable}.
 * Avoid using this field unless you
 * have an {@link JsonSerializable} with
 * {@link JsonSerializable#allPublic()} enabled.
 * This field will make the field to be treated
 * as a collection if it's one.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonCollection {
}

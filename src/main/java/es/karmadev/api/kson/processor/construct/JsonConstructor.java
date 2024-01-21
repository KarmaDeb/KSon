package es.karmadev.api.kson.processor.construct;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a constructor which can be
 * used by {@link es.karmadev.api.kson.io.JsonReader}.
 * That constructor must have parameters annotated
 * by
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonConstructor {
}

package es.karmadev.api.kson.processor.construct;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a collection parameter
 * on a {@link JsonParameter}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectionParam {

    /**
     * In case the parameter is a collection,
     * this specifies the collection type.
     * @return the collection type
     */
    Class<?> collectionType();
}

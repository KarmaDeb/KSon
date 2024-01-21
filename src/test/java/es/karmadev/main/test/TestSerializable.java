package es.karmadev.main.test;

import es.karmadev.api.kson.processor.JsonCollection;
import es.karmadev.api.kson.processor.construct.CollectionParam;
import es.karmadev.api.kson.processor.construct.JsonConstructor;
import es.karmadev.api.kson.processor.construct.JsonParameter;
import es.karmadev.api.kson.processor.conversor.UUIDTransformer;
import es.karmadev.api.kson.processor.field.JsonElement;
import es.karmadev.api.kson.processor.field.JsonNamed;
import es.karmadev.api.kson.processor.JsonSerializable;
import es.karmadev.api.kson.processor.field.Transformer;

import java.util.Collection;
import java.util.UUID;

@JsonSerializable
public class TestSerializable {

    @JsonElement
    private final String key;

    @JsonNamed
    @JsonCollection
    private final Collection<String> array;

    @JsonElement
    private final TestSerializableChild child;

    @JsonNamed(name = "uuid")
    @Transformer(transformer = UUIDTransformer.class)
    private final UUID uniqueId;

    @JsonConstructor
    public TestSerializable(final @JsonParameter(readFrom = "key") String key,
                            final @JsonParameter(readFrom = "array")
                            @CollectionParam(collectionType = String.class) Collection<String> array,
                            final @JsonParameter(readFrom = "child") TestSerializableChild child,
                            final @JsonParameter(readFrom = "uuid") UUID uniqueId) {
        this.key = key;
        this.array = array;
        this.child = child;
        this.uniqueId = uniqueId;
    }

    public String getKey() {
        return key;
    }

    public String[] getArray() {
        return array.toArray(new String[0]);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }
}

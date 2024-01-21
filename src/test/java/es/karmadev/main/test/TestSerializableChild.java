package es.karmadev.main.test;

import es.karmadev.api.kson.processor.construct.JsonConstructor;
import es.karmadev.api.kson.processor.construct.JsonParameter;
import es.karmadev.api.kson.processor.field.JsonElement;
import es.karmadev.api.kson.processor.JsonSerializable;

@JsonSerializable
public class TestSerializableChild {

    @JsonElement
    private final String key;

    @JsonConstructor
    public TestSerializableChild(final @JsonParameter(readFrom = "key") String key) {
        this.key = key;
    }
}

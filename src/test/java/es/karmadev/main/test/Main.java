package es.karmadev.main.test;

import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.kson.io.JsonWriter;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        TestSerializable serializable = new TestSerializable(
                "value",
                Arrays.asList("Hello", "world!"),
                new TestSerializableChild("value"),
                UUID.randomUUID());

        JsonWriter writer = new JsonWriter(serializable);
        writer.setPrettyPrinting(true);

        StringWriter sw = new StringWriter();
        writer.export(sw);

        System.out.println(sw);
        byte[] raw = sw.toString().replace("value", "hello").getBytes();
        TestSerializable value = JsonReader.load(TestSerializable.class, raw);
        System.out.println(value.getUniqueId());
    }
}

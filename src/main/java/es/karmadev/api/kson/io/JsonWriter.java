package es.karmadev.api.kson.io;

import es.karmadev.api.kson.*;
import lombok.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a json writer.
 * A writer is the responsible for
 * transforming the {@link es.karmadev.api.kson.JsonInstance json instance}
 * into readable text
 */
public final class JsonWriter {

    private final JsonInstance instance;
    private boolean prettyPrinting = false;
    private int indentation = 0;

    /**
     * Initialize the json writer
     *
     * @param instance the instance to write
     */
    public JsonWriter(final @NonNull JsonInstance instance) {
        this.instance = instance;
    }

    /**
     * Initialize the json writer
     *
     * @param instance the instance to write
     */
    public JsonWriter(final @NonNull Object instance) {
        this(AnnotatedWriter.processObject(instance, "", null));
    }

    /**
     * Set the writer pretty print
     * support
     *
     * @param prettyPrinting the pretty print support
     */
    public void setPrettyPrinting(final boolean prettyPrinting) {
        this.prettyPrinting = prettyPrinting;
    }

    /**
     * Set the indentation level. Only works when
     * {@link #prettyPrinting pretty printing} is true
     *
     * @param indentation the new indentation level
     */
    public void setIndentation(final int indentation) {
        this.indentation = indentation;
    }

    /**
     * Write the element into the
     * writer
     *
     * @param writer the writer
     * @throws AssertionError if the instance type is unknown
     * @throws KsonException if the writer fails
     */
    public void export(final Writer writer) throws AssertionError, KsonException {
        try {
            if (instance instanceof JsonObject) {
                writeJsonObjectToWriter(writer);
            } else if (instance instanceof JsonArray) {
                writeJsonArrayToWriter(writer);
            } else if (instance instanceof JsonNative) {
                writeJsonNativeToWriter(writer);
            } else {
                throw new AssertionError("Cannot write unknown json type");
            }
        } catch (IOException ex) {
            throw new KsonException(ex);
        }
    }

    /**
     * Make a java map from the
     * json object
     *
     * @return the map
     */
    public Map<String, Object> toTree() {
        Map<String, Object> tree = new HashMap<>();
        if (instance.isObjectType()) {
            JsonObject object = (JsonObject) instance;

            for (String key : object.getKeys(false)) {
                JsonInstance value = object.getChild(key);

                fill(value, (el) -> tree.put(key, el));
            }
        } else if (instance.isArrayType()) {
            JsonArray array = (JsonArray) instance;
            List<Object> objectList = new ArrayList<>();

            for (JsonInstance element : array) {
                fill(element, objectList::add);
            }

            tree.put(array.getKey(), objectList);
        }

        return tree;
    }

    private static void fill(final JsonInstance value, final Consumer<Object> fillFunc) {
        if (value instanceof JsonObject) {
            JsonWriter writer = new JsonWriter(value);
            Map<String, Object> resolved = writer.toTree();

            fillFunc.accept(resolved);
            //tree.put(key, resolved);
            return;
        }

        if (value instanceof JsonArray) {
            JsonWriter writer = new JsonWriter(value);
            Map<String, Object> resolved = writer.toTree();

            fillFunc.accept(resolved.get(value.getKey()));
            //tree.put(value.getKey(), resolved.get(value.getKey()));
            return;
        }

        JsonNative jsonNative = value.asNative();
        if (jsonNative.isString()) {
            fillFunc.accept(jsonNative.getString());
            //tree.put(key, jsonNative.getString());
        } else if (jsonNative.isNumber()) {
            fillFunc.accept(jsonNative.getNumber());
            //tree.put(key, jsonNative.getNumber());
        } else if (jsonNative.isBoolean()) {
            fillFunc.accept(jsonNative.getBoolean());
            //tree.put(key, jsonNative.getBoolean());
        } else {
            fillFunc.accept(jsonNative.toString());
            //tree.put(key, jsonNative.toString());
        }
    }

    private void writeJsonObjectToWriter(final Writer writer) throws IOException {
        JsonObject object = (JsonObject) instance;
        String indentation = buildIndentation();

        writer.write("{");
        if (prettyPrinting) {
            writer.write("\n");
        }

        int index = 0;
        Collection<String> keys = object.getKeys(false);
        for (String key : keys) {
            JsonInstance instance = object.getChild(key);
            if (instance.isNull()) {
                index++;
                continue;
            }

            String value = instance.toString(this.prettyPrinting, this.indentation + 1);
            if (this.prettyPrinting) {
                writer.write(indentation + '\t');
            }

            writer.write(String.format("\"%s\":%s%s", key, (prettyPrinting ? " " : ""), value));
            if (index++ < keys.size() - 1) {
                writer.write(",");
            }

            if (prettyPrinting) {
                writer.write("\n");
            }
        }

        writer.write(indentation);
        writer.write("}");
    }

    private void writeJsonArrayToWriter(final Writer writer) throws IOException {
        JsonArray array = (JsonArray) instance;
        String indentation = buildIndentation();

        writer.write("[");
        if (prettyPrinting) {
            writer.write("\n");
        }

        int index = 0;
        for (JsonInstance instance : array) {
            if (instance == null || instance.isNull()) continue;

            String value = instance.toString(this.prettyPrinting, this.indentation + 1);
            if (this.prettyPrinting) {
                writer.write(indentation + '\t');
            }

            writer.write(value);
            if (++index < array.size()) {
                writer.write(",");
            }

            if (prettyPrinting) {
                writer.write("\n");
            }
        }

        writer.write(indentation);
        writer.write("]");
    }

    private void writeJsonNativeToWriter(final Writer writer) throws IOException {
        JsonNative nat = (JsonNative) instance;
        if (nat.isNull()) {
            writer.write("null");
        } else if (nat.isString()) {
            writer.write(String.format("\"%s\"", nat.getString()));
        } else if (nat.isNumber()) {
            writer.write(String.valueOf(nat.getNumber()));
        } else {
            writer.write(String.valueOf(nat.getBoolean()));
        }
    }

    private String buildIndentation() {
        if (!prettyPrinting || indentation <= 0) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indentation; i++) {
            builder.append('\t');
        }

        return builder.toString();
    }
}

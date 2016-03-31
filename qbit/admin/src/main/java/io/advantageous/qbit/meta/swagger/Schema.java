package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.json.annotations.JsonProperty;

public class Schema {

    private final String type;
    private final String format;
    private final Schema items;
    private final Schema additionalProperties;
    private final String description;

    @JsonProperty("$ref")
    private final String ref;


    public Schema(final String type, final String format, final Schema items,
                  final Schema additionalProperties, final String ref, final String description) {
        this.type = type;
        this.format = format;
        this.items = items;
        this.ref = ref;
        this.additionalProperties = additionalProperties;
        this.description = description;
    }

    public static Schema map(Schema componentSchema, String description) {
        return new Schema("object", null, null, componentSchema, null, description);
    }

    public static Schema array(Schema items, String description) {
        return new Schema("array", null, items, null, null, description);
    }

    public static Schema array(Schema items) {
        return new Schema("array", null, items, null, null, null);
    }

    public static Schema schema(String type) {
        return new Schema(type, null, null, null, null, null);
    }

    public static Schema schemaWithFormat(String type, String format) {
        return new Schema(type, format, null, null, null, null);
    }

    public static Schema definitionRef(String type, String description) {
        return new Schema(null, null, null, null, "#/definitions/" + type, description);
    }

    public static Schema schemaWithDescription(Schema schema, String description) {
        return new Schema(schema.type, schema.format, schema.items,
                schema.additionalProperties, schema.ref, description);
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }


    public Schema getItems() {
        return items;
    }


    public String getRef() {
        return ref;
    }

    public Schema getAdditionalProperties() {
        return additionalProperties;
    }

    public String getDescription() {
        return description;
    }

}

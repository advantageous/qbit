package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.json.annotations.JsonProperty;

public class Schema {

    private final String type;
    private final String format;
    private final Schema items;


    @JsonProperty("$ref")
    private final String ref;


    public static Schema array(Schema items) {
        return new Schema("array", null, items);
    }

    public static Schema schema(String type) {
        return new Schema(type, null, null);
    }


    public static Schema schema(String type, String format) {
        return new Schema(type, format, null);
    }


    public static Schema definitionRef(String type) {
        return new Schema(null, null, null, "#/definitions/" + type);
    }



    public Schema(String type, String format, Schema items) {
        this.type = type;
        this.format = format;
        this.items = items;
        ref =  null;
    }

    public Schema(String type, String format, Schema items, String $ref) {
        this.type = type;
        this.format = format;
        this.items = items;
        this.ref =  $ref;
    }


    public Schema(String type, String format) {
        this.type = type;
        this.format = format;
        this.items = null;
        ref =  null;
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
}

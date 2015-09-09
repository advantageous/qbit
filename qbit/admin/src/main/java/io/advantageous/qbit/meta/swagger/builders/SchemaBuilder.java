package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Schema;

public class SchemaBuilder {


    private String type;
    private String format;
    private String description;

    public String getType() {
        return type;
    }

    public SchemaBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public SchemaBuilder setFormat(String format) {
        this.format = format;
        return this;
    }

    public Schema build() {
        return new Schema(getType(), getFormat(), null, null, null, description);
    }

    public String getDescription() {
        return description;
    }
}

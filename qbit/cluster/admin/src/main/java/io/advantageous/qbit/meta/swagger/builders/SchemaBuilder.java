package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Schema;

public class SchemaBuilder {


    private String type;
    private String format;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Schema build() {
        return new Schema(getType(), getFormat(), null);
    }
}

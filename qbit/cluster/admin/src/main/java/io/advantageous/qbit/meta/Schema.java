package io.advantageous.qbit.meta;

public class Schema {

    private final String type;
    private final String format;

    public Schema(String type, String format) {
        this.type = type;
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }
}

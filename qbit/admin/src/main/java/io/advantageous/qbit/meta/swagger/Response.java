package io.advantageous.qbit.meta.swagger;

public class Response {
    private final String description;
    private final Schema schema;

    public Response(final String description, final Schema schema) {
        this.description = description == null ? "returns" : description;
        this.schema = schema;
    }

    public String getDescription() {
        return description;
    }

    public Schema getSchema() {
        return schema;
    }
}

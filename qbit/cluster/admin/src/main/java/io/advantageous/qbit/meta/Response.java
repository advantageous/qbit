package io.advantageous.qbit.meta;

public class Response {
    private final String description;
    private final Schema schema;

    public Response(String description, Schema schema) {
        this.description = description;
        this.schema = schema;
    }
}

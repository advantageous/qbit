package io.advantageous.qbit.meta.builders;

import io.advantageous.qbit.meta.Response;
import io.advantageous.qbit.meta.Schema;

public class ResponseBuilder {

    private String description;
    private Schema schema;

    public String getDescription() {
        return description;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Response build() {
        return new Response(getDescription(), getSchema());
    }
}

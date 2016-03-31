package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Response;
import io.advantageous.qbit.meta.swagger.Schema;

public class ResponseBuilder {

    private String description;
    private Schema schema;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Response build() {
        return new Response(getDescription(), getSchema());
    }
}

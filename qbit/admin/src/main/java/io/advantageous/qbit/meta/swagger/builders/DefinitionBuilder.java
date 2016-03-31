package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Definition;
import io.advantageous.qbit.meta.swagger.Schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefinitionBuilder {


    private Map<String, Schema> properties;

    private String description;


    public Map<String, Schema> getProperties() {
        if (properties == null) {
            properties = new LinkedHashMap<>();
        }
        return properties;
    }

    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }

    public void addProperty(String name, Schema schema) {

        getProperties().put(name, schema);
    }

    public Definition build() {
        return new Definition(getProperties(), description);
    }

    public String getDescription() {
        return description;
    }

    public DefinitionBuilder setDescription(String description) {
        this.description = description;
        return this;
    }
}

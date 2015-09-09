package io.advantageous.qbit.meta.swagger;

import java.util.Collections;
import java.util.Map;

public class Definition {

    private final Map<String, Schema> properties;

    private final String description;

    public Definition(Map<String, Schema> properties, String description) {
        this.description = description;
        this.properties = Collections.unmodifiableMap(properties);
    }

    public Map<String, Schema> getProperties() {
        return properties;
    }

    public String getDescription() {
        return description;
    }
}

package io.advantageous.qbit.meta.swagger;

import java.util.Collections;
import java.util.Map;

public class Definition {

    private final Map<String, Schema> properties;

    public Definition(Map<String, Schema> properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    public Map<String, Schema> getProperties() {
        return properties;
    }
}

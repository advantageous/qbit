package io.advantageous.qbit.meta;

import java.util.Map;

public class Definition {

    private final Map<String, Schema> properties;
    public Definition(Map<String, Schema> properties) {
        this.properties = properties;
    }
}

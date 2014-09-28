package org.qbit.bindings;

public class RequestParamBinding {

    private final int paramIndex;
    private final boolean required;
    private final String name;
    private final Object defaultValue;

    public RequestParamBinding(String name, int paramIndex, boolean required,  Object defaultValue) {
        this.paramIndex = paramIndex;
        this.required = required;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public boolean isRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}

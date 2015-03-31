package io.advantageous.qbit.meta.params;

public class NamedParam extends Param {

    private final String name;

    public NamedParam(final boolean required, String name, Object defaultValue, final ParamType paramType) {
        super(required, defaultValue, paramType);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

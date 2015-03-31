package io.advantageous.qbit.meta.params;

public class HeaderParam extends NamedParam {


    public HeaderParam(final boolean required, final String name, Object defaultValue) {
        super(required, name, defaultValue, ParamType.HEADER);
    }
}

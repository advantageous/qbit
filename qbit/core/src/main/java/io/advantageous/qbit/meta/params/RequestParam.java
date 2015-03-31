package io.advantageous.qbit.meta.params;

public class RequestParam extends NamedParam {


    public RequestParam(final boolean required,
                        final String name,
                        final Object defaultValue) {

        super(required, name, defaultValue, ParamType.REQUEST);
    }
}

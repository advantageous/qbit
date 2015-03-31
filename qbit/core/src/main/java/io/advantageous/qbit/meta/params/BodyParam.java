package io.advantageous.qbit.meta.params;

public class BodyParam extends Param {
    public BodyParam(boolean required, Object defaultValue) {
        super(required, defaultValue, ParamType.BODY);
    }
}

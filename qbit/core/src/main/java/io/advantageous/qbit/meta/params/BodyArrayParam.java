package io.advantageous.qbit.meta.params;

public class BodyArrayParam extends PositionalParam {

    public BodyArrayParam(final boolean required,
                          final int position,
                          final Object defaultValue){
        super(required, position, defaultValue, ParamType.BODY_BY_POSITION);
    }


}

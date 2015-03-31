package io.advantageous.qbit.meta.params;

public class PositionalParam extends Param {

    private final int position;

    public PositionalParam(final boolean required, int position, Object defaultValue, final ParamType paramType) {
        super(required, defaultValue, paramType);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}

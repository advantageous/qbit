package io.advantageous.qbit.meta;

import io.advantageous.boon.core.TypeType;
import io.advantageous.qbit.meta.params.Param;

public class ParameterMeta {

    public static ParameterMeta[] parameters(final ParameterMeta... parameters) {
        return parameters;
    }




    public static ParameterMeta param(final TypeType typeType, final Param param) {
        return new ParameterMeta(typeType, param);
    }

    public static ParameterMeta stringParam(final Param param) {
        return new ParameterMeta(TypeType.STRING, param);
    }

    public static ParameterMeta intParam(final Param param) {
        return new ParameterMeta(TypeType.INT, param);
    }
    public static ParameterMeta floatParam(final Param param) {
        return new ParameterMeta(TypeType.FLOAT, param);
    }

    public static ParameterMeta doubleParam(final Param param) {
        return new ParameterMeta(TypeType.DOUBLE, param);
    }

    public static ParameterMeta objectParam(final Param param) {
        return new ParameterMeta(TypeType.OBJECT, param);
    }





    public static ParameterMeta paramMeta(final TypeType typeType, final Param param) {
        return new ParameterMeta(typeType, param);
    }

    private final TypeType type;

    private final Param param;

    public ParameterMeta(final TypeType typeType, final Param param) {
        this.type = typeType;
        this.param = param;
    }

    public TypeType getType() {
        return type;
    }

    public Param getParam() {
        return param;
    }
}

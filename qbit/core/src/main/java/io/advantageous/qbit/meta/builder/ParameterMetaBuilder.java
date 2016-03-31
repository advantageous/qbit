package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.TypeType;
import io.advantageous.qbit.meta.GenericParamType;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.params.Param;

public class ParameterMetaBuilder {

    private Class<?> classType;
    private TypeType type;
    private Param param;
    private GenericParamType genericParamType = GenericParamType.NONE;
    private Class<?> componentClass;
    private Class<?> componentClassKey;
    private Class<?> componentClassValue;

    private String description;

    public static ParameterMetaBuilder parameterMetaBuilder() {
        return new ParameterMetaBuilder();
    }

    public String getDescription() {
        return description;
    }

    public ParameterMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public Class<?> getComponentClassValue() {
        return componentClassValue;
    }

    public ParameterMetaBuilder setComponentClassValue(Class<?> componentClassValue) {
        this.componentClassValue = componentClassValue;
        return this;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public ParameterMetaBuilder setClassType(Class<?> classType) {
        this.classType = classType;
        return this;
    }

    public TypeType getType() {
        return type;
    }

    public ParameterMetaBuilder setType(TypeType type) {
        this.type = type;
        return this;
    }

    public Param getParam() {
        return param;
    }

    public ParameterMetaBuilder setParam(Param param) {
        this.param = param;
        return this;
    }

    public boolean isCollection() {
        return genericParamType == GenericParamType.COLLECTION;
    }

    public ParameterMetaBuilder setCollection() {
        genericParamType = GenericParamType.COLLECTION;
        return this;
    }

    public boolean isMap() {
        return genericParamType == GenericParamType.MAP;
    }

    public ParameterMetaBuilder setMap() {
        genericParamType = GenericParamType.MAP;
        return this;
    }

    public boolean isArray() {
        return genericParamType == GenericParamType.ARRAY;
    }

    public ParameterMetaBuilder setArray() {
        genericParamType = GenericParamType.ARRAY;
        return this;
    }

    public Class<?> getComponentClass() {
        return componentClass;
    }

    public ParameterMetaBuilder setComponentClass(Class<?> componentClass) {
        this.componentClass = componentClass;
        return this;
    }

    public Class<?> getComponentClassKey() {
        return componentClassKey;
    }

    public ParameterMetaBuilder setComponentClassKey(Class<?> componentClassKey) {
        this.componentClassKey = componentClassKey;
        return this;
    }

    public ParameterMeta build() {
        return new ParameterMeta(getClassType(), getType(), getParam(), getGenericParamType(),
                getComponentClass(), getComponentClassKey(), getComponentClassValue());
    }

    public GenericParamType getGenericParamType() {
        return genericParamType;
    }

    public ParameterMetaBuilder setGenericParamType(GenericParamType genericParamType) {
        this.genericParamType = genericParamType;
        return this;
    }
}

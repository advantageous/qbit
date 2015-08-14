package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.TypeType;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.params.Param;

public class ParameterMetaBuilder {

    private  Class<?> classType;
    private  TypeType type;
    private  Param param;
    private  boolean collection;
    private  boolean map;
    private  boolean array;
    private  Class<?> componentClass;
    private  Class<?> componentClassKey;
    private  Class<?> componentClassValue;

    private String description;

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
        return collection;
    }

    public ParameterMetaBuilder setCollection(boolean collection) {
        this.collection = collection;
        return this;
    }

    public boolean isMap() {
        return map;
    }

    public ParameterMetaBuilder setMap(boolean map) {
        this.map = map;
        return this;
    }

    public boolean isArray() {
        return array;
    }

    public ParameterMetaBuilder setArray(boolean array) {
        this.array = array;
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
        return new ParameterMeta(getClassType(), getType(), getParam(), isCollection(), isMap(), isArray(),
                getComponentClass(), getComponentClassKey(), getComponentClassValue());
    }

    public static ParameterMetaBuilder parameterMetaBuilder() {
        return new ParameterMetaBuilder();
    }
}

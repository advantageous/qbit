/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */
package io.advantageous.qbit.meta;

import io.advantageous.boon.core.TypeType;
import io.advantageous.qbit.meta.params.Param;


/**
 * Holds meta-data about a single parameter of a service method.
 */
public class ParameterMeta {

    private final Class<?> classType;
    private final TypeType type;
    private final Param param;
    private final Class<?> componentClass;
    private final Class<?> componentClassKey;
    private final Class<?> componentClassValue;
    private final GenericParamType genericParamType;


    public ParameterMeta(Class<?> classType,
                         TypeType type,
                         Param param,
                         GenericParamType genericParamType,
                         Class<?> returnTypeComponent,
                         Class<?> returnTypeComponentKey,
                         Class<?> returnTypeComponentValue) {
        this.classType = classType;
        this.type = type;
        this.param = param;
        this.genericParamType = genericParamType;
        this.componentClass = returnTypeComponent;
        this.componentClassKey = returnTypeComponentKey;
        this.componentClassValue = returnTypeComponentValue;
    }

    public ParameterMeta(Class<?> classType,
                         TypeType type,
                         Param param) {
        this.classType = classType;
        this.type = type;
        this.param = param;

        this.genericParamType = GenericParamType.NONE;
        this.componentClass = null;
        this.componentClassKey = null;
        this.componentClassValue = null;
    }


    public static ParameterMeta[] parameters(final ParameterMeta... parameters) {
        return parameters;
    }

    public static ParameterMeta param(Class<?> classType, final TypeType typeType, final Param param) {
        return new ParameterMeta(classType, typeType, param);
    }

    public static ParameterMeta stringParam(final Param param) {
        return new ParameterMeta(null, TypeType.STRING, param);
    }

    public static ParameterMeta intParam(final Param param) {
        return new ParameterMeta(int.class, TypeType.INT, param);
    }

    public static ParameterMeta floatParam(final Param param) {
        return new ParameterMeta(float.class, TypeType.FLOAT, param);
    }

    public static ParameterMeta doubleParam(final Param param) {
        return new ParameterMeta(double.class, TypeType.DOUBLE, param);
    }

    public static ParameterMeta objectParam(final Param param) {
        return new ParameterMeta(Object.class, TypeType.OBJECT, param);
    }

    public static ParameterMeta paramMeta(final TypeType typeType, final Param param) {
        return new ParameterMeta(null, typeType, param);
    }

    public TypeType getType() {
        return type;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public Param getParam() {
        return param;
    }

    public boolean isCollection() {
        return genericParamType == GenericParamType.COLLECTION;
    }

    public boolean isMap() {
        return genericParamType == GenericParamType.MAP;
    }

    public boolean isArray() {
        return genericParamType == GenericParamType.ARRAY;
    }

    public Class<?> getComponentClass() {
        return componentClass;
    }

    public Class<?> getComponentClassKey() {
        return componentClassKey;
    }

    public Class<?> getComponentClassValue() {
        return componentClassValue;
    }

    public boolean isString() {
        return type == TypeType.STRING;
    }

    public boolean isByteArray() {
        return type == TypeType.ARRAY_BYTE;
    }

    public GenericParamType getGenericParamType() {
        return genericParamType;
    }
}

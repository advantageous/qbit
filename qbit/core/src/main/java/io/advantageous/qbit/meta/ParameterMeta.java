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

    public ParameterMeta(final Class<?> classType, final TypeType typeType, final Param param) {
        this.type = typeType;
        this.param = param;
        this.classType = classType;
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
        return new ParameterMeta(null, TypeType.INT, param);
    }

    public static ParameterMeta floatParam(final Param param) {
        return new ParameterMeta(null, TypeType.FLOAT, param);
    }

    public static ParameterMeta doubleParam(final Param param) {
        return new ParameterMeta(null, TypeType.DOUBLE, param);
    }

    public static ParameterMeta objectParam(final Param param) {
        return new ParameterMeta(null, TypeType.OBJECT, param);
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
}

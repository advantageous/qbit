package io.advantageous.qbit.meta;


import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.JsonIgnore;

import java.util.List;

public class ServiceMethodMeta {


    public static ServiceMethodMeta serviceMethod(final MethodAccess methodAccess,
                         final RequestMeta... requestMetas) {

        return new ServiceMethodMeta(methodAccess, Lists.list(requestMetas));
    }


    public static ServiceMethodMeta method(final MethodAccess methodAccess,
                                              final RequestMeta... requestMetas) {

        return new ServiceMethodMeta(methodAccess, Lists.list(requestMetas));
    }

    public static ServiceMethodMeta method(final String name,
                                       final RequestMeta... requestMetas) {

        return new ServiceMethodMeta(name, Lists.list(requestMetas));
    }


    private final List<RequestMeta> requestEndpoints;

    @JsonIgnore
    private final MethodAccess methodAccess;
    private final String name;
    private final TypeType returnType;
    private final List<TypeType> paramTypes;

    public ServiceMethodMeta(final MethodAccess methodAccess,
                             final List<RequestMeta> requestMetaList) {
        this.requestEndpoints = requestMetaList;
        this.methodAccess = methodAccess;
        this.name = methodAccess.name();
        this.returnType = TypeType.getType(methodAccess.returnType());
        this.paramTypes = methodAccess.paramTypeEnumList();
    }


    public ServiceMethodMeta(final String name,
                             final List<RequestMeta> requestMetaList) {
        this.requestEndpoints = requestMetaList;
        this.methodAccess = null;
        this.name = name;
        this.returnType = null;
        this.paramTypes = null;
    }

    public ServiceMethodMeta(final String name,
                             final List<RequestMeta> requestMetaList,
                             final TypeType returnType,
                             final List<TypeType> paramTypes) {

        this.requestEndpoints = requestMetaList;
        this.methodAccess = null;
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    public List<RequestMeta> getRequestEndpoints() {
        return requestEndpoints;
    }

    @JsonIgnore
    public MethodAccess getMethodAccess() {
        return methodAccess;
    }

    public String getName() {
        return name;
    }

    public TypeType getReturnType() {
        return returnType;
    }

    public List<TypeType> getParamTypes() {
        return paramTypes;
    }
}

package io.advantageous.qbit.meta.builder;


import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.meta.RequestMeta;
import io.advantageous.qbit.meta.ServiceMethodMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceMethodMetaBuilder {


    private List<RequestMeta> requestEndpoints = new ArrayList<RequestMeta>();
    private MethodAccess methodAccess;
    private String name;
    private String address;
    private TypeType returnType;
    private List<TypeType> paramTypes;

    public static ServiceMethodMetaBuilder serviceMethodMetaBuilder() {
        return new ServiceMethodMetaBuilder();
    }

    public String getAddress() {
        return address;
    }

    public ServiceMethodMetaBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public List<RequestMeta> getRequestEndpoints() {
        return requestEndpoints;
    }

    public ServiceMethodMetaBuilder setRequestEndpoints(List<RequestMeta> requestEndpoints) {
        this.requestEndpoints = requestEndpoints;
        return this;
    }


    public ServiceMethodMetaBuilder addRequestEndpoint(RequestMeta requestEndpoint) {
        this.requestEndpoints.add(requestEndpoint);
        return this;
    }


    public ServiceMethodMetaBuilder addRequestEndpoint(RequestMeta... requestEndpointArray) {
        Collections.addAll(this.requestEndpoints, requestEndpointArray);
        return this;
    }

    public MethodAccess getMethodAccess() {
        return methodAccess;
    }

    public ServiceMethodMetaBuilder setMethodAccess(MethodAccess methodAccess) {
        this.methodAccess = methodAccess;
        return this;
    }

    public String getName() {
        return name;
    }

    public ServiceMethodMetaBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public TypeType getReturnType() {
        return returnType;
    }

    public ServiceMethodMetaBuilder setReturnType(TypeType returnType) {
        this.returnType = returnType;
        return this;
    }

    public List<TypeType> getParamTypes() {
        return paramTypes;
    }

    public ServiceMethodMetaBuilder setParamTypes(List<TypeType> paramTypes) {
        this.paramTypes = paramTypes;
        return this;
    }

    public ServiceMethodMeta build() {
        if (methodAccess != null) {
            return new ServiceMethodMeta(getMethodAccess(), this.getRequestEndpoints());
        } else {
            return new ServiceMethodMeta(getName(), this.getRequestEndpoints(),
                    this.getReturnType(), this.getParamTypes());
        }
    }
}

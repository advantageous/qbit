package io.advantageous.qbit.meta.builder;


import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.meta.RequestMeta;
import io.advantageous.qbit.meta.ServiceMethodMeta;
import io.advantageous.qbit.reactive.Callback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class ServiceMethodMetaBuilder {


    private List<RequestMeta> requestEndpoints = new ArrayList<>();
    private MethodAccess methodAccess;
    private String name;
    private String address;
    private TypeType returnTypeEnum;
    private List<TypeType> paramTypes;
    private boolean hasCallBack;
    private boolean returnCollection;
    private boolean returnMap;
    private boolean returnArray;
    private Class<?> returnType;
    private Class<?> returnTypeComponent;
    private Class<?> returnTypeComponentKey;
    private Class<?> returnTypeComponentValue;
    private boolean hasReturn;


    private String description;
    private String summary;
    private String returnDescription;

    public String getDescription() {
        return description;
    }

    public ServiceMethodMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
    }


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

        if (name == null) {
            if (methodAccess!=null) {
                name = methodAccess.name();
            }
        }
        return name;
    }

    public ServiceMethodMetaBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public TypeType getReturnTypeEnum() {
        return returnTypeEnum;
    }

    public ServiceMethodMetaBuilder setReturnTypeEnum(TypeType returnTypeEnum) {
        this.returnTypeEnum = returnTypeEnum;
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


            setHasCallBack(detectCallback());

            deduceReturnTypes();

            return new ServiceMethodMeta(isHasReturn(), getMethodAccess(), getName(), getRequestEndpoints(),
                    getReturnTypeEnum(), getParamTypes(), hasCallback(),isReturnCollection(),
                    isReturnMap(), isReturnArray(), getReturnType(),
                    getReturnTypeComponent(), getReturnTypeComponentKey(), getReturnTypeComponentValue(),
                    getDescription(), getSummary(), getReturnDescription());
        } else {
            return new ServiceMethodMeta(getName(), this.getRequestEndpoints(),
                    this.getReturnTypeEnum(), this.getParamTypes());
        }
    }

    private void deduceReturnTypes() {

        if (hasCallback()) {
            deduceReturnInfoFromCallbackArg();

        } else {

            returnType = methodAccess.returnType();

            returnTypeEnum = TypeType.getType(returnType);
            if (Collection.class.isAssignableFrom(returnType)) {
                returnCollection = true;
                ParameterizedType genericReturnType = (ParameterizedType) methodAccess.method().getGenericReturnType();

                this.returnTypeComponent = (Class) genericReturnType.getActualTypeArguments()[0];

            } else if (Map.class.isAssignableFrom(returnType)) {
                returnMap = true;
                ParameterizedType genericReturnType = (ParameterizedType) methodAccess.method().getGenericReturnType();
                this.returnTypeComponentKey = (Class) genericReturnType.getActualTypeArguments()[0];
                this.returnTypeComponentValue = (Class) genericReturnType.getActualTypeArguments()[1];
            } else {

                if (returnType.isArray()) {
                    returnArray = true;
                    this.returnTypeComponent = returnType.getComponentType();
                }

            }
        }

        if (returnType!= void.class && returnType !=Void.class) {

            hasReturn = true;
        }
    }

    private void deduceReturnInfoFromCallbackArg() {
        Type[] genericParameterTypes = methodAccess.method().getGenericParameterTypes();
        Type callback = genericParameterTypes[0];
        if (callback instanceof ParameterizedType) {
            Type callbackReturn = ((ParameterizedType) callback).getActualTypeArguments()[0];
            /* Now we know it is a map or list */
            if (callbackReturn instanceof ParameterizedType) {
                Class containerType = (Class)((ParameterizedType) callbackReturn).getRawType();

                this.returnTypeEnum = TypeType.getType(containerType);
                this.returnType = containerType;

                if (Collection.class.isAssignableFrom(containerType)) {
                    returnCollection = true;
                    this.returnTypeComponent =(Class) ((ParameterizedType) callbackReturn).getActualTypeArguments()[0];
                } else if (Map.class.isAssignableFrom(containerType)) {
                    returnMap = true;
                    this.returnTypeComponentKey =(Class)((ParameterizedType) callbackReturn).getActualTypeArguments()[0];
                    this.returnTypeComponentValue =(Class)((ParameterizedType) callbackReturn).getActualTypeArguments()[1];

                }
            }/* Now we know it is not a list or map */
            else if (callbackReturn instanceof Class) {
                this.returnType = ((Class) callbackReturn);

                if (returnType.isArray()) {
                    returnArray = true;
                }
                this.returnTypeEnum = TypeType.getType(returnType);
            }
        }
    }


    private boolean detectCallback() {
        boolean hasCallback = false;
        Class<?>[] classes = methodAccess.parameterTypes();
        if (classes.length > 0) {
            if (classes[0] == Callback.class) {
                hasCallback = true;
            }
        }
        return hasCallback;
    }

    public ServiceMethodMetaBuilder setHasCallBack(boolean hasCallBack) {
        this.hasCallBack = hasCallBack;
        return this;
    }

    public boolean isHasCallBack() {
        return hasCallBack;
    }


    public boolean hasCallback() {
        return hasCallBack;
    }

    public boolean isReturnCollection() {
        return returnCollection;
    }

    public ServiceMethodMetaBuilder setReturnCollection(boolean returnCollection) {
        this.returnCollection = returnCollection;
        return this;
    }

    public boolean isReturnMap() {
        return returnMap;
    }

    public ServiceMethodMetaBuilder setReturnMap(boolean returnMap) {
        this.returnMap = returnMap;
        return this;
    }

    public boolean isReturnArray() {
        return returnArray;
    }

    public ServiceMethodMetaBuilder setReturnArray(boolean returnArray) {
        this.returnArray = returnArray;
        return this;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public ServiceMethodMetaBuilder setReturnType(Class<?> returnType) {
        this.returnType = returnType;
        return this;
    }

    public Class<?> getReturnTypeComponent() {
        return returnTypeComponent;
    }

    public ServiceMethodMetaBuilder setReturnTypeComponent(Class<?> returnTypeComponent) {
        this.returnTypeComponent = returnTypeComponent;
        return this;
    }

    public Class<?> getReturnTypeComponentKey() {
        return returnTypeComponentKey;
    }

    public ServiceMethodMetaBuilder setReturnTypeComponentKey(Class<?> returnTypeComponentKey) {
        this.returnTypeComponentKey = returnTypeComponentKey;
        return this;
    }

    public Class<?> getReturnTypeComponentValue() {
        return returnTypeComponentValue;
    }

    public ServiceMethodMetaBuilder setReturnTypeComponentValue(Class<?> returnTypeComponentValue) {
        this.returnTypeComponentValue = returnTypeComponentValue;
        return this;
    }

    public boolean isHasReturn() {
        return hasReturn;
    }

    public ServiceMethodMetaBuilder setHasReturn(boolean hasReturn) {
        this.hasReturn = hasReturn;
        return this;
    }

    public ServiceMethodMetaBuilder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public ServiceMethodMetaBuilder setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
        return this;
    }


    public String getReturnDescription() {
        return returnDescription;
    }
}

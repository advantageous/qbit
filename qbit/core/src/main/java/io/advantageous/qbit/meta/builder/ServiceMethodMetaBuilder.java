package io.advantageous.qbit.meta.builder;


import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.http.request.HttpBinaryResponse;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.jsend.JSendResponse;
import io.advantageous.qbit.meta.GenericReturnType;
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


    private GenericReturnType genericReturnType = GenericReturnType.NONE;

    private Class<?> returnType;
    private Class<?> returnTypeComponent;
    private Class<?> returnTypeComponentKey;
    private Class<?> returnTypeComponentValue;
    private boolean hasReturn;


    private String description;
    private String summary;
    private String returnDescription;
    private int responseCode;
    private String contentType;

    public static ServiceMethodMetaBuilder serviceMethodMetaBuilder() {
        return new ServiceMethodMetaBuilder();
    }

    public String getDescription() {
        return description;
    }

    public ServiceMethodMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
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
            if (methodAccess != null) {
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
                    getReturnTypeEnum(), getParamTypes(), hasCallback(), getGenericReturnType(), getReturnType(),
                    getReturnTypeComponent(), getReturnTypeComponentKey(), getReturnTypeComponentValue(),
                    getDescription(), getSummary(), getReturnDescription(), getResponseCode(), getContentType());
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
                genericReturnType = GenericReturnType.COLLECTION;
                ParameterizedType genericReturnType = (ParameterizedType) methodAccess.method().getGenericReturnType();
                this.returnTypeComponent = (Class) genericReturnType.getActualTypeArguments()[0];
            } else if (Map.class.isAssignableFrom(returnType)) {
                genericReturnType = GenericReturnType.MAP;
                ParameterizedType genericReturnType = (ParameterizedType) methodAccess.method().getGenericReturnType();
                this.returnTypeComponentKey = (Class) genericReturnType.getActualTypeArguments()[0];
                this.returnTypeComponentValue = (Class) genericReturnType.getActualTypeArguments()[1];
            } else if (Optional.class.isAssignableFrom(returnType)) {
                genericReturnType = GenericReturnType.OPTIONAL;
                ParameterizedType genericReturnType = (ParameterizedType) methodAccess.method().getGenericReturnType();
                this.returnTypeComponent = (Class) genericReturnType.getActualTypeArguments()[0];
            } else if (JSendResponse.class.isAssignableFrom(returnType)) {
                genericReturnType = GenericReturnType.JSEND;
                ParameterizedType genericReturnType = (ParameterizedType) methodAccess.method().getGenericReturnType();

                final Type type = genericReturnType.getActualTypeArguments()[0];
                if (type instanceof Class) {
                    this.returnTypeComponent = (Class) type;
                    this.genericReturnType = GenericReturnType.JSEND;
                } else if (type instanceof ParameterizedType) {
                    final ParameterizedType jsendGenericReturnType = ((ParameterizedType) type);

                    if (jsendGenericReturnType.getRawType() instanceof Class) {

                        final Class rawType = (Class) jsendGenericReturnType.getRawType();
                        if (Collection.class.isAssignableFrom(rawType) || rawType.isArray()) {

                            this.genericReturnType = GenericReturnType.JSEND_ARRAY;
                            Type componentType = jsendGenericReturnType.getActualTypeArguments()[0];

                            if (componentType instanceof Class) {
                                this.returnTypeComponent = ((Class) componentType);
                            }

                        } else if (Map.class.isAssignableFrom(rawType)) {

                            this.genericReturnType = GenericReturnType.JSEND_MAP;

                            Type componentKey = jsendGenericReturnType.getActualTypeArguments()[0];

                            if (componentKey instanceof Class) {
                                this.returnTypeComponentKey = ((Class) componentKey);
                            }

                            this.genericReturnType = GenericReturnType.JSEND_MAP;
                            Type componentValue = jsendGenericReturnType.getActualTypeArguments()[0];

                            if (componentValue instanceof Class) {
                                this.returnTypeComponentValue = ((Class) componentValue);
                            }
                        }
                    }
                }
            } else if (returnType == HttpTextResponse.class) {
                genericReturnType = GenericReturnType.HTTP_TEXT_RESPONSE;
            } else if (returnType == HttpBinaryResponse.class) {
                genericReturnType = GenericReturnType.HTTP_BINARY_RESPONSE;
            } else {

                if (returnType.isArray()) {
                    genericReturnType = GenericReturnType.ARRAY;
                    this.returnTypeComponent = returnType.getComponentType();
                }

            }
        }

        if (returnType != void.class && returnType != Void.class) {

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
                final Class containerType = (Class) ((ParameterizedType) callbackReturn).getRawType();
                this.returnTypeEnum = TypeType.getType(containerType);
                this.returnType = containerType;
                if (Collection.class.isAssignableFrom(containerType)) {
                    this.genericReturnType = GenericReturnType.COLLECTION;
                    this.returnTypeComponent = (Class) ((ParameterizedType) callbackReturn).getActualTypeArguments()[0];
                } else if (Map.class.isAssignableFrom(containerType)) {
                    this.genericReturnType = GenericReturnType.MAP;
                    this.returnTypeComponentKey = (Class) ((ParameterizedType) callbackReturn).getActualTypeArguments()[0];
                    this.returnTypeComponentValue = (Class) ((ParameterizedType) callbackReturn).getActualTypeArguments()[1];
                } else if (Optional.class.isAssignableFrom(containerType)) {
                    this.genericReturnType = GenericReturnType.OPTIONAL;
                    this.returnTypeComponent = (Class) ((ParameterizedType) callbackReturn).getActualTypeArguments()[0];
                } else if (JSendResponse.class.isAssignableFrom(containerType)) {
                    final Type returnTypeForComponent = ((ParameterizedType) callbackReturn).getActualTypeArguments()[0];

                    if (returnTypeForComponent instanceof Class) {
                        this.returnTypeComponent = (Class) returnTypeForComponent;
                        this.genericReturnType = GenericReturnType.JSEND;
                    } else if (returnTypeForComponent instanceof ParameterizedType) {
                        final ParameterizedType jsendGenericReturnType = ((ParameterizedType) returnTypeForComponent);

                        if (jsendGenericReturnType.getRawType() instanceof Class) {

                            final Class rawType = (Class) jsendGenericReturnType.getRawType();
                            if (Collection.class.isAssignableFrom(rawType) || rawType.isArray()) {

                                this.genericReturnType = GenericReturnType.JSEND_ARRAY;
                                Type componentType = jsendGenericReturnType.getActualTypeArguments()[0];

                                if (componentType instanceof Class) {
                                    this.returnTypeComponent = ((Class) componentType);
                                }

                            } else if (Map.class.isAssignableFrom(rawType)) {

                                this.genericReturnType = GenericReturnType.JSEND_MAP;

                                Type componentKey = jsendGenericReturnType.getActualTypeArguments()[0];

                                if (componentKey instanceof Class) {
                                    this.returnTypeComponentKey = ((Class) componentKey);
                                }

                                this.genericReturnType = GenericReturnType.JSEND_MAP;
                                Type componentValue = jsendGenericReturnType.getActualTypeArguments()[0];

                                if (componentValue instanceof Class) {
                                    this.returnTypeComponentValue = ((Class) componentValue);
                                }
                            }
                        }

                    }
                }
            }/* Now we know it is not a list or map */ else if (callbackReturn instanceof Class) {
                this.returnType = ((Class) callbackReturn);

                if (this.returnType == HttpTextResponse.class) {
                    this.genericReturnType = GenericReturnType.HTTP_TEXT_RESPONSE;
                } else if (this.returnType == HttpBinaryResponse.class) {
                    this.genericReturnType = GenericReturnType.HTTP_BINARY_RESPONSE;
                } else if (returnType.isArray()) {
                    this.genericReturnType = GenericReturnType.ARRAY;
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

    public boolean isHasCallBack() {
        return hasCallBack;
    }

    public ServiceMethodMetaBuilder setHasCallBack(boolean hasCallBack) {
        this.hasCallBack = hasCallBack;
        return this;
    }

    public boolean hasCallback() {
        return hasCallBack;
    }

    public boolean isReturnCollection() {
        return genericReturnType == GenericReturnType.COLLECTION;
    }

    public ServiceMethodMetaBuilder setReturnCollection(boolean returnCollection) {
        this.genericReturnType = GenericReturnType.COLLECTION;
        return this;
    }

    public boolean isReturnMap() {
        return genericReturnType == GenericReturnType.MAP;
    }

    public ServiceMethodMetaBuilder setReturnMap(boolean returnMap) {
        this.genericReturnType = GenericReturnType.MAP;
        return this;
    }

    public boolean isReturnArray() {
        return genericReturnType == GenericReturnType.ARRAY;
    }

    public ServiceMethodMetaBuilder setReturnArray(boolean returnArray) {
        this.genericReturnType = GenericReturnType.ARRAY;
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

    public String getSummary() {
        return summary;
    }

    public ServiceMethodMetaBuilder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public ServiceMethodMetaBuilder setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
        return this;
    }

    public GenericReturnType getGenericReturnType() {
        return genericReturnType;
    }

    public ServiceMethodMetaBuilder setGenericReturnType(GenericReturnType genericReturnType) {
        this.genericReturnType = genericReturnType;
        return this;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ServiceMethodMetaBuilder setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public ServiceMethodMetaBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}

package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.AnnotationConstants;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.CallType;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.RequestMeta;
import io.advantageous.qbit.meta.params.*;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Allows you to build request meta data.
 */
public class RequestMetaBuilder {

    private CallType callType;
    private String requestURI;
    private List<ParameterMeta> parameters = new ArrayList<>();
    private List<RequestMethod> requestMethods = new ArrayList<>();
    private String description;
    private MultiMap<String, String> responseHeaders;


    public static RequestMetaBuilder requestMetaBuilder() {
        return new RequestMetaBuilder();
    }

    public static int findURIPosition(String path, String findString) {

        final String[] pathParts = Str.split(path, '/');
        int position;
        for (position = 0; position < pathParts.length; position++) {

            String pathPart = pathParts[position];
            if (pathPart.equals(findString)) {
                break;
            }
        }
        return position;
    }

    public MultiMap<String, String> getResponseHeaders() {
        if (responseHeaders == null) {
            responseHeaders = new MultiMapImpl<>();
        }
        return responseHeaders;
    }

    public RequestMetaBuilder setResponseHeaders(MultiMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
        return this;
    }

    public RequestMetaBuilder addResponseHeaders(String name, String value) {
        getResponseHeaders().add(name, value);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RequestMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CallType getCallType() {
        return callType;

    }

    public RequestMetaBuilder setCallType(CallType callType) {
        this.callType = callType;
        return this;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public RequestMetaBuilder setRequestURI(String requestURI) {
        this.requestURI = requestURI;
        return this;
    }

    public List<ParameterMeta> getParameters() {
        return parameters;
    }

    public RequestMetaBuilder setParameters(List<ParameterMeta> parameters) {
        this.parameters = parameters;
        return this;
    }

    public RequestMetaBuilder addParameter(ParameterMeta parameter) {
        this.parameters.add(parameter);
        return this;
    }

    public RequestMetaBuilder addParameters(ParameterMeta... parameters) {
        Collections.addAll(this.getParameters(), parameters);
        return this;
    }

    public List<RequestMethod> getRequestMethods() {
        return requestMethods;
    }

    public RequestMetaBuilder setRequestMethods(List<RequestMethod> requestMethods) {
        this.requestMethods = requestMethods;
        return this;
    }

    public RequestMeta build() {
        return new RequestMeta(getCallType(), getRequestMethods(),
                getRequestURI(), getParameters(), responseHeaders, responseHeaders != null && responseHeaders.size() > 0);
    }

    public void addParameters(final String rootPath, final String servicePath,
                              final String path, final MethodAccess methodAccess) {

        final List<List<AnnotationData>> paramsAnnotationData = methodAccess.annotationDataForParams();

        final List<TypeType> typeTypes = methodAccess.paramTypeEnumList();


        final Class<?>[] parameterTypes = methodAccess.method().getParameterTypes();


        final List<ParameterMeta> params = new ArrayList<>(typeTypes.size());


        for (int index = 0; index < typeTypes.size(); index++) {

            if (paramsAnnotationData.size() > index) {

                final List<AnnotationData> annotationDataList = paramsAnnotationData.get(index);

                final String finalPath = Str.join("/", rootPath, servicePath, path).replace("//", "/");

                final TypeType paramType = typeTypes.get(index);

                if (annotationDataList == null || annotationDataList.size() == 0) {
                    Param requestParam = getParam(finalPath, null, index, paramType, parameterTypes[index]);
                    final ParameterMeta param = createParamMeta(methodAccess, index, typeTypes, requestParam);


                    params.add(param);
                    continue;
                }

                boolean found = false;

                for (AnnotationData annotationData : annotationDataList) {


                    Param requestParam = getParam(finalPath, annotationData, index, paramType, parameterTypes[index]);

                    if (requestParam != null) {
                        final ParameterMeta param = createParamMeta(methodAccess, index, typeTypes, requestParam);
                        params.add(param);
                        found = true;
                        break;
                    }
                }


                if (!found) {
                    Param requestParam = getParam(finalPath, null, index, paramType, parameterTypes[index]);
                    final ParameterMeta param = createParamMeta(methodAccess, index, typeTypes, requestParam);
                    params.add(param);
                }
            }
        }

        this.parameters.addAll(params);


    }

    private ParameterMeta createParamMeta(final MethodAccess methodAccess, final int index,
                                          final List<TypeType> typeTypes, final Param requestParam) {

        final ParameterMetaBuilder builder = ParameterMetaBuilder.parameterMetaBuilder();
        builder.setType(typeTypes.get(index));
        builder.setParam(requestParam);

        final Type type = methodAccess.method().getGenericParameterTypes()[index];

        if (type instanceof ParameterizedType) {

            final ParameterizedType parameterizedType = ((ParameterizedType) type);

            final Class containerClass = (Class) parameterizedType.getRawType();
            builder.setClassType(containerClass);

            /* It is a collection or a map. */
            if (Collection.class.isAssignableFrom(containerClass)) {
                builder.setCollection();
                builder.setComponentClass((Class) parameterizedType.getActualTypeArguments()[0]);
            } else if (Map.class.isAssignableFrom(containerClass)) {
                builder.setMap();
                builder.setComponentClassKey((Class) parameterizedType.getActualTypeArguments()[0]);
                builder.setComponentClassValue((Class) parameterizedType.getActualTypeArguments()[1]);
            }
        } else {
            final Class classType = methodAccess.method().getParameterTypes()[index];
            builder.setClassType(classType);
            if (classType.isArray()) {
                builder.setComponentClass(classType.getComponentType());
                builder.setArray();
            }

        }

        return builder.build();
    }

    private Param getParam(final String path, final AnnotationData annotationData, final int index, TypeType paramType, Class<?> parameterType) {

        if (annotationData == null) {
            return new BodyParam(true, null, null);
        }

        Param param;
        final String paramName = getParamName(annotationData);
        final boolean required = getRequired(annotationData);
        final String description = getParamDescription(annotationData);
        final Object defaultValue = getDefaultValue(annotationData, paramType, parameterType);

        switch (annotationData.getName()) {
            case "requestParam":
                param = new RequestParam(required, paramName, defaultValue, description);
                break;
            case "headerParam":
                param = new HeaderParam(required, paramName, defaultValue, description);
                break;
            case "dataParam":
                param = new DataParam(required, paramName, defaultValue, description);
                break;
            case "pathVariable":

                if (!path.contains("{")) {
                    throw new IllegalStateException();
                }
                if (paramName == null || Str.isEmpty(paramName)) {

                    String findString = "{" + index + "}";

                    int position = findURIPosition(path, findString);

                    param = new URIPositionalParam(required, index, defaultValue, position, description);
                } else {
                    String findString = "{" + paramName + "}";
                    int position = findURIPosition(path, findString);
                    param = new URINamedParam(required, paramName, defaultValue, position, description);
                }
                break;
            default:
                param = null;
        }

        return param;
    }

    private Object getDefaultValue(AnnotationData annotationData, TypeType paramType, Class<?> parameterType) {

        if (annotationData == null)
            return null;

        final Object value = annotationData.getValues().get("defaultValue");

        if (value == null) {
            return null;
        }


        /** Support not string objects if we want to create params with stronger typed annotations. */
        if (!(value instanceof String)) {
            return value;
        }


        if (value.equals(AnnotationConstants.NOT_SET)) {
            switch (paramType) {
                case STRING:
                    return null;
                case INT:
                case FLOAT:
                case DOUBLE:
                case SHORT:
                case CHAR:
                    return 0;
                case BOOLEAN:
                    return false;
                default:
                    return null;
            }
        }

        return Conversions.coerce(paramType, parameterType, value);
    }

    private String getParamName(AnnotationData annotationData) {

        if (annotationData == null)
            return null;

        final Object value = annotationData.getValues().get("value");
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    private String getParamDescription(AnnotationData annotationData) {

        if (annotationData == null)
            return null;

        final Object value = annotationData.getValues().get("description");
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    private Boolean getRequired(AnnotationData annotationData) {

        if (annotationData == null)
            return false;

        final Object value = annotationData.getValues().get("required");
        if (value == null) {
            return false;
        }

        return value instanceof Boolean ? ((Boolean) value) : Boolean.valueOf(value.toString());
    }
}

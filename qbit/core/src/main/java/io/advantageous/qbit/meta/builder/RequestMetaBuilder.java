package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.CallType;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.RequestMeta;
import io.advantageous.qbit.meta.params.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows you to build request meta data.
 */
public class RequestMetaBuilder {

    private CallType callType;
    private String requestURI;
    private List<ParameterMeta> parameters = new ArrayList<>();
    private List<RequestMethod> requestMethods = new ArrayList<>();

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
                getRequestURI(), getParameters());
    }

    public void addParameters(final String rootPath, final String servicePath,
                              final String path, final MethodAccess methodAccess) {

        final List<List<AnnotationData>> paramsAnnotationData = methodAccess.annotationDataForParams();

        final List<TypeType> typeTypes = methodAccess.paramTypeEnumList();


        final List<ParameterMeta> params = new ArrayList<>(typeTypes.size());


        for (int index = 0; index < typeTypes.size(); index++) {

            if (paramsAnnotationData.size() > index) {

                final List<AnnotationData> annotationDataList = paramsAnnotationData.get(index);

                final String finalPath = Str.join("/", rootPath, servicePath, path).replace("//", "/");

                if (annotationDataList == null || annotationDataList.size() == 0) {
                    Param requestParam = getParam(finalPath, null, index);
                    final ParameterMeta param = ParameterMeta.param(methodAccess.method().getParameterTypes()[index],
                            typeTypes.get(index), requestParam);
                    params.add(param);
                    continue;
                }

                for (AnnotationData annotationData : annotationDataList) {


                    Param requestParam = getParam(finalPath, annotationData, index);

                    if (requestParam != null) {
                        final ParameterMeta param = ParameterMeta.param(methodAccess.method().getParameterTypes()[index],
                                typeTypes.get(index), requestParam);
                        params.add(param);
                        break;
                    }
                }
            }
        }

        this.parameters.addAll(params);


    }

    private Param getParam(final String path, final AnnotationData annotationData, final int index) {

        if (annotationData == null) {
            return new BodyParam(true, null);
        }

        Param param;
        String paramName = getParamName(annotationData);

        boolean required = getRequired(annotationData);


        String defaultValue = getDefaultValue(annotationData);

        switch (annotationData.getName()) {
            case "requestParam":
                param = new RequestParam(required, paramName, defaultValue);
                break;
            case "headerParam":
                param = new HeaderParam(required, paramName, defaultValue);
                break;
            case "pathVariable":

                if (!path.contains("{")) {
                    throw new IllegalStateException();
                }
                if (paramName == null || Str.isEmpty(paramName)) {

                    String findString = "{" + index + "}";

                    int position = findURIPosition(path, findString);

                    param = new URIPositionalParam(required, index, defaultValue, position);
                } else {
                    String findString = "{" + paramName + "}";
                    int position = findURIPosition(path, findString);
                    param = new URINamedParam(required, paramName, defaultValue, position);
                }
                break;
            default:
                param = null;
        }

        return param;
    }

    private String getDefaultValue(AnnotationData annotationData) {

        if (annotationData == null)
            return null;

        final Object value = annotationData.getValues().get("defaultValue");
        if (value == null) {
            return null;
        }

        return value.toString();
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

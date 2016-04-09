package io.advantageous.qbit.meta.builder;


import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.reflection.Annotated;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.HttpHeaders;
import io.advantageous.qbit.meta.CallType;
import io.advantageous.qbit.meta.ServiceMeta;
import io.advantageous.qbit.meta.ServiceMethodMeta;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.advantageous.qbit.meta.builder.ContextMetaBuilder.*;


/**
 * Allows you to build service method data.
 */
@SuppressWarnings("UnusedReturnValue")
public class ServiceMetaBuilder {


    private String name;
    private List<String> requestPaths = new ArrayList<>();
    private List<ServiceMethodMeta> methods = new ArrayList<>();


    private String description;
    private MultiMap<String, String> responseHeaders;

    public static ServiceMetaBuilder serviceMetaBuilder() {
        return new ServiceMetaBuilder();
    }

    public String getDescription() {
        return description;
    }

    public ServiceMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public ServiceMetaBuilder setName(String name) {
        this.name = name;
        return this;
    }


    public List<String> getRequestPaths() {
        return requestPaths;
    }

    public ServiceMetaBuilder setRequestPaths(List<String> requestPaths) {
        this.requestPaths = requestPaths;
        return this;
    }


    public ServiceMetaBuilder addRequestPath(String requestPath) {
        this.requestPaths.add(requestPath);
        return this;
    }


    public List<ServiceMethodMeta> getMethods() {
        return methods;
    }

    public ServiceMetaBuilder setMethods(List<ServiceMethodMeta> methods) {
        this.methods = methods;
        return this;
    }


    public ServiceMetaBuilder addMethod(ServiceMethodMeta method) {
        this.methods.add(method);
        return this;
    }


    public ServiceMetaBuilder addMethods(ServiceMethodMeta... methodArray) {
        Collections.addAll(methods, methodArray);
        return this;
    }

    public ServiceMeta build() {

        return new ServiceMeta(getName(), getRequestPaths(), getMethods(), getDescription());
    }


    public void addMethods(final String path, final Collection<MethodAccess> methods) {

        /* Only add methods that could be REST endpoints. */
        methods.stream().filter(methodAccess ->
                !methodAccess.isPrivate() && //No private methods
                        !methodAccess.isStatic() && //No static methods
                        !methodAccess.method().isSynthetic() && //No synthetic methods
                        !methodAccess.method().getDeclaringClass().getName().contains("$$EnhancerByGuice$$") &&
                        !methodAccess.name().contains("$")) //No methods with $ as this could be Scala generated
                // method or byte code lib generated
                .forEach(methodAccess -> addMethod(path, methodAccess));
    }

    public ServiceMetaBuilder addMethod(final String rootPath, final MethodAccess methodAccess) {

        for (String servicePath : this.getRequestPaths()) {


            final AnnotationData requestMapping = getAnnotationData(methodAccess);

            final List<String> requestPaths
                    = getRequestPathsByAnnotated(methodAccess, methodAccess.name().toLowerCase());


            if (requestMapping != null && requestMapping.getName().equals("bridge")) {
                requestPaths.add("/" + methodAccess.name().toLowerCase());
            }

            final String description = getDescriptionFromRequestMapping(methodAccess);

            final String returnDescription = getReturnDescriptionFromRequestMapping(methodAccess);

            final String summary = getSummaryFromRequestMapping(methodAccess);

            final int code = getCodeFromRequestMapping(methodAccess);


            final String contentType = getContentTypeFromRequestMapping(methodAccess);


            final MultiMap<String, String> responseHeaders = getResponseHeaders(methodAccess);

            final List<RequestMethod> requestMethods = getRequestMethodsByAnnotated(methodAccess);


            final ServiceMethodMetaBuilder serviceMethodMetaBuilder = ServiceMethodMetaBuilder.serviceMethodMetaBuilder();
            serviceMethodMetaBuilder.setMethodAccess(methodAccess);
            serviceMethodMetaBuilder.setDescription(description);
            serviceMethodMetaBuilder.setSummary(summary);
            serviceMethodMetaBuilder.setReturnDescription(returnDescription);
            serviceMethodMetaBuilder.setResponseCode(code);
            serviceMethodMetaBuilder.setContentType(contentType);

            for (String path : requestPaths) {
                CallType callType = path.contains("{") ? CallType.ADDRESS_WITH_PATH_PARAMS : CallType.ADDRESS;

                final RequestMetaBuilder requestMetaBuilder = new RequestMetaBuilder();

                requestMetaBuilder.setResponseHeaders(responseHeaders);

                requestMetaBuilder.addParameters(rootPath, servicePath, path, methodAccess);
                requestMetaBuilder.setCallType(callType).setRequestURI(path).setRequestMethods(requestMethods);
                serviceMethodMetaBuilder.addRequestEndpoint(requestMetaBuilder.build());
            }
            addMethod(serviceMethodMetaBuilder.build());
        }
        return this;


    }

    private MultiMap<String, String> getResponseHeaders(final Annotated annotated) {

        MultiMap<String, String> responseHeadersMap = MultiMap.empty();

        if (responseHeaders != null && responseHeaders.size() > 0) {
            responseHeadersMap = new MultiMapImpl<>();
            responseHeadersMap.putAll(responseHeaders);
        }

        final AnnotationData responseHeaderAnnotation = annotated.annotation("ResponseHeader");


        if (responseHeaderAnnotation != null) {
            final String name = responseHeaderAnnotation.getValues().get("name").toString();
            final String value = responseHeaderAnnotation.getValues().get("value").toString();

            if (responseHeadersMap.size() == 0) {
                responseHeadersMap = new MultiMapImpl<>();
            }
            responseHeadersMap.add(name, value);
        }


        final AnnotationData responseHeadersAnnotation = annotated.annotation("ResponseHeaders");

        if (responseHeadersAnnotation != null) {
            if (responseHeadersMap.size() == 0) {
                responseHeadersMap = new MultiMapImpl<>();
            }
            final Object[] values = (Object[]) responseHeadersAnnotation.getValues().get("value");

            for (Object object : values) {

                if (object instanceof Annotation) {
                    AnnotationData annotationData = new AnnotationData((Annotation) object);

                    final String name = annotationData.getValues().get("name").toString();
                    final String value = annotationData.getValues().get("value").toString();
                    responseHeadersMap.add(name, value);
                }
            }

        }


        final AnnotationData noCache = annotated.annotation("NoCacheHeaders");
        if (noCache != null) {
            if (responseHeadersMap.size() == 0) {
                responseHeadersMap = new MultiMapImpl<>();
            }
            responseHeadersMap.add(HttpHeaders.CACHE_CONTROL, "max-age=0");
            responseHeadersMap.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
        }


        final AnnotationData getAnnotation = annotated.annotation("GET");

        if (getAnnotation != null) {
            final Object noCache1 = getAnnotation.getValues().get("noCache");
            if (Conversions.toBoolean(noCache1)) {
                if (responseHeadersMap.size() == 0) {
                    responseHeadersMap = new MultiMapImpl<>();
                }
                responseHeadersMap.add(HttpHeaders.CACHE_CONTROL, "max-age=0");
                responseHeadersMap.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
            }
        }
        return responseHeadersMap;
    }


    public ServiceMetaBuilder setResponseHeaders(MultiMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
        return this;
    }
}

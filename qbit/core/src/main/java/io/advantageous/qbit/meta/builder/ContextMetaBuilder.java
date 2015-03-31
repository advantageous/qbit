package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.reflection.Annotated;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.ContextMeta;
import io.advantageous.qbit.meta.ServiceMeta;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContextMetaBuilder {

    public static ContextMetaBuilder contextMetaBuilder() {
        return new ContextMetaBuilder();
    }

    private String rootURI = "/services";
    private List<ServiceMeta> services = new ArrayList<ServiceMeta>();

    public String getRootURI() {
        return rootURI;
    }

    public ContextMetaBuilder setRootURI(String rootURI) {
        this.rootURI = rootURI;
        return this;
    }

    public List<ServiceMeta> getServices() {
        return services;
    }

    public ContextMetaBuilder setServices(List<ServiceMeta> services) {
        this.services = services;
        return this;
    }


    public ContextMetaBuilder addService(ServiceMeta service) {
        this.services.add(service);
        return this;
    }


    public ContextMetaBuilder addService(Class<?> serviceClass) {


        final ClassMeta<?> classMeta = ClassMeta.classMeta(serviceClass);
        String name = getServiceName(classMeta);

        final List<String> requestPaths = getRequestPathsByAnnotated(classMeta, name);


        final ServiceMetaBuilder serviceMetaBuilder = ServiceMetaBuilder.serviceMetaBuilder()
                .setRequestPaths(requestPaths).setName(name);

        serviceMetaBuilder.addMethods(this.getRootURI(), classMeta.methods());


        addService(serviceMetaBuilder.build());

        return this;
    }

    private String getServiceName(ClassMeta<?> classMeta) {
        AnnotationData annotationData = classMeta.annotation("Name");
        String name = "";

        if (annotationData == null) {
            annotationData = classMeta.annotation("Service");
            if (annotationData == null) {
                name = Str.camelCaseLower(classMeta.name());
            }
        }

        if (annotationData!=null) {
            annotationData.getValues().get("value");
        }

        return name;
    }

    public static List<String> getRequestPathsByAnnotated(Annotated classMeta, String name) {
        Object value = getRequestPath(classMeta, name);

        if (value instanceof String) {
            return Lists.list(asPath(value.toString()));
        } else if (value instanceof String[]){

            return Lists.list((String[])value);
        } else {
            throw new IllegalStateException();
        }
    }


    public static List<RequestMethod> getRequestMethodsByAnnotated(Annotated annotated) {

        final AnnotationData requestMapping = annotated.annotation("RequestMapping");

        if (requestMapping == null) {
            return Collections.singletonList(RequestMethod.GET);
        }

        final Object method = requestMapping.getValues().get("method");

        if (method == null) {

            return Collections.singletonList(RequestMethod.GET);
        }


        if (method instanceof RequestMethod[]) {
            final List<RequestMethod> requestMethods = Arrays.asList(((RequestMethod[]) method));
            return requestMethods;
        }

        if (method instanceof Object[]) {

            final Object[] methods = (Object[]) method;
            if (methods.length==0) {

                return Collections.singletonList(RequestMethod.GET);
            }
            final List<RequestMethod> requestMethods = new ArrayList<>(methods.length);

            for (Object object : methods) {
                requestMethods.add(RequestMethod.valueOf(object.toString()));
            }

            return requestMethods;

        }

        return Collections.singletonList(RequestMethod.valueOf(method.toString()));


    }

    static Object getRequestPath(Annotated classMeta, final String name) {
        final AnnotationData requestMapping = classMeta.annotation("RequestMapping");

        if (requestMapping!=null) {
            Object value = requestMapping.getValues().get("value");
            if (value == null) {
                value = "/" + name.toLowerCase();
            }
            return value;
        } else {
            return "/" + name.toLowerCase();
        }
    }

    public static String asPath(String s) {
        String path =s;
        if (!s.startsWith("/")) {
            path = "/" + s;
        }

        if (s.endsWith("/")) {
            if (s.length() > 2) {
                path = path.substring(0, path.length()-1);
            }
        }

        return path;
    }


    public ContextMetaBuilder addServices(ServiceMeta... serviceArray) {
        Collections.addAll(this.services, serviceArray);
        return this;
    }

    public ContextMeta build() {
        return new ContextMeta(rootURI, services);
    }
}

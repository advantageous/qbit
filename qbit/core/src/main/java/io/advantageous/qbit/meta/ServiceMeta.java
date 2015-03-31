package io.advantageous.qbit.meta;


import io.advantageous.boon.core.Lists;

import java.util.Collections;
import java.util.List;

public class ServiceMeta {

    public static ServiceMeta serviceMeta(final String name, final String address,
                                          final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, Lists.list(address), Lists.list(serviceMethods));
    }

    public static ServiceMeta service(final String name, final String address,
                                          final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, Lists.list(address), Lists.list(serviceMethods));
    }



    public static ServiceMeta serviceMeta(final String name, final List<String> requestPaths,
                                          final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, requestPaths, Lists.list(serviceMethods));
    }

    public static ServiceMeta service(final String name, final List<String> requestPaths,
                                      final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, requestPaths, Lists.list(serviceMethods));
    }


    private final String name;

    private final List<String> requestPaths;

    private final  List<ServiceMethodMeta> methods;

    public ServiceMeta(final String name, final List<String> requestPaths,
                       final List<ServiceMethodMeta> methods) {
        this.name = name;
        this.requestPaths = Collections.unmodifiableList(requestPaths);
        this.methods = Collections.unmodifiableList(methods);
    }

    public String getName() {
        return name;
    }

    public List<String> getRequestPaths() {
        return requestPaths;
    }

    public List<ServiceMethodMeta> getMethods() {
        return methods;
    }
}

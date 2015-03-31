package io.advantageous.qbit.meta;

import io.advantageous.boon.core.Lists;

import java.util.Collections;
import java.util.List;

public class ContextMeta {

    public static ContextMeta context(final String rootURI, final ServiceMeta... services) {
        return new ContextMeta(rootURI, Lists.list(services));
    }

    private final String rootURI;
    private final List<ServiceMeta> services;


    public ContextMeta(final String rootURI, final List<ServiceMeta> services) {
        this.rootURI = rootURI;
        this.services = Collections.unmodifiableList(services);
    }

    public String getRootURI() {
        return rootURI;
    }

    public List<ServiceMeta> getServices() {
        return services;
    }
}

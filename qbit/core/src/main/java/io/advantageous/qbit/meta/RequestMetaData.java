package io.advantageous.qbit.meta;


public class RequestMetaData {


    private final ContextMeta context;
    private final RequestMeta request;
    private final ServiceMethodMeta method;
    private final ServiceMeta service;
    private final String path;

    public RequestMetaData( final String path,
                            final ContextMeta context,
                            final RequestMeta request,
                            final ServiceMethodMeta method,
                            final ServiceMeta service) {
        this.request = request;
        this.method = method;
        this.service = service;
        this.path = path;
        this.context = context;
    }


    public RequestMeta getRequest() {
        return request;
    }

    public ServiceMethodMeta getMethod() {
        return method;
    }

    public ServiceMeta getService() {
        return service;
    }

    public String getPath() {
        return path;
    }

}

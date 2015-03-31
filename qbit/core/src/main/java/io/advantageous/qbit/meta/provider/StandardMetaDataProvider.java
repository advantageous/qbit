package io.advantageous.qbit.meta.provider;


import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.StringScanner;
import io.advantageous.qbit.meta.*;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class StandardMetaDataProvider implements MetaDataProvider {


    final Map<String, RequestMetaData> metaDataMap = new ConcurrentHashMap<>(100);
    final NavigableMap<String, RequestMetaData> treeMap = new TreeMap<>();



    public StandardMetaDataProvider(final ContextMeta context) {
        context.getServices().forEach(service -> addService(context, service));
    }

    private void addService(final ContextMeta context,
                            final ServiceMeta service) {

        service.getMethods().forEach(method -> addMethod(context, service, method));
    }

    private void addMethod(final ContextMeta context,
                           final ServiceMeta service,
                           final ServiceMethodMeta method) {

         method.getRequestEndpoints().forEach(requestMeta -> addRequest(context, service, method, requestMeta));
    }

    private void addRequest(final ContextMeta context,
                             final ServiceMeta service,
                             final ServiceMethodMeta method,
                             final RequestMeta requestMeta) {


        service.getRequestPaths().forEach(path -> addEndPoint(context, service, method, requestMeta, path));

    }

    private void addEndPoint(final ContextMeta context,
                             final ServiceMeta service,
                             final ServiceMethodMeta method,
                             final RequestMeta requestMeta,
                             final String servicePath) {



        String requestPath = requestMeta.getCallType() == CallType.ADDRESS ? requestMeta.getRequestURI() :
                StringScanner.substringBefore(requestMeta.getRequestURI(), "{");

        String path = Str.join('/', context.getRootURI(), servicePath, requestPath).replace("//", "/").toLowerCase();


        RequestMetaData metaData = new RequestMetaData(path, context, requestMeta, method, service);

        if (requestMeta.getCallType()== CallType.ADDRESS) {
            metaDataMap.put(path, metaData);
        } else {
            treeMap.put(path, metaData);
        }

    }


    @Override
    public RequestMetaData get(final String path) {
        return metaDataMap.get(path);
    }
}

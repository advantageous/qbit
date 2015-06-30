package io.advantageous.qbit.meta;

import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.builders.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaTransformer {

    public ServiceEndpointInfo serviceEndpointInfo(final ContextMeta contextMeta) {

        final ServiceEndpointInfoBuilder builder = new ServiceEndpointInfoBuilder();

        builder.getApiInfoBuilder().getContactBuilder().setEmail(contextMeta.getContactEmail());
        builder.getApiInfoBuilder().getContactBuilder().setName(contextMeta.getContactName());
        builder.getApiInfoBuilder().getContactBuilder().setUrl(contextMeta.getContactURL());
        builder.getApiInfoBuilder().getLicenseBuilder().setName(contextMeta.getLicenseName());
        builder.getApiInfoBuilder().getLicenseBuilder().setUrl(contextMeta.getLicenseURL());

        builder.setBasePath(contextMeta.getRootURI());
        builder.setHost(contextMeta.getHostAddress());

        final List<ServiceMeta> services = contextMeta.getServices();
        final Map<String, PathBuilder> pathBuilderMap = new HashMap<>();

        for (ServiceMeta serviceMeta : services) {

            final List<ServiceMethodMeta> methodMetas = serviceMeta.getMethods();
            for (ServiceMethodMeta methodMeta : methodMetas) {
                final List<RequestMeta> requestEndpoints = methodMeta.getRequestEndpoints();
                final MethodAccess methodAccess = methodMeta.getMethodAccess();

                for (RequestMeta requestMeta : requestEndpoints) {
                    final String requestURI = requestMeta.getRequestURI();

                    PathBuilder pathBuilder = pathBuilderMap.get(requestURI);
                    if (pathBuilder == null) {
                        pathBuilder = new PathBuilder();
                        pathBuilderMap.put(requestURI, pathBuilder);
                    }

                    final List<RequestMethod> requestMethods = requestMeta.getRequestMethods();


                    for (RequestMethod requestMethod : requestMethods) {
                        OperationBuilder operationBuilder = new OperationBuilder();
                        operationBuilder.setOperationId(methodAccess.name());

                        if (methodMeta.hasCallBack() || methodAccess.returnType() != void.class) {

                            ResponseBuilder responseBuilder = new ResponseBuilder();
                            //TODO add schema for response
                            operationBuilder.getResponses().put(200, responseBuilder.build());
                            operationBuilder.getProduces().add("application/json");

                        } else {
                            ResponseBuilder responseBuilder = new ResponseBuilder();
                            SchemaBuilder schemaBuilder = new SchemaBuilder();
                            schemaBuilder.setType("string");
                            operationBuilder.getResponses().put(201, responseBuilder.build());
                        }

                        switch (requestMethod) {
                            case GET:
                                pathBuilder.setGet(operationBuilder.build());
                                break;
                            case POST:
                                pathBuilder.setPost(operationBuilder.build());
                                break;
                            case PUT:
                                pathBuilder.setPut(operationBuilder.build());
                                break;
                            case OPTIONS:
                                pathBuilder.setOptions(operationBuilder.build());
                                break;
                            case DELETE:
                                pathBuilder.setDelete(operationBuilder.build());
                                break;
                            case HEAD:
                                pathBuilder.setHead(operationBuilder.build());
                                break;

                        }
                    }
                }
            }
        }

        return builder.build();
    }
}

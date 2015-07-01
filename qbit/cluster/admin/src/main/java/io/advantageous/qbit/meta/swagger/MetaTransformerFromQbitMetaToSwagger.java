package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.ContextMeta;
import io.advantageous.qbit.meta.RequestMeta;
import io.advantageous.qbit.meta.ServiceMeta;
import io.advantageous.qbit.meta.ServiceMethodMeta;
import io.advantageous.qbit.meta.swagger.builders.*;


import java.util.*;
import java.util.function.Consumer;

public class MetaTransformerFromQbitMetaToSwagger {

    final DefinitionClassCollector definitionClassCollector = new DefinitionClassCollector();

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

        buildDefinitions(services);



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

                            responseBuilder.setSchema(definitionClassCollector.getSchema(methodAccess.returnType()));
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

        final Set<Map.Entry<String, PathBuilder>> pathEntries = pathBuilderMap.entrySet();

        for (Map.Entry<String, PathBuilder> entry : pathEntries) {
            builder.addPath(entry.getKey(), entry.getValue().build());
        }

        final Map<String, Definition> definitionMap = definitionClassCollector.getDefinitionMap();

        definitionMap.entrySet().forEach(entry -> {
            builder.addDefinition(entry.getKey(), entry.getValue());
        });


        return builder.build();
    }

    private void buildDefinitions(final List<ServiceMeta> services) {


        services.forEach(serviceMeta -> {
            populateDefinitionMapByService(serviceMeta);
        });

    }

    private void populateDefinitionMapByService(final ServiceMeta serviceMeta) {
        serviceMeta.getMethods().forEach(serviceMethodMeta -> populateDefinitionMapByServiceMethod(serviceMethodMeta));
    }

    private void populateDefinitionMapByServiceMethod(final ServiceMethodMeta serviceMethodMeta) {

        MethodAccess methodAccess = serviceMethodMeta.getMethodAccess();
        definitionClassCollector.addClass(methodAccess.returnType());
        for (Class<?> paramType :  methodAccess.parameterTypes()) {
            definitionClassCollector.addClass(paramType);
        }
    }
}

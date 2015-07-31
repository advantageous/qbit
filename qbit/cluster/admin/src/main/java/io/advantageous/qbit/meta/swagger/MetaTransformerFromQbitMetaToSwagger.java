package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.*;
import io.advantageous.qbit.meta.params.*;
import io.advantageous.qbit.meta.swagger.builders.*;


import java.util.*;

public class MetaTransformerFromQbitMetaToSwagger {

    final DefinitionClassCollector definitionClassCollector = new DefinitionClassCollector();

    public ServiceEndpointInfo serviceEndpointInfo(final ContextMeta contextMeta) {

        final ServiceEndpointInfoBuilder builder = new ServiceEndpointInfoBuilder();

        builder.getApiInfoBuilder().getContactBuilder().setEmail(contextMeta.getContactEmail());
        builder.getApiInfoBuilder().getContactBuilder().setName(contextMeta.getContactName());
        builder.getApiInfoBuilder().getContactBuilder().setUrl(contextMeta.getContactURL());
        builder.getApiInfoBuilder().getLicenseBuilder().setName(contextMeta.getLicenseName());
        builder.getApiInfoBuilder().getLicenseBuilder().setUrl(contextMeta.getLicenseURL());

        builder.getApiInfoBuilder().getContactBuilder().setEmail(contextMeta.getContactEmail());
        builder.getApiInfoBuilder().getContactBuilder().setName(contextMeta.getContactName());
        builder.getApiInfoBuilder().setDescription(contextMeta.getDescription());
        builder.getApiInfoBuilder().getLicenseBuilder().setName(contextMeta.getLicenseName());
        builder.getApiInfoBuilder().getLicenseBuilder().setUrl(contextMeta.getLicenseURL());
        builder.getApiInfoBuilder().setTitle(contextMeta.getTitle());
        builder.getApiInfoBuilder().setVersion(contextMeta.getVersion());

        builder.setBasePath(contextMeta.getRootURI());
        builder.setHost(contextMeta.getHostAddress());

        final List<ServiceMeta> services = contextMeta.getServices();
        final Map<String, PathBuilder> pathBuilderMap = new HashMap<>();

        buildDefinitions(services);



        for (ServiceMeta serviceMeta : services) {

            final List<ServiceMethodMeta> methodMetas = serviceMeta.getMethods();

            final List<String> serviceMetaRequestPaths = serviceMeta.getRequestPaths();

            for (final String servicePath : serviceMetaRequestPaths) {

                extractPathsFromRequestMetaList(servicePath, contextMeta, pathBuilderMap, methodMetas);
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

    private void extractPathsFromRequestMetaList(String servicePath, ContextMeta contextMeta, Map<String, PathBuilder> pathBuilderMap, List<ServiceMethodMeta> methodMetas) {
        for (ServiceMethodMeta methodMeta : methodMetas) {
            final List<RequestMeta> requestEndpoints = methodMeta.getRequestEndpoints();
            final MethodAccess methodAccess = methodMeta.getMethodAccess();


            for (RequestMeta requestMeta : requestEndpoints) {


                final String requestURI = (servicePath + requestMeta.getRequestURI()).replaceAll("//", "/");

                final PathBuilder pathBuilder = createPathBuilderIfAbsent(pathBuilderMap, requestURI);

                final List<RequestMethod> requestMethods = requestMeta.getRequestMethods();


                for (RequestMethod requestMethod : requestMethods) {
                    extractPathFromRequestMeta(contextMeta, methodMeta, methodAccess, requestMeta,
                            pathBuilder, requestMethod);
                }
            }
        }
    }

    private void extractPathFromRequestMeta(ContextMeta contextMeta, ServiceMethodMeta methodMeta, MethodAccess methodAccess, RequestMeta requestMeta, PathBuilder pathBuilder, RequestMethod requestMethod) {
        final OperationBuilder operationBuilder = new OperationBuilder();


        addParameters(operationBuilder, requestMeta.getParameters(), methodAccess,
                requestMeta.getRequestURI(), contextMeta.getRootURI());
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

    private PathBuilder createPathBuilderIfAbsent(Map<String, PathBuilder> pathBuilderMap, String requestURI) {
        PathBuilder pathBuilder = pathBuilderMap.get(requestURI);
        if (pathBuilder == null) {
            pathBuilder = new PathBuilder();
            pathBuilderMap.put(requestURI, pathBuilder);
        }
        return pathBuilder;
    }

    private void addParameters(final OperationBuilder operationBuilder,
                               final List<ParameterMeta> parameterMetaList,
                               final MethodAccess methodAccess, final String requestURI,
                               final String baseURI
                               ) {


        final String[] uriParts = Str.split(requestURI, '/');



        for (ParameterMeta parameterMeta : parameterMetaList) {


            ParameterBuilder parameterBuilder = new ParameterBuilder();


            if (parameterMeta.getParam() instanceof NamedParam) {
                parameterBuilder.setName(((NamedParam) parameterMeta.getParam()).getName());
            }

            if (parameterMeta.getParam() instanceof RequestParam) {
                parameterBuilder.setIn("query");

            }
            if (parameterMeta.getParam() instanceof URINamedParam) {
                parameterBuilder.setIn("path");

            }

            if (parameterMeta.getParam() instanceof HeaderParam) {
                parameterBuilder.setIn("header");

            }


            if (parameterMeta.getParam() instanceof URIPositionalParam) {
                parameterBuilder.setIn("THIS QBIT FEATURE URI POSITIONAL PARAM IS NOT SUPPORTED BY SWAGGER");

            }


            if (parameterMeta.getParam() instanceof BodyArrayParam) {
                parameterBuilder.setIn("THIS QBIT FEATURE BodyArrayParam IS NOT SUPPORTED BY SWAGGER");

            }


            if (parameterMeta.getParam() instanceof BodyParam) {
                parameterBuilder.setIn("body");
                parameterBuilder.setName("body");

                /** TODO handle generic types */
                if (parameterMeta.getType() == TypeType.INSTANCE) {
                    parameterBuilder.setSchema(Schema.definitionRef(parameterMeta.getClassType().getSimpleName()));
                    parameterBuilder.setRequired(parameterMeta.getParam().isRequired());
                    operationBuilder.addParameter(parameterBuilder.build());
                    return;
                }
            }

            Schema schema = definitionClassCollector.getSchema(parameterMeta.getClassType());
            parameterBuilder.setType(schema.getType());

            if ( "array".equals(schema.getType()) ) {
                parameterBuilder.setItems(schema.getItems());
                parameterBuilder.setCollectionFormat("csv");
            }
            parameterBuilder.setRequired(parameterMeta.getParam().isRequired());
            operationBuilder.addParameter(parameterBuilder.build());
        }
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

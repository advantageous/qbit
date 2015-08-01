package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.*;
import io.advantageous.qbit.meta.params.*;
import io.advantageous.qbit.meta.swagger.builders.*;
import io.advantageous.qbit.reactive.Callback;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MetaTransformerFromQbitMetaToSwagger {

    private final DefinitionClassCollector definitionClassCollector = new DefinitionClassCollector();

    public ServiceEndpointInfo serviceEndpointInfo(final ContextMeta contextMeta) {

        final ServiceEndpointInfoBuilder builder = new ServiceEndpointInfoBuilder();

        populateAPIInfo(contextMeta, builder);

        builder.setBasePath(contextMeta.getRootURI());
        builder.setHost(contextMeta.getHostAddress());

        final List<ServiceMeta> services = contextMeta.getServices();
        final Map<String, PathBuilder> pathBuilderMap = new HashMap<>();

        buildDefinitions(builder, services);


        buildPaths(contextMeta, builder, services, pathBuilderMap);



        return builder.build();
    }

    private void buildPaths(final ContextMeta contextMeta,
                            final ServiceEndpointInfoBuilder builder,
                            final List<ServiceMeta> services,
                            final Map<String, PathBuilder> pathBuilderMap) {
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
    }

    private void populateAPIInfo(ContextMeta contextMeta, ServiceEndpointInfoBuilder builder) {
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

    private void extractPathFromRequestMeta(final ContextMeta contextMeta,
                                            final ServiceMethodMeta methodMeta,
                                            final MethodAccess methodAccess,
                                            final RequestMeta requestMeta,
                                            final PathBuilder pathBuilder,
                                            final RequestMethod requestMethod) {
        final OperationBuilder operationBuilder = new OperationBuilder();


        addParameters(operationBuilder, requestMeta.getParameters());
        operationBuilder.setOperationId(methodAccess.name());

        if (methodMeta.hasCallBack() || methodAccess.returnType() != void.class) {

            final ResponseBuilder responseBuilder = new ResponseBuilder();


            final Class actualReturnType = !methodMeta.hasCallBack()
                 ? methodAccess.returnType() :
                    getComponentClassForReturnFromMethodFromCallback(methodAccess);


            TypeType returnType = actualReturnType==null ? TypeType.NULL :
                    actualReturnType == void.class ? TypeType.VOID :
                    TypeType.getType(actualReturnType);

            switch (returnType) {
                case VOID:
                    responseBuilder.setSchema(definitionClassCollector.getSchema(String.class));
                    operationBuilder.getResponses().put(201, responseBuilder.build());
                    operationBuilder.getProduces().add("application/json");
                    break;

                case LIST:
                case SET:
                case COLLECTION:


                    Class componentReturnType;

                    if (methodMeta.hasCallBack()) {

                        ParameterizedType parameterizedType = (ParameterizedType) methodAccess.method().getGenericParameterTypes()[0];

                        ParameterizedType componentType = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
                        componentReturnType  = (Class) componentType.getActualTypeArguments()[0];
                    } else {
                        componentReturnType = calculateReturnTypeComponentClassForCollection(methodAccess);
                    }


                    responseBuilder.setSchema(definitionClassCollector.getSchema(actualReturnType, componentReturnType));
                    operationBuilder.getResponses().put(200, responseBuilder.build());
                    operationBuilder.getProduces().add("application/json");

                    break;


                case ARRAY:

                    responseBuilder.setSchema(definitionClassCollector.getSchema(actualReturnType,
                            actualReturnType.getComponentType()));
                    operationBuilder.getResponses().put(200, responseBuilder.build());
                    operationBuilder.getProduces().add("application/json");


                default:
                    responseBuilder.setSchema(definitionClassCollector.getSchema(actualReturnType));
                    operationBuilder.getResponses().put(200, responseBuilder.build());
                    operationBuilder.getProduces().add("application/json");

            }


        } else {
            final ResponseBuilder responseBuilder = new ResponseBuilder();
            final SchemaBuilder schemaBuilder = new SchemaBuilder();
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

    private Class getComponentClassForReturnFromMethodFromCallback(MethodAccess methodAccess) {

        if (methodAccess.parameterTypes().length > 0) {

            Type genericType = methodAccess.getGenericParameterTypes()[0];

            if (genericType instanceof ParameterizedType) {

                Type compType = ((ParameterizedType) genericType)
                        .getActualTypeArguments()[0];

                Class componentClassTypeForReturn = null;

                if (compType instanceof ParameterizedType) {
                    componentClassTypeForReturn = (Class) ((ParameterizedType) compType).getRawType();
                } else {
                    componentClassTypeForReturn = (Class) compType;
                }


                return componentClassTypeForReturn;
            }
        }

        return Object.class;

    }


    private Class getComponentClassForReturnFromMethod(MethodAccess methodAccess) {
        Class componentClassTypeForReturn = null;

        TypeType type = TypeType.getType(methodAccess.returnType());


        switch (type) {
            case LIST:
            case SET:

                componentClassTypeForReturn = calculateReturnTypeComponentClassForCollection(methodAccess);

                break;

            case ARRAY:
                componentClassTypeForReturn =methodAccess.returnType().getComponentType();
                break;

            default:
                componentClassTypeForReturn = null;
        }
        return componentClassTypeForReturn;
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
                               final List<ParameterMeta> parameterMetaList
                               //final MethodAccess methodAccess, final String requestURI,
                               //final String baseURI
                               ) {


        //final String[] uriParts = Str.split(requestURI, '/');



        for (final ParameterMeta parameterMeta : parameterMetaList) {


            if (parameterMeta.getClassType() == Callback.class) {
                continue;
            }

            final ParameterBuilder parameterBuilder = new ParameterBuilder();


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

    private void buildDefinitions(ServiceEndpointInfoBuilder builder, final List<ServiceMeta> services) {


        services.forEach(serviceMeta -> {
            populateDefinitionMapByService(serviceMeta);
        });

        final Map<String, Definition> definitionMap = definitionClassCollector.getDefinitionMap();

        definitionMap.entrySet().forEach(entry -> {
            builder.addDefinition(entry.getKey(), entry.getValue());
        });


    }

    private void populateDefinitionMapByService(final ServiceMeta serviceMeta) {
        serviceMeta.getMethods().forEach(serviceMethodMeta -> populateDefinitionMapByServiceMethod(serviceMethodMeta));
    }

    private void populateDefinitionMapByServiceMethod(final ServiceMethodMeta serviceMethodMeta) {

        final MethodAccess methodAccess = serviceMethodMeta.getMethodAccess();

        final TypeType type = methodAccess.returnType() == void.class ? TypeType.VOID :
                TypeType.getType(methodAccess.returnType());

        switch (type) {
            case LIST:
            case SET:


                final Class componentClassTypeForReturn = calculateReturnTypeComponentClassForCollection(methodAccess);

                if (componentClassTypeForReturn!=null) {
                    definitionClassCollector.addClass(componentClassTypeForReturn);
                }
                break;

            case ARRAY:
                definitionClassCollector.addClass(methodAccess.returnType().getComponentType());
                break;

            case VOID:
                Type[] genericParameterTypes = serviceMethodMeta.getMethodAccess().getGenericParameterTypes();
                if (genericParameterTypes.length > 1) {

                    Type genericParameterType = genericParameterTypes[0];

                    if (genericParameterType instanceof Callback) {
                        extractReturnTypeFromCallback(genericParameterType);
                    }
                }
                break;

            default:
                definitionClassCollector.addClass(methodAccess.returnType());
        }
        for (Class<?> paramType :  methodAccess.parameterTypes()) {
            definitionClassCollector.addClass(paramType);
        }
    }

    private void extractReturnTypeFromCallback(Type genericParameterType) {
        if (genericParameterType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericParameterType).getActualTypeArguments();
            Type argument = actualTypeArguments[0];
            final Class actualReturnClass = (Class) argument;

            final TypeType type = actualReturnClass == void.class ? TypeType.VOID :
                    TypeType.getType(actualReturnClass);




            switch (type) {
                case LIST:
                case SET:



                    final Class componentClassTypeForReturn =
                            calculateReturnTypeComponentFromGenericReturnType(genericParameterType);

                    if (componentClassTypeForReturn!=null) {
                        definitionClassCollector.addClass(componentClassTypeForReturn);
                    }
                    break;

                case ARRAY:
                    definitionClassCollector.addClass(actualReturnClass.getComponentType());
                    break;


                default:

                    definitionClassCollector.addClass(actualReturnClass);
            }

        }
    }

    private Class calculateReturnTypeComponentClassForCollection(MethodAccess methodAccess) {
        final Type genericReturnType = methodAccess.method().getGenericReturnType();
        return calculateReturnTypeComponentFromGenericReturnType(genericReturnType);

    }

    private Class calculateReturnTypeComponentFromGenericReturnType(final Type genericReturnType) {
        Class componentClassTypeForReturn = null;
        if (genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            Type argument = actualTypeArguments[0];
            componentClassTypeForReturn = (Class) argument;
        }
        return componentClassTypeForReturn;
    }
}

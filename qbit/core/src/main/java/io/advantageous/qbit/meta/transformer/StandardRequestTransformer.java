/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */
package io.advantageous.qbit.meta.transformer;


import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.core.value.ValueContainer;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.RequestMetaData;
import io.advantageous.qbit.meta.params.*;
import io.advantageous.qbit.meta.provider.StandardMetaDataProvider;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.CaptureRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static io.advantageous.boon.core.Str.sputs;

/**
 * The only implementation of RequestTransformer which takes a map
 * of StandardMetaDataProvider mapped to request methods (GET, POST), and uses it to
 * decide which method to invoke on an object.
 */
public class StandardRequestTransformer implements RequestTransformer {


    private final Logger logger = LoggerFactory.getLogger(StandardRequestTransformer.class);
    private final boolean debug = logger.isDebugEnabled();

    private final Map<RequestMethod, StandardMetaDataProvider> metaDataProviderMap;

    private final Factory factory = QBit.factory();

    protected final ThreadLocal<JsonMapper> jsonMapper = new ThreadLocal<JsonMapper>() {
        @Override
        protected JsonMapper initialValue() {
            return factory.createJsonMapper();
        }
    };
    private final Optional<Consumer<Throwable>> errorHandler;


    public StandardRequestTransformer(final Map<RequestMethod, StandardMetaDataProvider> metaDataProviderMap,
                                      final Optional<Consumer<Throwable>> errorHandler) {
        this.metaDataProviderMap = metaDataProviderMap;
        this.errorHandler = errorHandler;
    }

    private final String decodeURLEncoding(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }


    @Override
    public MethodCall<Object> transform(final HttpRequest request,
                                        final List<String> errorsList) {

        return transformByPosition(request, errorsList, false);
    }


    @Override
    public MethodCall<Object> transformByPosition(final HttpRequest request,
                                                  final List<String> errorsList, boolean byPosition) {


        final StandardMetaDataProvider standardMetaDataProvider = metaDataProviderMap
                .get(RequestMethod.valueOf(request.getMethod()));
        final RequestMetaData metaData = standardMetaDataProvider.get(request.address());


        final MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setAddress(request.address());
        methodCallBuilder.setOriginatingRequest(request);

        if (metaData == null) {
            errorsList.add("Unable to find handler");
            if (debug) {
                standardMetaDataProvider.getPaths()
                        .forEach(mappedPath -> logger.debug("Path not found path {}, mapped path {}", request.address(), mappedPath));
            }
            return null;
        }
        methodCallBuilder.setName(metaData.getMethod().getName());
        methodCallBuilder.setObjectName(metaData.getService().getName());


        final List<ParameterMeta> parameters = metaData.getRequest().getParameters();

        final List<Object> args = new ArrayList<>(parameters.size());

        int index = 0;
        loop:
        for (ParameterMeta parameterMeta : parameters) {

            ParamType paramType = parameterMeta.getParam().getParamType();

            paramType = paramType == null ? ParamType.BODY : paramType;

            Object value;
            NamedParam namedParam;

            if (parameterMeta.getClassType() == Callback.class) {
                continue;
            }

            switch (paramType) {
                case REQUEST:
                    namedParam = ((NamedParam) parameterMeta.getParam());
                    value = request.params().get(namedParam.getName());

                    if (namedParam.isRequired() && Str.isEmpty(value)) {
                        errorsList.add(sputs("Unable to find required request param", namedParam.getName()));
                        break loop;

                    }
                    if (Str.isEmpty(value)) {
                        value = namedParam.getDefaultValue();
                    }

                    value = value != null ? decodeURLEncoding(value.toString()) : value;

                    break;
                case HEADER:
                    namedParam = ((NamedParam) parameterMeta.getParam());
                    value = request.headers().get(namedParam.getName());
                    if (namedParam.isRequired() && Str.isEmpty(value)) {
                        errorsList.add(sputs("Unable to find required header param", namedParam.getName()));
                        break loop;
                    }

                    if (Str.isEmpty(value)) {
                        value = namedParam.getDefaultValue();
                    }
                    value = value != null ? decodeURLEncoding(value.toString()) : value;
                    break;
                case DATA:
                    namedParam = ((NamedParam) parameterMeta.getParam());
                    value = request.data().get(namedParam.getName());
                    if (namedParam.isRequired() && Str.isEmpty(value)) {
                        errorsList.add(sputs("Unable to find required data param", namedParam.getName()));
                        break loop;
                    }
                    if (value == null) {
                        value = namedParam.getDefaultValue();
                    }
                    break;
                case PATH_BY_NAME:
                    URINamedParam uriNamedParam = ((URINamedParam) parameterMeta.getParam());

                    final String[] split = Str.split(request.address(), '/');

                    if (uriNamedParam.getIndexIntoURI() >= split.length) {
                        if (uriNamedParam.isRequired()) {
                            errorsList.add(sputs("Unable to find required path param", uriNamedParam.getName()));
                            break loop;
                        }
                    }
                    value = split[uriNamedParam.getIndexIntoURI()];
                    if (uriNamedParam.isRequired() && Str.isEmpty(value)) {
                        errorsList.add(sputs("Unable to find required path param", uriNamedParam.getName()));
                        break loop;
                    }
                    if (Str.isEmpty(value)) {
                        value = uriNamedParam.getDefaultValue();
                    }
                    value = value != null ? decodeURLEncoding(value.toString()) : value;
                    break;

                case PATH_BY_POSITION:
                    URIPositionalParam positionalParam = ((URIPositionalParam) parameterMeta.getParam());

                    final String[] pathSplit = Str.split(request.address(), '/');

                    value = null;
                    if (positionalParam.getIndexIntoURI() >= pathSplit.length) {
                        if (positionalParam.isRequired()) {
                            errorsList.add(sputs("Unable to find required path param",
                                    positionalParam.getIndexIntoURI()));
                            break loop;
                        }
                    } else {
                        value = pathSplit[positionalParam.getIndexIntoURI()];
                        if (positionalParam.isRequired() && Str.isEmpty(value)) {
                            errorsList.add(sputs("Unable to find required path param",
                                    positionalParam.getIndexIntoURI()));
                            break loop;
                        }
                    }

                    if (Str.isEmpty(value)) {
                        value = positionalParam.getDefaultValue();
                    }
                    value = value != null ? decodeURLEncoding(value.toString()) : value;
                    break;

                case BODY:
                    final BodyParam bodyParam = (BodyParam) parameterMeta.getParam();
                    value = request.body();

                    final String contentType = request.getContentType();

                    if (isJsonContent(contentType)) {


                        if (value instanceof byte[]) {
                            final byte[] bytes = (byte[]) value;
                            value = new String(bytes, StandardCharsets.UTF_8);
                        }

                        if (bodyParam.isRequired() && Str.isEmpty(value)) {

                            errorsList.add("Unable to find body");
                            break loop;

                        }


                        if (Str.isEmpty(value)) {
                            value = bodyParam.getDefaultValue();


                        }

                        if (byPosition) {

                            value = jsonMapper.get().fromJson(value.toString());
                            value = ValueContainer.toObject(value);

                            if (value instanceof List) {
                                value = ((List) value).get(index);
                                value = ValueContainer.toObject(value);
                            }

                            try {
                                if (parameterMeta.isArray() || parameterMeta.isCollection()) {

                                    value = MapObjectConversion.convertListOfMapsToObjects(parameterMeta.getComponentClass(), (List<Map>) value);
                                } else {

                                    if (value instanceof Map) {
                                        value = MapObjectConversion.fromMap((Map) value, parameterMeta.getClassType());
                                    } else {
                                        value = Conversions.coerce(parameterMeta.getClassType(), value);
                                    }
                                }
                            } catch (Exception exception) {

                                handleMehtodTransformError(errorsList, methodCallBuilder, exception);
                            }
                        } else {

                            try {
                                if (parameterMeta.isArray() || parameterMeta.isCollection()) {
                                    value = jsonMapper.get().fromJsonArray(value.toString(), parameterMeta.getComponentClass());
                                } else if (parameterMeta.isMap()) {

                                    value = jsonMapper.get().fromJsonMap(value.toString(), parameterMeta.getComponentClassKey(),
                                            parameterMeta.getComponentClassValue());
                                } else {
                                    value = jsonMapper.get().fromJson(value.toString(), parameterMeta.getClassType());
                                }
                            } catch (Exception exception) {

                                handleMehtodTransformError(errorsList, methodCallBuilder, exception);
                            }
                        }
                    } else if (parameterMeta.isString()) {
                        if (value instanceof byte[]) {
                            final byte[] bytes = (byte[]) value;
                            value = new String(bytes, StandardCharsets.UTF_8);
                        }
                    }
                    break;

                case BODY_BY_POSITION:
                    BodyArrayParam bodyArrayParam = (BodyArrayParam) parameterMeta.getParam();
                    value = request.body();
                    if (value instanceof byte[]) {
                        final byte[] bytes = (byte[]) value;
                        value = new String(bytes, StandardCharsets.UTF_8);
                    }

                    if (bodyArrayParam.isRequired() && Str.isEmpty(value)) {

                        errorsList.add("Unable to find body");
                        break loop;

                    }

                    if (Str.isEmpty(value)) {
                        value = bodyArrayParam.getDefaultValue();
                    }

                    value = jsonMapper.get().fromJson(value.toString());

                    if (value instanceof List) {
                        final List list = (List) value;
                        final Object o = list.get(bodyArrayParam.getPosition());
                        if (o instanceof Map) {
                            //noinspection unchecked
                            value = MapObjectConversion.fromMap(((Map) o), parameterMeta.getClassType());
                        }
                    }
                    break;

                default:
                    throw new IllegalArgumentException();
            }

            args.add(value);


            index++;
        }

        methodCallBuilder.setBody(args);

        return methodCallBuilder.build();

    }

    @Override
    public MethodCall<Object> transFormBridgeBody(Object body, List<String> errors, String address, String method) {
        final String uri = ("/" + address + "/" + method).replace("//", "/");
        final HttpRequest request = HttpRequestBuilder.httpRequestBuilder().setUri(uri).setBody(body == null ? null : body.toString()).setMethod("BRIDGE").build();
        return this.transformByPosition(request, errors, true);
    }

    private void handleMehtodTransformError(List<String> errorsList, MethodCallBuilder methodCallBuilder, Exception exception) {
        if (errorHandler.isPresent()) {

            errorsList.add("Unable to JSON parse body :: " + exception.getMessage());
            final MethodCall<Object> methodCall = methodCallBuilder.build();
            final CaptureRequestInterceptor captureRequestInterceptor = new CaptureRequestInterceptor();
            captureRequestInterceptor.before(methodCall);
            errorHandler.get().accept(exception);
            captureRequestInterceptor.after(methodCall, null);

        } else {
            errorsList.add("Unable to JSON parse body :: " + exception.getMessage());
            logger.warn("Unable to parse object", exception);
        }
    }

    private boolean isJsonContent(final String contentType) {
        return Str.isEmpty(contentType) ||
                contentType.equals("application/json") ||
                contentType.equals("application/json;charset=utf-8") ||
                contentType.startsWith("application/json");
    }
}

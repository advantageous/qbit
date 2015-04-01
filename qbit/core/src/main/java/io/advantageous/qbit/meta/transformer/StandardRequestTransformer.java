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


import io.advantageous.boon.core.Str;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.RequestMetaData;
import io.advantageous.qbit.meta.params.*;
import io.advantageous.qbit.meta.provider.MetaDataProvider;


import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.core.Str.sputs;

public class StandardRequestTransformer implements RequestTransformer {


    private final MetaDataProvider metaDataProvider;

    private final Factory factory = QBit.factory();

    protected final ThreadLocal<JsonMapper> jsonMapper = new ThreadLocal<JsonMapper>() {
        @Override
        protected JsonMapper initialValue() {
            return factory.createJsonMapper();
        }
    };


    public StandardRequestTransformer(MetaDataProvider metaDataProvider) {
        this.metaDataProvider = metaDataProvider;
    }


    @Override
    public MethodCall<Object> transform(Request<Object> request, List<String> errorsList) {

        final RequestMetaData metaData = metaDataProvider.get(request.address());


        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setAddress(request.address());
        methodCallBuilder.setOriginatingRequest(request);

        if (metaData==null) {
            errorsList.add("Unable to find handler");
            return null;
        }
        methodCallBuilder.setName(metaData.getMethod().getName());
        methodCallBuilder.setObjectName(metaData.getService().getName());


        final List<ParameterMeta> parameters = metaData.getRequest().getParameters();

        final List<Object> args = new ArrayList<>(parameters.size());

        for (ParameterMeta parameterMeta : parameters) {

            ParamType paramType = parameterMeta.getParam().getParamType();

            paramType = paramType == null ? ParamType.BODY : paramType;

            Object value;
            NamedParam namedParam;

            switch (paramType) {
                case REQUEST:
                    namedParam = ((NamedParam) parameterMeta.getParam());
                    value = request.params().get(namedParam.getName());
                    if (namedParam.isRequired() && value == null) {
                        errorsList.add(sputs("Unable to find required request param", namedParam.getName()));
                        return null;

                    }
                    break;
                case HEADER:
                    namedParam = ((NamedParam) parameterMeta.getParam());
                    value = request.headers().get(namedParam.getName());
                    if (namedParam.isRequired() && value == null) {
                        errorsList.add(sputs("Unable to find required header param", namedParam.getName()));
                        return null;
                    }
                    break;
                case PATH_BY_NAME:
                    URINamedParam uriNamedParam = ((URINamedParam) parameterMeta.getParam());

                    final String[] split = Str.split(request.address(), '/');

                    if (uriNamedParam.getIndexIntoURI() >= split.length) {
                        if (uriNamedParam.isRequired()) {
                            errorsList.add(sputs("Unable to find required path param", uriNamedParam.getName()));
                            return null;
                        }
                    }

                    value = split[uriNamedParam.getIndexIntoURI()];
                    if (uriNamedParam.isRequired()) {
                        errorsList.add(sputs("Unable to find required path param", uriNamedParam.getName()));
                        return null;
                    }
                    break;

                case PATH_BY_POSITION:
                    URIPositionalParam positionalParam = ((URIPositionalParam) parameterMeta.getParam());

                    final String[] pathSplit = Str.split(request.address(), '/');

                    if (positionalParam.getIndexIntoURI() >= pathSplit.length) {
                        if (positionalParam.isRequired()) {
                            errorsList.add(sputs("Unable to find required path param",
                                    positionalParam.getIndexIntoURI()));
                            return null;
                        }
                    }

                    value = pathSplit[positionalParam.getIndexIntoURI()];
                    if (positionalParam.isRequired()) {
                        errorsList.add(sputs("Unable to find required path param",
                                positionalParam.getIndexIntoURI()));
                        return null;
                    }
                    break;

                case BODY:
                    BodyParam bodyParam = (BodyParam) parameterMeta.getParam();
                    value = request.body();
                    if (bodyParam.isRequired() || Str.isEmpty(value)) {

                        errorsList.add("Unable to find body");
                        return null;

                    }

                    value = jsonMapper.get().fromJson(value.toString());
                    break;

                default:
                    throw new IllegalArgumentException();
            }

            args.add(value);
            value = null;


        }

        methodCallBuilder.setBody(args);

        return methodCallBuilder.build();

    }
}

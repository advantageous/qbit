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
    public MethodCall<Object> transform(Request<Object> request) {

        final RequestMetaData metaData = metaDataProvider.get(request.address());

        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setAddress(request.address());
        methodCallBuilder.setOriginatingRequest(request);
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
                        return null;
                    }
                    break;
                case HEADER:
                    namedParam = ((NamedParam) parameterMeta.getParam());
                    value = request.headers().get(namedParam.getName());
                    if (namedParam.isRequired() && value == null) {
                        return null;
                    }
                    break;
                case PATH_BY_NAME:
                    URINamedParam uriNamedParam = ((URINamedParam) parameterMeta.getParam());

                    final String[] split = Str.split(request.address(), '/');

                    if (uriNamedParam.getIndexIntoURI() >= split.length) {
                        if (uriNamedParam.isRequired()) {
                            return null;
                        }
                    }

                    value = split[uriNamedParam.getIndexIntoURI()];
                    if (uriNamedParam.isRequired()) {
                        return null;
                    }
                    break;

                case PATH_BY_POSITION:
                    URIPositionalParam positionalParam = ((URIPositionalParam) parameterMeta.getParam());

                    final String[] pathSplit = Str.split(request.address(), '/');

                    if (positionalParam.getIndexIntoURI() >= pathSplit.length) {
                        if (positionalParam.isRequired()) {
                            return null;
                        }
                    }

                    value = pathSplit[positionalParam.getIndexIntoURI()];
                    if (positionalParam.isRequired()) {
                        return null;
                    }
                    break;

                case BODY:
                    BodyParam bodyParam = (BodyParam) parameterMeta.getParam();
                    value = request.body();
                    if (bodyParam.isRequired() || Str.isEmpty(value)) {
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

package io.advantageous.qbit.spi;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.MultiMap;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.impl.BoonServiceMethodCallHandler;
import io.advantageous.qbit.service.impl.ServiceBundleImpl;
import io.advantageous.qbit.service.impl.ServiceImpl;
import io.advantageous.qbit.service.method.impl.MethodCallImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Richard on 9/26/14.
 */
public class FactoryImpl implements Factory {

    private ProtocolParser defaultProtocol = new ProtocolParserVersion1();

    private List<ProtocolParser> protocolParserList = new ArrayList<>();

    {
        protocolParserList.add(new ProtocolParserVersion1());
    }

    @Override
    public MethodCall<Object> createMethodCallToBeEncodedAndSent(long id, String address,
                                                                 String returnAddress,
                                                                 String objectName,
                                                                 String methodName,
                                                                 long timestamp,
                                                                 Object body,
                                                                 MultiMap<String, String> params) {

        return MethodCallImpl.method(id, address, returnAddress, objectName, methodName, timestamp, body, params);
    }

    @Override
    public MethodCall<Object> createMethodCallToBeParsedFromBody(String address,
                                                                 String returnAddress,
                                                                 String objectName,
                                                                 String methodName,
                                                                 Object body,
                                                                 MultiMap<String, String> params) {

        MethodCall<Object> mc = null;
        MethodCallImpl methodCall =
                MethodCallImpl.method(0L, address, returnAddress, objectName, methodName, 0L, body, params);

        if (body != null) {
            ProtocolParser parser = selectProtocolParser(body, params);

            if (parser != null) {
                mc = parser.parseMethodCall(body);
            } else {
                mc = defaultProtocol.parseMethodCall(body);
            }
        }

        if (mc instanceof MethodCallImpl) {
            MethodCallImpl mcImpl = (MethodCallImpl) mc;
            mcImpl.overrides(methodCall);
            methodCall = mcImpl;
        } else {
            methodCall.overridesFromParams();
        }

        return methodCall;
    }

    @Override
    public MethodCall<Object> createMethodCallByAddress(String address, String returnAddress, Object args, MultiMap<String, String> params) {
        return createMethodCallToBeParsedFromBody(address, returnAddress, "", "", args, params);
    }

    @Override
    public MethodCall<Object> createMethodCallByNames(String methodName, String objectName, String returnAddress, Object args, MultiMap<String, String> params) {
        return createMethodCallToBeParsedFromBody("", returnAddress, methodName, objectName, args, params);
    }

    private ProtocolParser selectProtocolParser(Object args, MultiMap<String, String> params) {
        for (ProtocolParser parser : protocolParserList) {
            if (parser.supports(args, params)) {
                return parser;
            }
        }
        return null;
    }

    @Override
    public ServiceBundle createBundle(String path) {
        return new ServiceBundleImpl(path, 50, 5, this);
    }

    @Override
    public Service createService(String rootAddress, String serviceAddress, Object object, Queue<Response<Object>> responseQueue) {

        return new ServiceImpl(
                rootAddress,
                serviceAddress,
                object,
                GlobalConstants.POLL_WAIT, TimeUnit.MILLISECONDS,
                GlobalConstants.BATCH_SIZE,
                new BoonServiceMethodCallHandler(),
                responseQueue
        );

    }

    @Override
    public ProtocolEncoder createEncoder() {
        return new ProtocolEncoderVersion1();
    }
}

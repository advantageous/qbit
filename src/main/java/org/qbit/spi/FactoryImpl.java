package org.qbit.spi;

import org.boon.Boon;
import org.boon.Logger;
import org.boon.collections.MultiMap;
import org.boon.core.reflection.ClassMeta;
import org.qbit.Factory;
import org.qbit.GlobalConstants;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.Queue;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;
import org.qbit.service.impl.ServiceBundleImpl;
import org.qbit.service.impl.ServiceImpl;
import org.qbit.service.impl.ServiceMethodCallHandlerImpl;
import org.qbit.service.method.impl.MethodCallImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Richard on 9/26/14.
 */
public class FactoryImpl implements Factory{

    private ProtocolParser defaultProtocol = new ProtocolParserVersion1();

    private Logger logger = Boon.logger(FactoryImpl.class);

    private List<ProtocolParser> protocolParserList = new ArrayList<>();

    {
        protocolParserList.add(new ProtocolParserVersion1());
    }


    @Override
    public MethodCall<Object> createMethodCall(String address,
                                               String returnAddress,
                                       String objectName,
                                       String methodName,
                                       Object body,
                                       MultiMap<String, String> params){

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
        return createMethodCall(address, returnAddress, "", "", args, params);
    }

    @Override
    public MethodCall<Object> createMethodCallByNames(String methodName, String objectName, String returnAddress, Object args, MultiMap<String, String> params) {
        return createMethodCall("", returnAddress, methodName, objectName, args, params);
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


        if (GlobalConstants.DEBUG) {
            logger.info("createService2", object, responseQueue);
        }


        final ClassMeta<?> classMeta = ClassMeta.classMeta(object.getClass());

        return new ServiceImpl(
                rootAddress,
                serviceAddress,
                object,
                GlobalConstants.POLL_WAIT, TimeUnit.MILLISECONDS,
                GlobalConstants.BATCH_SIZE,
                new ServiceMethodCallHandlerImpl(),
                responseQueue
        );

    }
}

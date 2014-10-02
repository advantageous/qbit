package org.qbit.spi;

import org.boon.Boon;
import org.boon.Logger;
import org.boon.collections.MultiMap;
import org.boon.core.reflection.ClassMeta;
import org.qbit.Factory;
import org.qbit.GlobalConstants;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.proxy.*;
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
    private ServiceProxyFactory serviceProxyFactory = new ServiceProxyFactoryImpl();
    private ServiceProxyFactory serviceRemoteProxyFactory = new ServiceProxyForTextJsonImpl(this);


    private Logger logger = Boon.logger(FactoryImpl.class);

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


        MethodCallImpl methodCall =
                MethodCallImpl.method(id, address, returnAddress, objectName, methodName, timestamp, body, params);





        return methodCall;

    }

    @Override
    public <T> T createRemoteProxy(Class<T> serviceInterface, String address, String serviceName, String returnAddressArg, Sender<String> sender) {
        return serviceRemoteProxyFactory.createProxyWithReturnAddress(serviceInterface, serviceName, returnAddressArg, new SenderEndPoint(this.createEncoder(), address, sender));
    }


    @Override
    public MethodCall<Object> createMethodCallToBeParsedFromBody(String address,
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

    @Override
    public <T> T createLocalProxyWithReturnAddress(Class<T> serviceInterface, String serviceName, String returnAddressArg, ServiceBundle serviceBundle) {
        return serviceProxyFactory.createProxyWithReturnAddress(serviceInterface, serviceName, returnAddressArg, serviceBundle);
    }

    @Override
    public <T> T createLocalProxy(Class<T> serviceInterface, String serviceName, ServiceBundle serviceBundle) {
        return serviceProxyFactory.createProxy(serviceInterface, serviceName,  serviceBundle);
    }

    @Override
    public ProtocolEncoder createEncoder() {
        return new ProtocolEncoderVersion1();
    }
}

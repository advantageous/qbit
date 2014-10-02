package org.qbit;

import org.boon.collections.MultiMap;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.proxy.Sender;
import org.qbit.queue.Queue;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;
import org.qbit.spi.ProtocolEncoder;

public interface Factory {


    MethodCall<Object> createMethodCallToBeParsedFromBody(String address,
                                                          String returnAddress,
                                                          String objectName,
                                                          String methodName,
                                                          Object args,
                                                          MultiMap<String, String> params);


    MethodCall<Object> createMethodCallByAddress(String address,
                                                 String returnAddress,
                                                 Object args,
                                                 MultiMap<String, String> params);


    MethodCall<Object> createMethodCallByNames(
            String methodName, String objectName, String returnAddress, Object args,
            MultiMap<String, String> params);

    ServiceBundle createBundle(String path);

    Service createService(String rootAddress, String serviceAddress, Object object, Queue<Response<Object>> responseQueue);

    <T> T createLocalProxyWithReturnAddress(final Class<T> serviceInterface,
                                            final String serviceName,
                                            String returnAddressArg,
                                            final ServiceBundle serviceBundle);


    <T> T createLocalProxy(Class<T> serviceInterface,
                           String serviceName,
                           ServiceBundle serviceBundle);


    ProtocolEncoder createEncoder();

    MethodCall<Object> createMethodCallToBeEncodedAndSent(long id, String address,
                                                                 String returnAddress,
                                                                 String objectName,
                                                                 String methodName,
                                                                 long timestamp,
                                                                 Object body,
                                                                 MultiMap<String, String> params);



    <T> T createRemoteProxy(final Class<T> serviceInterface,
                            final String address,
                                            final String serviceName,
                                            String returnAddressArg,
                                            Sender<String> sender);




}
package org.qbit.service;

import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.ReceiveQueue;

import java.util.List;

/**
 * Created by Richard on 9/26/14.
 */
public interface ServiceBundle extends EndPoint{


    String address();

    void addService(String address, Object object);


    void addService(Object object);

    ReceiveQueue<Response<Object>> responses();


    void flushSends();


    void stop();


    List<String> endPoints();

    void startReturnHandlerProcessor();


    <T> T createLocalProxyWithReturnAddress(final Class<T> serviceInterface,
                                            final String serviceName,
                                            String returnAddressArg
                                            );


    <T> T createLocalProxy(Class<T> serviceInterface,
                           String serviceName
                           );
}

package io.advantageous.qbit.service;

import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;

import java.util.List;

/**
 * A client bundle is a collection of services.
 * The client bundle does the routing of calls based on addresses to a particular client.
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public interface ServiceBundle extends EndPoint {

    String address();

    void addService(String address, Object object);


    void addService(Object object);

    Queue<Response<Object>> responses();

    void flushSends();



    void stop();

    List<String> endPoints();

    void startReturnHandlerProcessor(ReceiveQueueListener<Response<Object>> listener);

    void startReturnHandlerProcessor();

    <T> T createLocalProxy(Class<T> serviceInterface, String myService);
}

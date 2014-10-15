package io.advantageous.qbit.service;

import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;

import java.util.List;

/**
 * Created by Richard on 9/26/14.
 */
public interface ServiceBundle extends EndPoint {

    String address();

    void addService(String address, Object object);


    void addService(Object object);

    ReceiveQueue<Response<Object>> responses();

    void flushSends();

    void stop();

    List<String> endPoints();

    void startReturnHandlerProcessor();

}

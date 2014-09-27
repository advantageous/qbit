package org.qbit.service;

import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.ReceiveQueue;

import java.util.List;

/**
 * Created by Richard on 9/26/14.
 */
public interface ServiceBundle {


    String address();

    void addService(String address, Object object);

    ReceiveQueue<Response<Object>> responses();

    void call(MethodCall<Object> methodCall);

    void flushSends();


    List<String> endPoints();

}

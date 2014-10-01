package org.qbit.service;

import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.ReceiveQueueListener;
import org.qbit.queue.SendQueue;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Richard on 9/8/14.
 */
public interface ServiceMethodHandler extends ReceiveQueueListener<MethodCall<Object>> {

    void init(Object service, String rootAddress, String serviceAddress);
    Response<Object> receiveMethodCall(MethodCall<Object> methodCall);

    String address();

    Collection<String> addresses();

    void initQueue(SendQueue<Response<Object>> responseSendQueue);
}

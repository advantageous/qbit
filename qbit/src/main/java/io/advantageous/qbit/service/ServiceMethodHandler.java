package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;

import java.util.Collection;

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

package org.qbit.service;

import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.ReceiveQueueListener;

import java.util.List;

/**
 * Created by Richard on 9/8/14.
 */
public interface ServiceMethodHandler extends ReceiveQueueListener<MethodCall<Object>> {

    void init(Object service);
    Response<Object> receiveMethodCall(MethodCall<Object> methodCall);

    String address();

    List<String> addresses();

}

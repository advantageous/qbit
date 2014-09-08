package org.qbit.service;

import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.ReceiveQueueListener;

/**
 * Created by Richard on 9/8/14.
 */
public interface ServiceMethodHandler extends ReceiveQueueListener<MethodCall<Object>> {

    void init(Object service);
    public Response<Object> receive(MethodCall<Object> methodCall, Object arg);

}

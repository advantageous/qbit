package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;

import java.util.Collection;

/**
 * This is a plugin just for the piece that does the invocation.
 * QBit has a boon implementation of this that uses reflection.
 * One could, for example, plugin an implementation of this that used bytecode generation.
 *
 * Created by Richard on 9/8/14.
 * @author rhightower
 */
public interface ServiceMethodHandler extends ReceiveQueueListener<MethodCall<Object>> {

    void init(Object service, String rootAddress, String serviceAddress);
    Response<Object> receiveMethodCall(MethodCall<Object> methodCall);

    String address();


    String name();

    Collection<String> addresses();

    void initQueue(SendQueue<Response<Object>> responseSendQueue);
}

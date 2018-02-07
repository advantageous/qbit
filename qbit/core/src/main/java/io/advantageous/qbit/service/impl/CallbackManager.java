package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;

public interface CallbackManager {
    void registerCallbacks(MethodCall<Object> methodCall);

    default void startReturnHandlerProcessor(Queue<Response<Object>> responseQueue) {

        //noinspection Convert2MethodRef
        responseQueue.startListener(response -> handleResponse(response));
    }

    void handleResponse(Response<Object> response);

    void process(long currentTime);
}

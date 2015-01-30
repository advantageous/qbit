package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rhightower on 1/30/15.
 */
public class CallbackManager {


    private final Logger logger = LoggerFactory.getLogger(CallbackManager.class);
    /**
     * Maps incoming calls with outgoing handlers (returns, async returns really).
     */
    private final Map<HandlerKey, Callback<Object>> handlers = new ConcurrentHashMap<>();

    /**
     * Register a callback handler
     *
     * @param methodCall method call
     * @param handler    call back handler to register
     */
    private void registerHandlerCallbackForClient(final MethodCall<Object> methodCall,
                                                  final Callback<Object> handler) {
        handlers.put(new HandlerKey(methodCall.returnAddress(), methodCall.id()), handler);
    }


    public void registerCallbacks(MethodCall<Object> methodCall) {
        Object args = methodCall.body();

        /** Look for callback handler in the args */
        if (args instanceof Iterable) {
            final Iterable list = (Iterable) args;
            for (Object arg : list) {
                if (arg instanceof Callback) {
                    registerHandlerCallbackForClient(methodCall, (Callback) arg);
                }
            }
        } else if (args instanceof Object[]) {
            final Object[] array = (Object[]) args;
            for (Object arg : array) {
                if (arg instanceof Callback) {
                    registerHandlerCallbackForClient(methodCall, ((Callback) arg));
                }
            }
        }
    }


    /**
     * Handles responses coming back from services.
     */
    public void startReturnHandlerProcessor(final Queue<Response<Object>> responseQueue)
     {

        responseQueue.startListener(new ReceiveQueueListener<Response<Object>>() {
            @Override
            public void receive(Response<Object> response) {
                final Callback<Object> handler = handlers.get(new HandlerKey(response.returnAddress(), response.id()));
                if (response.wasErrors()) {
                    if (response.body() instanceof Throwable) {
                        logger.error("Service threw an exception address", response.address(),
                                "\n return address", response.returnAddress(), "\n message id",
                                response.id(), response.body());
                        handler.onError(((Throwable) response.body()));
                    } else {
                        logger.error("Service threw an exception address", response.address(),
                                "\n return address", response.returnAddress(), "\n message id",
                                response.id());

                        handler.onError(new Exception(response.body().toString()));
                    }
                } else {
                    handler.accept(response.body());
                }
            }

        });
    }


}

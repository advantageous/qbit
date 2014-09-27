package org.qbit.service.impl;


import org.boon.Str;
import org.qbit.message.Event;
import org.qbit.message.Request;
import org.qbit.queue.*;
import org.qbit.message.MethodCall;
import org.qbit.queue.impl.BasicQueue;
import org.qbit.service.AfterMethodCall;
import org.qbit.service.BeforeMethodCall;
import org.qbit.service.Service;
import org.qbit.service.ServiceMethodHandler;
import org.qbit.service.method.impl.MethodCallImpl;
import org.qbit.message.Response;
import org.qbit.transforms.*;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Richard on 8/11/14.
 */
public class ServiceImpl implements Service {

    private final Object service;
    private final String name;
    private ServiceMethodHandler serviceMethodHandler;

    private BeforeMethodCall beforeMethodCall = new NoOpBeforeMethodCall();


    private BeforeMethodCall beforeMethodCallAfterTransform = new NoOpBeforeMethodCall();


    private AfterMethodCall afterMethodCall = new NoOpAfterMethodCall();


    private AfterMethodCall afterMethodCallAfterTransform = new NoOpAfterMethodCall();

    private ReceiveQueueListener<MethodCall> inputQueueListener = new NoOpInputMethodCallQueueListener();

    private final Queue<Response<Object>> responseQueue;

    private final Queue<MethodCall<Object>> requestQueue;


    private Transformer<Request, Object> requestObjectTransformer = new NoOpRequestTransform();

    private Transformer<Response, Response> responseObjectTransformer = new NoOpResponseTransformer();


    public ServiceImpl requestObjectTransformer(Transformer<Request, Object> requestObjectTransformer) {
        this.requestObjectTransformer = requestObjectTransformer;
        return this;
    }


    public ServiceImpl responseObjectTransformer(Transformer<Response, Response> responseObjectTransformer) {
        this.responseObjectTransformer = responseObjectTransformer;
        return this;
    }

    public ServiceImpl(final String name, final Object service,
                       int waitTime,
                       TimeUnit timeUnit,
                       int batchSize,
                       final ServiceMethodHandler serviceMethodHandler) {
        this.service = service;
        this.serviceMethodHandler = serviceMethodHandler;

        serviceMethodHandler.init(service);

        if (Str.isEmpty(name)) {

            this.name = serviceMethodHandler.address();

        } else {
            this.name = name;
        }

        requestQueue = new BasicQueue<>("Request Queue " + name, waitTime,
                timeUnit, batchSize);

        responseQueue = new BasicQueue<>("Response Queue " + name, waitTime,
                timeUnit, batchSize);


        final SendQueue<Response<Object>> responseSendQueue = responseQueue.sendQueue();
        requestQueue.startListener(new ReceiveQueueListener<MethodCall<Object>>() {
            @Override
            public void receive(MethodCall methodCall) {

                inputQueueListener.receive(methodCall);

                if (!beforeMethodCall.before(methodCall)) {
                    return;
                }

                final Object arg = requestObjectTransformer.transform(methodCall);

                if (beforeMethodCallAfterTransform != null) {
                    methodCall = MethodCallImpl.transformed(methodCall, arg);

                    if (!beforeMethodCallAfterTransform.before(methodCall)) {
                        return;
                    }
                }


                Response<Object> response = serviceMethodHandler.receiveMethodCall(methodCall);


                if (response != ServiceConstants.VOID) {

                    if (!afterMethodCall.after(methodCall, response)) {
                        return;
                    }

                    response = responseObjectTransformer.transform(response);


                    if (!afterMethodCallAfterTransform.after(methodCall, response)) {
                        return;
                    }

                    responseSendQueue.sendAndFlush(response);

                }
            }

            @Override
            public void empty() {


                if (inputQueueListener != null) {
                    inputQueueListener.empty();
                }

                serviceMethodHandler.empty();
            }

            @Override
            public void limit() {


                if (inputQueueListener != null) {
                    inputQueueListener.limit();
                }

                serviceMethodHandler.limit();

            }

            @Override
            public void shutdown() {


                if (inputQueueListener != null) {
                    inputQueueListener.shutdown();
                }


                serviceMethodHandler.shutdown();
            }

            @Override
            public void idle() {


                if (inputQueueListener != null) {
                    inputQueueListener.idle();
                }


                serviceMethodHandler.idle();
            }
        });

    }


    public Queue<Response<Object>> responseQueue() {
        return responseQueue;
    }

    @Override
    public SendQueue<MethodCall<Object>> requests() {
        return requestQueue.sendQueue();
    }

    @Override
    public ReceiveQueue<Response<Object>> responses() {
        return responseQueue.receiveQueue();
    }

    @Override
    public ReceiveQueue<Event> events() {
        return null;
    }

    @Override
    public void stop() {
        requestQueue.stop();
        responseQueue.stop();
    }

    @Override
    public List<String> addresses(String address) {

        return this.serviceMethodHandler.addresses();
    }


    public Object service() {
        return service;
    }


    public ServiceImpl beforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public ServiceImpl afterMethodCall(AfterMethodCall afterMethodCall) {
        this.afterMethodCall = afterMethodCall;
        return this;
    }

    public ServiceImpl beforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;
    }


    public ServiceImpl afterMethodCallAfterTransform(AfterMethodCall afterMethodCallAfterTransform) {
        this.afterMethodCallAfterTransform = afterMethodCallAfterTransform;
        return this;
    }

    public String name() {
        return name;
    }
}

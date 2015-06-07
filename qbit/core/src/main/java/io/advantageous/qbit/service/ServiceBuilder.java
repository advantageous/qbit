/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.service;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.ServiceHealthListener;
import io.advantageous.qbit.service.impl.NoOpAfterMethodCall;
import io.advantageous.qbit.service.impl.NoOpInputMethodCallQueueListener;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.service.impl.ServiceQueueImpl;
import io.advantageous.qbit.service.stats.ServiceStatsListener;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.NoOpResponseTransformer;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Builds a service queue for a service that sits behind a queue.
 * created by rhightower on 1/28/15.
 */
public class ServiceBuilder {


    private final Logger logger = LoggerFactory.getLogger(ServiceBuilder.class);
    private final boolean debug = logger.isDebugEnabled();
    private boolean handleCallbacks;
    private ServiceMethodHandler serviceMethodHandler;
    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    private AfterMethodCall afterMethodCall = new NoOpAfterMethodCall();
    private AfterMethodCall afterMethodCallAfterTransform = new NoOpAfterMethodCall();
    private ReceiveQueueListener<MethodCall<Object>> inputQueueListener = new NoOpInputMethodCallQueueListener();
    private Transformer<Request, Object> requestObjectTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;
    private Transformer<Response<Object>, Response> responseObjectTransformer = new NoOpResponseTransformer();
    private Queue<Response<Object>> responseQueue;
    private QueueBuilder requestQueueBuilder;
    private QueueBuilder responseQueueBuilder = new QueueBuilder();
    private boolean asyncResponse = true;
    private boolean invokeDynamic = true;
    private String rootAddress;
    private String serviceAddress;
    private Object serviceObject;
    private QBitSystemManager qBitSystemManager;
    private List<QueueCallBackHandler> queueCallBackHandlers;
    private Timer timer;


    public static ServiceBuilder serviceBuilder() {
        return new ServiceBuilder();
    }


    public List<QueueCallBackHandler> getQueueCallBackHandlers() {
        if (queueCallBackHandlers==null) {
            queueCallBackHandlers = new ArrayList<>();
        }
        return queueCallBackHandlers;
    }

    public ServiceBuilder setQueueCallBackHandlers(List<QueueCallBackHandler> queueCallBackHandlers) {
        this.queueCallBackHandlers = queueCallBackHandlers;
        return this;
    }

    public ServiceBuilder addQueueCallbackHandler(final QueueCallBackHandler queueCallBackHandler) {
        getQueueCallBackHandlers().add(queueCallBackHandler);
        return this;
    }

    private QueueCallBackHandler buildQueueCallBackHandler() {
        if (queueCallBackHandlers == null || queueCallBackHandlers.size()==0) {
            return new QueueCallBackHandler() {
                @Override
                public void queueLimit() {

                }

                @Override
                public void queueEmpty() {

                }
            };
        } else {
            return new QueueCallBackHandlerHub(queueCallBackHandlers);
        }
    }



    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public ServiceBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }

    public QueueBuilder getResponseQueueBuilder() {
        return responseQueueBuilder;
    }

    public ServiceBuilder setResponseQueueBuilder(QueueBuilder responseQueueBuilder) {
        this.responseQueueBuilder = responseQueueBuilder;
        return this;
    }

    public Queue<Response<Object>> getResponseQueue() {
        return responseQueue;
    }

    public ServiceBuilder setResponseQueue(Queue<Response<Object>> responseQueue) {
        this.responseQueue = responseQueue;
        return this;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public ServiceBuilder setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
        return this;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public ServiceBuilder setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
        return this;
    }

    public String getRootAddress() {
        return rootAddress;
    }

    public ServiceBuilder setRootAddress(String rootAddress) {
        this.rootAddress = rootAddress;
        return this;
    }

    public ServiceMethodHandler getServiceMethodHandler() {
        return serviceMethodHandler;
    }

    public ServiceBuilder setServiceMethodHandler(ServiceMethodHandler serviceMethodHandler) {
        this.serviceMethodHandler = serviceMethodHandler;
        return this;

    }

    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public ServiceBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;

    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;
    }

    public ServiceBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;

    }

    public AfterMethodCall getAfterMethodCall() {
        return afterMethodCall;
    }

    public ServiceBuilder setAfterMethodCall(AfterMethodCall afterMethodCall) {
        this.afterMethodCall = afterMethodCall;
        return this;

    }

    public AfterMethodCall getAfterMethodCallAfterTransform() {
        return afterMethodCallAfterTransform;
    }

    public ServiceBuilder setAfterMethodCallAfterTransform(AfterMethodCall afterMethodCallAfterTransform) {
        this.afterMethodCallAfterTransform = afterMethodCallAfterTransform;
        return this;

    }

    public ReceiveQueueListener<MethodCall<Object>> getInputQueueListener() {
        return inputQueueListener;
    }

    public ServiceBuilder setInputQueueListener(ReceiveQueueListener<MethodCall<Object>> inputQueueListener) {
        this.inputQueueListener = inputQueueListener;
        return this;

    }

    public Transformer<Request, Object> getRequestObjectTransformer() {
        return requestObjectTransformer;
    }

    public ServiceBuilder setRequestObjectTransformer(Transformer<Request, Object> requestObjectTransformer) {
        this.requestObjectTransformer = requestObjectTransformer;
        return this;

    }

    public Transformer<Response<Object>, Response> getResponseObjectTransformer() {
        return responseObjectTransformer;
    }

    public ServiceBuilder setResponseObjectTransformer(Transformer<Response<Object>, Response> responseObjectTransformer) {
        this.responseObjectTransformer = responseObjectTransformer;
        return this;

    }

    public QueueBuilder getRequestQueueBuilder() {
        return requestQueueBuilder;
    }

    public ServiceBuilder setRequestQueueBuilder(QueueBuilder requestQueueBuilder) {
        this.requestQueueBuilder = requestQueueBuilder;
        return this;

    }

    public boolean isAsyncResponse() {
        return asyncResponse;
    }

    public ServiceBuilder setAsyncResponse(boolean asyncResponse) {
        this.asyncResponse = asyncResponse;
        return this;

    }

    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public ServiceBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }

    public boolean isHandleCallbacks() {
        return handleCallbacks;
    }

    public ServiceBuilder setHandleCallbacks(final boolean handleCallbacks) {
        this.handleCallbacks = handleCallbacks;
        return this;
    }



    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public ServiceBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public ServiceBuilder registerHealthChecks(
            final HealthServiceAsync healthServiceAsync,
            final String serviceName) {
        this.addQueueCallbackHandler(new ServiceHealthListener(serviceName, healthServiceAsync,
                getTimer(), 5, 10, TimeUnit.SECONDS));
        return this;
    }

    public ServiceBuilder registerStatsCollections(
            final String serviceName,
            final StatsCollector statsCollector,
            final int flushTimeSeconds,
            final int sampleEvery) {

        this.addQueueCallbackHandler(new ServiceStatsListener(serviceName, statsCollector,
                getTimer(),flushTimeSeconds, TimeUnit.SECONDS, sampleEvery ));

        return this;
    }


    public ServiceBuilder registerHealthChecksWithTTLInSeconds(
            final HealthServiceAsync healthServiceAsync,
            final String serviceName, final int seconds) {

        int ttl = seconds > 2 ? seconds : 10;

        int checkInterval = (ttl / 2==0) ? 1 : ttl/2;


        this.addQueueCallbackHandler(new ServiceHealthListener(serviceName, healthServiceAsync,
                getTimer(), checkInterval, ttl, TimeUnit.SECONDS));
        return this;
    }

    public ServiceQueue build(final Object serviceObject) {
        this.serviceObject = serviceObject;
        return build();
    }

    /**
     * Builds a service.
     * @return new service queue
     */
    public ServiceQueue build() {


        if (debug) logger.debug("Building a service");

        ServiceQueue serviceQueue = new ServiceQueueImpl(this.getRootAddress(),
                this.getServiceAddress(),
                this.getServiceObject(),
                this.getRequestQueueBuilder(),
                this.getResponseQueueBuilder(),
                QBit.factory().createServiceMethodHandler(this.isInvokeDynamic()),
                this.getResponseQueue(),
                this.isAsyncResponse(),
                this.isHandleCallbacks(),
                this.getSystemManager(),
                buildQueueCallBackHandler());

        if (qBitSystemManager != null) {
            qBitSystemManager.registerService(serviceQueue);
        }

        return serviceQueue;
    }

    /**
     * Builds and starts the service queue.
     *
     * @return the service queue
     */
    public ServiceQueue buildAndStart() {

        return build().startServiceQueue();
    }

}

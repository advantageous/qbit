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

import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.impl.QueueCallBackHandlerHub;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.ServiceHealthListener;
import io.advantageous.qbit.service.impl.*;
import io.advantageous.qbit.service.stats.ServiceQueueSizer;
import io.advantageous.qbit.service.stats.ServiceStatsListener;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.NoOpResponseTransformer;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private QueueBuilder responseQueueBuilder;
    private boolean asyncResponse = true;
    private boolean invokeDynamic = true;
    private String rootAddress;
    private String serviceAddress;
    private Object serviceObject;
    private QBitSystemManager qBitSystemManager;
    private List<QueueCallBackHandler> queueCallBackHandlers;
    private Timer timer;
    private StatsConfig statsConfig;

    private CallbackManager callbackManager;
    private CallbackManagerBuilder callbackManagerBuilder;

    private boolean createCallbackHandler = true;
    private EventManager eventManager;
    private BeforeMethodSent beforeMethodSent;
    private boolean joinEventManager = true;


    public static ServiceBuilder serviceBuilder() {
        return new ServiceBuilder();
    }


    public boolean isCreateCallbackHandler() {
        return createCallbackHandler;
    }

    public ServiceBuilder setCreateCallbackHandler(boolean createCallbackHandler) {
        this.createCallbackHandler = createCallbackHandler;
        return this;
    }

    public CallbackManagerBuilder getCallbackManagerBuilder() {
        if (callbackManagerBuilder == null && isCreateCallbackHandler()) {
            callbackManagerBuilder = CallbackManagerBuilder.callbackManagerBuilder();
            if (serviceObject != null) {
                callbackManagerBuilder.setName(serviceObject.getClass().getSimpleName());
            }
        }
        return callbackManagerBuilder;
    }

    public ServiceBuilder setCallbackManagerBuilder(CallbackManagerBuilder callbackManagerBuilder) {
        this.callbackManagerBuilder = callbackManagerBuilder;
        return this;
    }

    public CallbackManager getCallbackManager() {
        if (callbackManager == null && isCreateCallbackHandler()) {
            callbackManager = this.getCallbackManagerBuilder().build();
        }
        return callbackManager;
    }

    public ServiceBuilder setCallbackManager(CallbackManager callbackManager) {
        this.callbackManager = callbackManager;
        return this;
    }

    public List<QueueCallBackHandler> getQueueCallBackHandlers() {
        if (queueCallBackHandlers == null) {
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
        if (queueCallBackHandlers == null || queueCallBackHandlers.size() == 0) {
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
        if (responseQueueBuilder == null) {
            this.responseQueueBuilder = QueueBuilder.queueBuilder();
        }
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

    public ServiceBuilder setServiceObject(final Object serviceObject) {

        if (serviceObject == null) {
            throw new IllegalArgumentException("ServiceBuilder setServiceObject:: serviceObject cant be null");
        }

        ClassMeta<?> classMeta = ClassMeta.classMeta(serviceObject.getClass());

        Iterable<MethodAccess> methods = classMeta.methods();

        Set<String> methodNames = new HashSet<>();


        for (MethodAccess methodAccess : methods) {

            if (methodAccess.isPrivate()
                    || methodAccess.method().getDeclaringClass().getName().contains("$$EnhancerByGuice$$")) {
                continue;
            }


            if (methodNames.contains(methodAccess.name())) {
                logger.error("QBit does not support method overloading methods in Service Queues" +
                        " problem name "
                        + methodAccess.name() + " is overloaded " + methodNames + " from class "
                        + classMeta.longName());

            } else {
                methodNames.add(methodAccess.name());
            }

        }

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

        if (serviceMethodHandler == null) {
            serviceMethodHandler =
                    QBit.factory().createServiceMethodHandler(this.isInvokeDynamic());
        }
        return serviceMethodHandler;
    }

    public ServiceBuilder setServiceMethodHandler(ServiceMethodHandler serviceMethodHandler) {
        this.serviceMethodHandler = serviceMethodHandler;
        return this;

    }

    public BeforeMethodCall getBeforeMethodCall() {

        if (beforeMethodCall == null) {
            beforeMethodCall = new NoOpBeforeMethodCall();
        }
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
        if (afterMethodCall == null) {
            afterMethodCall = new NoOpAfterMethodCall();
        }
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
        if (requestQueueBuilder == null) {
            requestQueueBuilder = QueueBuilder.queueBuilder();
        }
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

        statsConfig = new StatsConfig(serviceName, statsCollector, flushTimeSeconds, sampleEvery);
        return this;
    }


    public ServiceBuilder registerHealthChecksWithTTLInSeconds(
            final HealthServiceAsync healthServiceAsync,
            final String serviceName, final int seconds) {

        int ttl = seconds > 2 ? seconds : 10;

        int checkInterval = (ttl / 2 == 0) ? 1 : ttl / 2;


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
     *
     * @return new service queue
     */
    public ServiceQueue build() {


        if (debug) logger.debug("Building a service");

        ServiceQueueSizer serviceQueueSizer = null;

        if (statsConfig != null) {


            serviceQueueSizer = new ServiceQueueSizer();
            this.addQueueCallbackHandler(new ServiceStatsListener(statsConfig.serviceName,
                    statsConfig.statsCollector,
                    getTimer(), statsConfig.flushTimeSeconds, TimeUnit.SECONDS,
                    statsConfig.sampleEvery, serviceQueueSizer));
        }

        ServiceQueue serviceQueue = new ServiceQueueImpl(this.getRootAddress(),
                this.getServiceAddress(),
                this.getServiceObject(),
                this.getRequestQueueBuilder(),
                this.getResponseQueueBuilder(),
                this.getServiceMethodHandler(),
                this.getResponseQueue(),
                this.isAsyncResponse(),
                this.isHandleCallbacks(),
                this.getSystemManager(),
                this.getBeforeMethodCall(),
                this.getBeforeMethodCallAfterTransform(),
                this.getAfterMethodCall(),
                this.getAfterMethodCallAfterTransform(),
                buildQueueCallBackHandler(),
                getCallbackManager(),
                getBeforeMethodSent(),
                getEventManager(),
                isJoinEventManager()
        );

        if (serviceQueueSizer != null) {
            serviceQueueSizer.setServiceQueue(serviceQueue);
        }

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


    public ServiceQueue buildAndStartAll() {

        this.setCreateCallbackHandler(true);
        return build().startAll();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ServiceBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    public BeforeMethodSent getBeforeMethodSent() {
        return beforeMethodSent;
    }

    public ServiceBuilder setBeforeMethodSent(final BeforeMethodSent beforeMethodSent) {
        this.beforeMethodSent = beforeMethodSent;
        return this;
    }

    public ServiceBuilder copy() {
        ServiceBuilder serviceBuilder = new ServiceBuilder();
        serviceBuilder.setAfterMethodCall(this.getAfterMethodCall());
        serviceBuilder.setBeforeMethodCall(this.getBeforeMethodCall());
        serviceBuilder.setAsyncResponse(this.isAsyncResponse());
        serviceBuilder.setEventManager(this.getEventManager());
        serviceBuilder.setHandleCallbacks(this.handleCallbacks);
        serviceBuilder.setSystemManager(this.getSystemManager());
        serviceBuilder.setBeforeMethodCallAfterTransform(this.getBeforeMethodCallAfterTransform());
        serviceBuilder.setResponseQueueBuilder(this.getResponseQueueBuilder());
        serviceBuilder.setRequestQueueBuilder(this.getRequestQueueBuilder());
        return serviceBuilder;
    }

    public boolean isJoinEventManager() {
        return joinEventManager;
    }

    public ServiceBuilder setJoinEventManager(boolean joinEventManager) {
        this.joinEventManager = joinEventManager;
        return this;
    }

    private static class StatsConfig {
        final String serviceName;
        final StatsCollector statsCollector;
        final int flushTimeSeconds;
        final int sampleEvery;

        private StatsConfig(String serviceName, StatsCollector statsCollector, int flushTimeSeconds, int sampleEvery) {
            this.serviceName = serviceName;
            this.statsCollector = statsCollector;
            this.flushTimeSeconds = flushTimeSeconds;
            this.sampleEvery = sampleEvery;
        }
    }
}

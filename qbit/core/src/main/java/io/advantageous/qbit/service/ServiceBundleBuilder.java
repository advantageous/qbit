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

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.impl.CallbackManager;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.Timer;

import java.util.Properties;

/**
 * Allows for the programmatic construction of a service bundle.
 *
 * @author rhightower
 *         created by Richard on 11/14/14.
 */
public class ServiceBundleBuilder {


    private static final String QBIT_SERVER_BUNDLE_BUILDER = "qbit.service.bundle.builder.";
    private QueueBuilder requestQueueBuilder;
    private QueueBuilder responseQueueBuilder;
    private QueueBuilder webResponseQueueBuilder;
    private boolean invokeDynamic = true;
    private String address = "/services";
    private boolean eachServiceInItsOwnThread = true;
    private QBitSystemManager qBitSystemManager;
    private Queue<Response<Object>> responseQueue;
    private HealthServiceAsync healthService = null;
    private StatsCollector statsCollector = null;
    private int statsFlushRateSeconds;
    private int checkTimingEveryXCalls = -1;
    private BeforeMethodSent beforeMethodSent;


    private CallbackManager callbackManager;
    private CallbackManagerBuilder callbackManagerBuilder;
    private EventManager eventManager;
    private BeforeMethodCall beforeMethodCallOnServiceQueue;

    private Factory factory;
    private AfterMethodCall afterMethodCallOnServiceQueue;
    /**
     * Allows interception of method calls before they get sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows interception of method calls before they get transformed and sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows transformation of arguments, for example from JSON to Java objects.
     */
    private Transformer<Request, Object> argTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;
    private Timer timer;

    public ServiceBundleBuilder(PropertyResolver propertyResolver) {
        this.invokeDynamic = propertyResolver.getBooleanProperty("invokeDynamic", true);
        this.statsFlushRateSeconds = propertyResolver.getIntegerProperty("statsFlushRateSeconds", 5);
        this.checkTimingEveryXCalls = propertyResolver.getIntegerProperty("checkTimingEveryXCalls", 10000);

    }

    public ServiceBundleBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(QBIT_SERVER_BUNDLE_BUILDER));
    }

    public ServiceBundleBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(
                QBIT_SERVER_BUNDLE_BUILDER, properties));
    }

    public static ServiceBundleBuilder serviceBundleBuilder() {
        return new ServiceBundleBuilder();
    }

    public Factory getFactory() {
        if (factory == null) {
            factory = QBit.factory();
        }
        return factory;
    }

    public BeforeMethodSent getBeforeMethodSent() {
        if (beforeMethodSent == null) {
            beforeMethodSent = new BeforeMethodSent() {
            };
        }
        return beforeMethodSent;
    }

    public ServiceBundleBuilder setBeforeMethodSent(BeforeMethodSent beforeMethodSent) {
        this.beforeMethodSent = beforeMethodSent;
        return this;
    }

    public CallbackManagerBuilder getCallbackManagerBuilder() {
        if (callbackManagerBuilder == null) {
            callbackManagerBuilder = CallbackManagerBuilder.callbackManagerBuilder();
            if (address != null) {
                callbackManagerBuilder.setName("ServiceBundle-" + address);
            }
        }
        return callbackManagerBuilder;
    }

    public ServiceBundleBuilder setCallbackManagerBuilder(CallbackManagerBuilder callbackManagerBuilder) {
        this.callbackManagerBuilder = callbackManagerBuilder;
        return this;
    }

    public CallbackManager getCallbackManager() {
        if (callbackManager == null) {

            callbackManager = this.getCallbackManagerBuilder().build();
        }
        return callbackManager;
    }

    public ServiceBundleBuilder setCallbackManager(CallbackManager callbackManager) {
        this.callbackManager = callbackManager;
        return this;
    }

    public QueueBuilder getWebResponseQueueBuilder() {

        if (webResponseQueueBuilder == null) {
            webResponseQueueBuilder = new QueueBuilder();
        }
        return webResponseQueueBuilder;
    }

    public ServiceBundleBuilder setWebResponseQueueBuilder(QueueBuilder webResponseQueueBuilder) {
        this.webResponseQueueBuilder = webResponseQueueBuilder;
        return this;
    }

    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public ServiceBundleBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }

    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public ServiceBundleBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }

    public boolean isEachServiceInItsOwnThread() {
        return eachServiceInItsOwnThread;
    }

    public ServiceBundleBuilder setEachServiceInItsOwnThread(boolean eachServiceInItsOwnThread) {
        this.eachServiceInItsOwnThread = eachServiceInItsOwnThread;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public ServiceBundleBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;

    }

    public ServiceBundleBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;

    }

    public Transformer<Request, Object> getArgTransformer() {
        return argTransformer;

    }

    public ServiceBundleBuilder setArgTransformer(Transformer<Request, Object> argTransformer) {
        this.argTransformer = argTransformer;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ServiceBundleBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public QueueBuilder getRequestQueueBuilder() {

        if (requestQueueBuilder == null) {
            requestQueueBuilder = QueueBuilder.queueBuilder();
        }
        return requestQueueBuilder;
    }

    public ServiceBundleBuilder setRequestQueueBuilder(QueueBuilder queueBuilder) {
        this.requestQueueBuilder = queueBuilder;
        return this;
    }

    public QueueBuilder getResponseQueueBuilder() {

        if (responseQueueBuilder == null) {

            if (responseQueue != null) {

                responseQueueBuilder = new QueueBuilder() {

                    @Override
                    public <T> Queue<T> build() {
                        //noinspection unchecked
                        return (Queue<T>) responseQueue;
                    }
                };

            } else {

                responseQueueBuilder = QueueBuilder.queueBuilder();
            }

        }
        return responseQueueBuilder;
    }

    public ServiceBundleBuilder setResponseQueueBuilder(QueueBuilder queueBuilder) {
        this.responseQueueBuilder = queueBuilder;
        return this;
    }

    public Queue<Response<Object>> getResponseQueue() {
        return responseQueue;
    }

    public ServiceBundleBuilder setResponseQueue(final Queue<Response<Object>> responseQueue) {
        this.responseQueue = responseQueue;
        return this;
    }


    public HealthServiceAsync getHealthService() {
        return healthService;
    }

    public ServiceBundleBuilder setHealthService(HealthServiceAsync healthServiceAsync) {
        this.healthService = healthServiceAsync;
        return this;
    }

    public StatsCollector getStatsCollector() {
        return statsCollector;
    }

    public ServiceBundleBuilder setStatsCollector(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        return this;
    }


    public ServiceBundle build() {


        final ServiceBundle serviceBundle = getFactory().createServiceBundle(this.getAddress(),
                getRequestQueueBuilder(),
                getResponseQueueBuilder(),
                getWebResponseQueueBuilder(),
                getFactory(),
                eachServiceInItsOwnThread,
                this.getBeforeMethodCall(),
                this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer(),
                invokeDynamic,
                this.getSystemManager(),
                getHealthService(),
                getStatsCollector(),
                getTimer(),
                getStatsFlushRateSeconds(),
                getCheckTimingEveryXCalls(),
                getCallbackManager(),
                getEventManager(),
                getBeforeMethodSent(),
                getBeforeMethodCallOnServiceQueue(),
                getAfterMethodCallOnServiceQueue());


        if (serviceBundle != null && qBitSystemManager != null) {
            qBitSystemManager.registerServiceBundle(serviceBundle);
        }

        if (serviceBundle == null) {
            throw new IllegalStateException("Service Bundle was null");
        }

        return serviceBundle;


    }

    public ServiceBundle buildAndStart() {
        final ServiceBundle build = build();
        build.startUpCallQueue();
        return build;
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public ServiceBundleBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public int getStatsFlushRateSeconds() {
        return statsFlushRateSeconds;
    }

    public ServiceBundleBuilder setStatsFlushRateSeconds(int statsFlushRateSeconds) {
        this.statsFlushRateSeconds = statsFlushRateSeconds;
        return this;
    }

    public int getCheckTimingEveryXCalls() {
        return checkTimingEveryXCalls;
    }

    public ServiceBundleBuilder setCheckTimingEveryXCalls(int checkTimingEveryXCalls) {
        this.checkTimingEveryXCalls = checkTimingEveryXCalls;
        return this;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ServiceBundleBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallOnServiceQueue() {
        return beforeMethodCallOnServiceQueue;
    }

    public ServiceBundleBuilder setBeforeMethodCallOnServiceQueue(BeforeMethodCall beforeMethodCallOnServiceQueue) {
        this.beforeMethodCallOnServiceQueue = beforeMethodCallOnServiceQueue;
        return this;
    }

    public AfterMethodCall getAfterMethodCallOnServiceQueue() {
        return afterMethodCallOnServiceQueue;
    }

    public ServiceBundleBuilder setAfterMethodCallOnServiceQueue(AfterMethodCall afterMethodCallOnServiceQueue) {
        this.afterMethodCallOnServiceQueue = afterMethodCallOnServiceQueue;
        return this;
    }
}


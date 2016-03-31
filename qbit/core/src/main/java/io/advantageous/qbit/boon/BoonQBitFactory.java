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

package io.advantageous.qbit.boon;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.boon.events.impl.BoonEventBusProxyCreator;
import io.advantageous.qbit.boon.service.impl.BoonServiceMethodCallHandler;
import io.advantageous.qbit.boon.service.impl.BoonServiceProxyFactory;
import io.advantageous.qbit.boon.spi.BoonJsonMapper;
import io.advantageous.qbit.boon.spi.BoonProtocolEncoder;
import io.advantageous.qbit.boon.spi.BoonProtocolParser;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.sender.Sender;
import io.advantageous.qbit.sender.SenderEndPoint;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.impl.CallbackManager;
import io.advantageous.qbit.service.impl.ServiceBundleImpl;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;


/**
 * created by Richard on 9/26/14.
 *
 * @author rhightower
 *         This factory uses Boon reflection and JSON support.
 *         The Factory is a facade over other factories providing a convienient unified interface to QBIT.
 */
public class BoonQBitFactory implements Factory {

    private final Logger logger = LoggerFactory.getLogger(BoonQBitFactory.class);
    private final AtomicReference<ServiceQueue> systemEventManager = new AtomicReference<>();
    private final ThreadLocal<EventManager> eventManagerThreadLocal = new ThreadLocal<>();
    private final ProtocolParser defaultProtocol = new BoonProtocolParser();
    private final ServiceProxyFactory serviceProxyFactory = new BoonServiceProxyFactory(this);
    private final ServiceProxyFactory remoteServiceProxyFactory = new BoonServiceProxyFactory(this);
    private final ThreadLocal<List<ProtocolParser>> protocolParserListRef = new ThreadLocal<List<ProtocolParser>>() {

        @Override
        protected List<ProtocolParser> initialValue() {
            ArrayList<ProtocolParser> list = new ArrayList<>();
            list.add(createProtocolParser());
            return list;
        }
    };


    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4,
            r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("PeriodicTasks");
                return thread;
            });

    public PeriodicScheduler periodicScheduler() {

        return (runnable, interval, timeUnit) -> scheduledExecutorService.scheduleAtFixedRate(runnable, interval, interval, timeUnit);
    }

    public PeriodicScheduler createPeriodicScheduler(int poolSize) {


        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(poolSize,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("PeriodicTasks");
                    return thread;
                });

        return new PeriodicScheduler() {
            @Override
            public ScheduledFuture repeat(Runnable runnable, int interval, TimeUnit timeUnit) {
                return scheduledExecutorService.scheduleAtFixedRate(runnable, interval, interval, timeUnit);
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
                scheduledExecutorService.shutdown();
            }
        };

    }

    @Override
    public EventManager systemEventManager() {

        final EventManager eventManager = eventManagerThreadLocal.get();
        if (eventManager != null) {
            return eventManager;
        }

        EventManager proxy;
        if (systemEventManager.get() == null) {
            final ServiceQueue serviceQueue = serviceBuilder().setInvokeDynamic(false)
                    .setServiceObject(
                            EventManagerBuilder.eventManagerBuilder().setName("QBIT_SYSTEM").build()).build().startServiceQueue();

            systemEventManager.set(serviceQueue);
            proxy = serviceQueue.createProxyWithAutoFlush(EventManager.class, 100, TimeUnit.MILLISECONDS);
        } else {
            proxy = systemEventManager.get().createProxyWithAutoFlush(EventManager.class, 100, TimeUnit.MILLISECONDS);
        }

        eventManagerThreadLocal.set(proxy);
        return proxy;
    }

    @Override
    public void shutdownSystemEventBus() {
        final ServiceQueue serviceQueue = systemEventManager.get();
        if (serviceQueue != null) {
            serviceQueue.stop();
        }
    }

    public EventManager eventManagerProxy() {
        return eventManagerThreadLocal.get();
    }

    public void clearEventManagerProxy() {
        eventManagerThreadLocal.set(null);
    }


    @Override
    public <T> T createLocalProxy(Class<T> serviceInterface, String serviceName, ServiceBundle serviceBundle, BeforeMethodSent beforeMethodSent) {

        return this.serviceProxyFactory.createProxy(serviceInterface, serviceName, serviceBundle, beforeMethodSent);
    }


    @Override
    public <T> T createRemoteProxyWithReturnAddress(final Class<T> serviceInterface,
                                                    final String address,
                                                    final String serviceName,
                                                    final String host,
                                                    final int port,
                                                    final AtomicBoolean connected,
                                                    final String returnAddressArg,
                                                    final Sender<String> sender,
                                                    final BeforeMethodCall beforeMethodCall,
                                                    final int requestBatchSize,
                                                    final BeforeMethodSent beforeMethodSent) {
        return remoteServiceProxyFactory.createProxyWithReturnAddress(
                serviceInterface,
                serviceName,
                host, port, connected,
                returnAddressArg, new SenderEndPoint(this.createEncoder(), address, sender, beforeMethodCall,
                        requestBatchSize), beforeMethodSent);
    }


    @Override
    public MethodCall<Object> createMethodCallFromHttpRequest(final Request<Object> request, Object args) {

        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setOriginatingRequest(request);
        methodCallBuilder.setBody(args);
        methodCallBuilder.setHeaders(request.headers());
        methodCallBuilder.setParams(request.params());
        methodCallBuilder.setAddress(request.address());
        methodCallBuilder.overridesFromParams();
        return methodCallBuilder.build();

    }

    @Override
    public JsonMapper createJsonMapper() {
        return new BoonJsonMapper();
    }


    @Override
    public Client createClient(final String uri,
                               final HttpClient httpClient,
                               final int requestBatchSize,
                               final BeforeMethodSent beforeMethodSent) {
        return FactorySPI.getClientFactory().create(uri, httpClient, requestBatchSize, beforeMethodSent);
    }

    @Override
    public ProtocolParser createProtocolParser() {
        return new BoonProtocolParser();
    }


    @Override
    public MethodCall<Object> createMethodCallToBeParsedFromBody(String address,
                                                                 String returnAddress,
                                                                 String objectName,
                                                                 String methodName,
                                                                 Object body, MultiMap<String, String> params) {

        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setName(methodName);
        methodCallBuilder.setBody(body);
        methodCallBuilder.setObjectName(objectName);
        methodCallBuilder.setAddress(address);
        methodCallBuilder.setReturnAddress(returnAddress);
        if (params != null) {
            methodCallBuilder.setParams(params);
        }
        methodCallBuilder.overridesFromParams();
        return methodCallBuilder.build();
    }

    @Override
    public MethodCall<Object> createMethodCallByNames(String methodName, String objectName, String returnAddress, Object args, MultiMap<String, String> params) {
        return createMethodCallToBeParsedFromBody("", returnAddress, objectName, methodName, args, params);
    }


    @Override
    public ServiceBundle createServiceBundle(final String address,
                                             final QueueBuilder requestQueueBuilder,
                                             final QueueBuilder responseQueueBuilder,
                                             final QueueBuilder webResponseQueueBuilder,
                                             final Factory factory,
                                             final boolean asyncCalls,
                                             final BeforeMethodCall beforeMethodCall,
                                             final BeforeMethodCall beforeMethodCallAfterTransform,
                                             final Transformer<Request, Object> argTransformer,
                                             final boolean invokeDynamic,
                                             final QBitSystemManager systemManager,
                                             final HealthServiceAsync healthService,
                                             final StatsCollector statsCollector,
                                             final Timer timer,
                                             final int statsFlushRateSeconds,
                                             final int checkTimingEveryXCalls,
                                             final CallbackManager callbackManager,
                                             final EventManager eventManager,
                                             final BeforeMethodSent beforeMethodSent,
                                             final BeforeMethodCall beforeMethodCallOnServiceQueue,
                                             final AfterMethodCall afterMethodCallOnServiceQueue) {
        return new ServiceBundleImpl(address, requestQueueBuilder, responseQueueBuilder,
                webResponseQueueBuilder,
                factory, asyncCalls, beforeMethodCall, beforeMethodCallAfterTransform,
                argTransformer, invokeDynamic, systemManager, healthService, statsCollector, timer,
                statsFlushRateSeconds, checkTimingEveryXCalls, callbackManager,
                eventManager, beforeMethodSent, beforeMethodCallOnServiceQueue,
                afterMethodCallOnServiceQueue);
    }


    @Override
    public ServiceMethodHandler createServiceMethodHandler(boolean invokeDynamic) {

        return new BoonServiceMethodCallHandler(invokeDynamic);
    }


    @Override
    public ProtocolEncoder createEncoder() {
        return new BoonProtocolEncoder();
    }


    public EventBusProxyCreator eventBusProxyCreator() {

        return new BoonEventBusProxyCreator();
    }

}

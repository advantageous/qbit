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

package io.advantageous.qbit.service.impl;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.ConcurrentHashSet;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * Manages a collection of services.
 */
public class ServiceBundleImpl implements ServiceBundle {


    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ServiceBundleImpl.class);
    private final boolean debug = GlobalConstants.DEBUG && logger.isDebugEnabled();
    private final boolean asyncCalls;
    private final boolean invokeDynamic;
    private final QBitSystemManager systemManager;
    private final CallbackManager callbackManager;
    /**
     * Keep track of servicesToStop to forwardEvent queue mappings.
     *///SendQueue<MethodCall<Object>>
    private final Map<String, Consumer<MethodCall<Object>>> serviceMapping = new ConcurrentHashMap<>();
    /**
     * Keep a list of current servicesToStop that we are routing to.
     */
    private final Set<Stoppable> servicesToStop = new ConcurrentHashSet<>(10);

    private final Set<ServiceFlushable> servicesToFlush = new ConcurrentHashSet<>(10);
    /**
     * Keep a list of current forwardEvent queue.
     */
    private final Set<SendQueue<MethodCall<Object>>> sendQueues = new ConcurrentHashSet<>(10);
    /**
     * Method queue for receiving method calls.
     */
    private final Queue<MethodCall<Object>> methodQueue;
    /**
     *
     */
    private final SendQueue<MethodCall<Object>> methodSendQueue;
    /**
     * Response queue for returning responses from servicesToStop that we invoked.
     */
    private final Queue<Response<Object>> responseQueue;

    /**
     * Response queue for returning responses from servicesToStop that we invoked.
     */
    private final Queue<Response<Object>> webResponseQueue;

    /**
     * Base URI for servicesToStop that this bundle is managing.
     */
    private final String rootAddress;
    /**
     * Access to QBit factory.
     */
    private final Factory factory;


    /**
     * Allows interception of method calls before they get encoded by the client proxy.
     * This allows us to transform or reject method calls.
     */
    private final BeforeMethodSent beforeMethodSent;


    /**
     * Allows interception of method calls before they get sent to a client.
     * This allows us to transform or reject method calls.
     */
    private final BeforeMethodCall beforeMethodCall;
    /**
     * Allows interception of method calls before they get transformed and sent to a client.
     * This allows us to transform or reject method calls.
     */
    private final BeforeMethodCall beforeMethodCallAfterTransform;
    /**
     * Allows transformation of arguments, for example from JSON to Java objects.
     */
    private final Transformer<Request, Object> argTransformer;

    /*
     */
    private final QueueBuilder requestQueueBuilder;
    private final HealthServiceAsync healthService;
    private final StatsCollector statsCollector;
    private final Timer timer;
    private final int sampleStatFlushRate;
    private final int checkTimingEveryXCalls;
    private final EventManager eventManager;
    private final BeforeMethodCall beforeMethodCallOnServiceQueue;
    private final AfterMethodCall afterMethodCallOnServiceQueue;

    public ServiceBundleImpl(final String address,
                             final QueueBuilder requestQueueBuilder,
                             final QueueBuilder responseQueueBuilder,
                             final QueueBuilder webResponseQueueBuilder,
                             final Factory factory, final boolean asyncCalls,
                             final BeforeMethodCall beforeMethodCall,
                             final BeforeMethodCall beforeMethodCallAfterTransform,
                             final Transformer<Request, Object> argTransformer,
                             final boolean invokeDynamic,
                             final QBitSystemManager systemManager,
                             final HealthServiceAsync healthService,
                             final StatsCollector statsCollector,
                             final Timer timer,
                             final int sampleStatFlushRate,
                             final int checkTimingEveryXCalls,
                             final CallbackManager callbackManager,
                             final EventManager eventManager,
                             final BeforeMethodSent beforeMethodSent,
                             final BeforeMethodCall beforeMethodCallOnServiceQueue,
                             final AfterMethodCall afterMethodCallOnServiceQueue) {

        this.beforeMethodCallOnServiceQueue = beforeMethodCallOnServiceQueue;
        this.afterMethodCallOnServiceQueue = afterMethodCallOnServiceQueue;

        this.healthService = healthService;
        this.statsCollector = statsCollector;
        this.invokeDynamic = invokeDynamic;
        this.systemManager = systemManager;
        this.timer = timer;
        this.sampleStatFlushRate = sampleStatFlushRate;
        this.checkTimingEveryXCalls = checkTimingEveryXCalls;
        this.callbackManager = callbackManager;
        this.beforeMethodSent = beforeMethodSent;

        String rootAddress;
        if (address.endsWith("/")) {
            rootAddress = address.substring(0, address.length() - 1);
        } else {
            rootAddress = address;
        }
        this.beforeMethodCall = beforeMethodCall;
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        this.argTransformer = argTransformer;
        this.rootAddress = rootAddress;
        this.factory = factory;
        this.asyncCalls = asyncCalls;
        this.requestQueueBuilder = requestQueueBuilder;
        this.methodQueue = requestQueueBuilder.setName("Call Queue " + address).build();
        this.responseQueue = responseQueueBuilder.setName("Response Queue " + address).build();
        this.webResponseQueue = webResponseQueueBuilder.setName("Web Response Queue " + address).build();
        this.methodSendQueue = methodQueue.sendQueueWithAutoFlush(10, TimeUnit.SECONDS);
        this.eventManager = eventManager;
    }


    /**
     * Handles calling a method
     *
     * @param methodCall method call
     */
    private void doCall(MethodCall<Object> methodCall) {
        if (debug) {
            logger.debug(ServiceBundleImpl.class.getName(), "::doCall() ",
                    methodCall.name(),
                    methodCall.address(),
                    "\n", methodCall);
        }

        try {
            if (methodCall.hasCallback()) {
                callbackManager.registerCallbacks(methodCall);
            }
            boolean[] continueFlag = new boolean[1];
            methodCall = handleBeforeMethodCall(methodCall, continueFlag);

            if (!continueFlag[0]) {
                if (debug) {
                    logger.debug(ServiceBundleImpl.class.getName() + "::doCall() " +
                            "Flag from before call handling does not want to continue");
                }
            } else {
                final Consumer<MethodCall<Object>> methodDispatcher = getMethodDispatcher(methodCall);
                methodDispatcher.accept(methodCall);

            }

        } catch (Exception ex) {
            Response<Object> response = new ResponseImpl<>(methodCall, ex);
            this.responseQueue.sendQueue().sendAndFlush(response);
        }
    }

    /**
     * Base URI for all of the servicesToStop in this bundle.
     *
     * @return base URI.
     */
    @Override
    public String address() {
        return rootAddress;
    }

    /**
     * Add a client to this bundle.
     *
     * @param object the client we want to add.
     */
    @Override
    public ServiceBundle addService(Object object) {
        if (debug) {
            logger.debug("ServiceBundleImpl::addServiceObject(object)- service added");
        }
        addServiceObject(null, object);
        return this;
    }

    /**
     * Add a client to this bundle, under a certain address.
     *
     * @param serviceAddress the address of the client
     * @param serviceObject  the client we want to add.
     */
    @Override
    public ServiceBundle addServiceObject(final String serviceAddress, final Object serviceObject) {

        logger.info(ServiceBundleImpl.class.getName() + " serviceAddress " + serviceAddress + " service object " + serviceObject);

        if (serviceObject instanceof Consumer) {

            //noinspection unchecked
            addServiceConsumer(serviceAddress, (Consumer<MethodCall<Object>>) serviceObject);
            return this;
        }


        if (serviceObject instanceof ServiceQueue) {
            addServiceService(serviceAddress, (ServiceQueue) serviceObject);
            return this;
        }

        /** Turn this client object into a client with queues. */
        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder()
                .setRootAddress(rootAddress)
                .setServiceObject(serviceObject)
                .setServiceAddress(serviceAddress)
                .setTimer(timer)
                .setResponseQueue(responseQueue)
                .setAsyncResponse(asyncCalls)
                .setInvokeDynamic(invokeDynamic)
                .setSystemManager(systemManager)
                .setRequestQueueBuilder(BeanUtils.copy(this.requestQueueBuilder))
                .setRequestQueueBuilder(requestQueueBuilder)
                .setHandleCallbacks(false)
                .setCreateCallbackHandler(false)
                .setEventManager(eventManager)
                .setBeforeMethodCall(this.beforeMethodCallOnServiceQueue)
                .setAfterMethodCall(this.afterMethodCallOnServiceQueue);


        final String bindStatHealthName = serviceAddress == null
                ? AnnotationUtils.readServiceName(serviceObject)
                : serviceAddress;

        if (healthService != null) {
            serviceBuilder.registerHealthChecks(healthService, bindStatHealthName);
        }


        if (statsCollector != null) {
            /*
              The default is to flush stats every five seconds, and sample
              every 10_000 queue calls.
             */
            serviceBuilder.registerStatsCollections(bindStatHealthName,
                    statsCollector, sampleStatFlushRate, checkTimingEveryXCalls);
        }


        final ServiceQueue serviceQueue = serviceBuilder.buildAndStart();

        addServiceService(serviceAddress, serviceQueue);
        return this;
    }

    public ServiceBundle addServiceConsumer(final String serviceAddress,
                                            final Consumer<MethodCall<Object>> methodCallConsumer) {

        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("UnnecessaryLocalVariable") String address = serviceAddress;

        if (address != null && !address.isEmpty()) {
            serviceMapping.put(address, methodCallConsumer);
            serviceMapping.put(address.toLowerCase(), methodCallConsumer);

            if (methodCallConsumer instanceof ServiceFlushable) {
                this.servicesToFlush.add((ServiceFlushable) methodCallConsumer);
            }

            if (methodCallConsumer instanceof Stoppable) {
                this.servicesToStop.add((Stoppable) methodCallConsumer);
            }
        } else {
            throw new IllegalStateException("Service consumer must have an address");//TODO lookup name like you do in BoonMethodCallHandler
        }
        return this;

    }

    public void addServiceService(final String serviceAddress, final ServiceQueue serviceQueue) {


        addServiceService(null, serviceAddress, serviceQueue);
    }

    public void addServiceService(final String objectName, final String serviceAddress, final ServiceQueue serviceQueue) {


        /** add to our list of servicesToStop. */
        servicesToStop.add(serviceQueue);
        servicesToFlush.add(serviceQueue);


        QueueDispatch dispatch = new QueueDispatch(serviceQueue);

        /** Add the client given the address if we have an address. */
        if (serviceAddress != null && !serviceAddress.isEmpty()) {
            serviceMapping.put(serviceAddress, dispatch);
        }

        if (objectName != null) {
            /** Put the client incoming requests in our client name, request queue mapping. */
            serviceMapping.put(objectName, dispatch);
        }


        serviceMapping.put(serviceQueue.name().toLowerCase(), dispatch);
        serviceMapping.put(serviceQueue.name(), dispatch);
        serviceMapping.put(serviceQueue.address(), dispatch);
        serviceMapping.put(serviceQueue.address().toLowerCase(), dispatch);

        /** Add the request queue to our set of request queues. */
        sendQueues.add(dispatch.requests);

        /** Generate a list of end point addresses based on the client bundle root address. */
        final Collection<String> addresses = serviceQueue.addresses(this.rootAddress);

        if (debug) {
            logger.debug(ServiceBundleImpl.class.getName() + " addresses: " + addresses);
        }

        /** Add mappings to all addresses for this client to our serviceMapping. */
        for (String addr : addresses) {
            serviceMapping.put(addr, dispatch);
        }
    }

    /**
     * Returns a receive queue for all servicesToStop managed by this bundle.
     *
     * @return responses queue
     */
    @Override
    public Queue<Response<Object>> responses() {
        return responseQueue;
    }

    @Override
    public SendQueue<MethodCall<Object>> methodSendQueue() {
        return methodQueue.sendQueue();
    }

    /**
     * Call the method.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void call(final MethodCall<Object> methodCall) {

        if (debug) {
            logger.debug(ServiceBundleImpl.class.getName() + "::call() " +
                    methodCall.name() + " " + " " +
                    methodCall.address() +
                    "\n" + methodCall);
        }
        methodSendQueue.send(methodCall);
    }

    @Override
    public void call(List<MethodCall<Object>> methodCalls) {
        if (debug) {
            logger.debug("ServiceBundleImpl::call()- methodCalls: \n" + methodCalls);
        }
        methodSendQueue.sendBatch(methodCalls);
    }

    /**
     * Creates a proxy interface to a particular client. Given a particular address.
     *
     * @param serviceInterface client view interface of client
     * @param myService        address or name of client
     * @param <T>              type of client
     * @return proxy client to client
     */
    @Override
    public <T> T createLocalProxy(final Class<T> serviceInterface, final String myService) {


        final Consumer<MethodCall<Object>> callConsumer = this.serviceMapping.get(myService);

        if (callConsumer == null) {
            logger.error("Service requested does not exist " + myService);
        }

        return factory.createLocalProxy(serviceInterface, myService, this, beforeMethodSent);

    }

    public <T> T createOneWayLocalProxy(final Class<T> serviceInterface, final String myService) {


        final Consumer<MethodCall<Object>> callConsumer = this.serviceMapping.get(myService);

        if (callConsumer == null) {
            throw new IllegalStateException("Service requested does not exist " + myService);
        }

        if (callConsumer instanceof QueueDispatch) {

            return ((QueueDispatch) callConsumer).serviceQueue.createProxyWithAutoFlush(
                    serviceInterface, 100, TimeUnit.MILLISECONDS);
        } else {
            return factory.createLocalProxy(serviceInterface, myService, this, beforeMethodSent);
        }
    }


    private MethodCall<Object> handleBeforeMethodCall(MethodCall<Object> methodCall, boolean[] continueFlag) {
        methodCall = beforeMethodCall(methodCall, continueFlag);

        return methodCall;
    }

    private Consumer<MethodCall<Object>> getMethodDispatcher(MethodCall<Object> methodCall) {
        Consumer<MethodCall<Object>> methodCallConsumer = null;

        boolean hasAddress = !Str.isEmpty(methodCall.address());

        boolean hasMethodName = !Str.isEmpty(methodCall.name());

        boolean hasObjectName = !Str.isEmpty(methodCall.objectName());

        if (hasMethodName && hasObjectName) {
            methodCallConsumer = serviceMapping.get(methodCall.objectName());
        }

        if (hasAddress && methodCallConsumer == null) {
            methodCallConsumer = getMethodDispatchByAddress(methodCall);
        }

        if (methodCallConsumer == null) {
            logger.error("No service at method address " + methodCall.address()
                    + " method name " + methodCall.name() + " object name " + methodCall.objectName() + "\n SERVICES" + serviceMapping.keySet() + "\n");

            Set<String> uris = serviceMapping.keySet();

            //noinspection CodeBlock2Expr
            uris.forEach((String it) -> {
                logger.error("known URI path " + it);
            });

            throw new ServiceMethodNotFoundException("there is no object at this address: " + methodCall.address()
                    + "\n method name=" + methodCall.name() + "\n objectName=" + methodCall.objectName(), methodCall.address());
        }
        return methodCallConsumer;
    }

    private Consumer<MethodCall<Object>> getMethodDispatchByAddress(final MethodCall<Object> methodCall) {
        Consumer<MethodCall<Object>> methodConsumerByAddress;
        final String callAddress = methodCall.address();
        methodConsumerByAddress = serviceMapping.get(callAddress);
        return methodConsumerByAddress;
    }

    /**
     * Handles before call operation
     *
     * @param methodCall   method call
     * @param continueCall should we continue the call.
     * @return call object which could have been transformed
     */
    private MethodCall<Object> beforeMethodCall(MethodCall<Object> methodCall, boolean[] continueCall) {
        if (this.beforeMethodCall.before(methodCall)) {
            continueCall[0] = true;
            methodCall = transformBeforeMethodCall(methodCall);

            continueCall[0] = this.beforeMethodCallAfterTransform.before(methodCall);
            return methodCall;

        } else {
            continueCall[0] = false;

        }
        return methodCall;
    }

    /**
     * Handles the before argument transformer.
     *
     * @param methodCall method call that we might transform
     * @return method call
     */
    private MethodCall<Object> transformBeforeMethodCall(MethodCall<Object> methodCall) {
        if (argTransformer == null || argTransformer == ServiceConstants.NO_OP_ARG_TRANSFORM) {
            return methodCall;
        }
        Object arg = this.argTransformer.transform(methodCall);
        return MethodCallBuilder.transformed(methodCall, arg);
    }

    /**
     * Flush the sends.
     */
    @Override
    public void flushSends() {

        this.methodSendQueue.flushSends();
    }

    /**
     * Stop the client bundle.
     */
    @SuppressWarnings("Convert2streamapi")
    public void stop() {
        if (debug) {
            logger.debug(ServiceBundleImpl.class.getName(), "::stop()");
        }
        methodQueue.stop();
        for (Stoppable service : servicesToStop) {
            service.stop();
        }

        try {
            responseQueue.stop();
        } catch (Exception ex) {
            logger.debug("", ex);
        }

        try {
            webResponseQueue.stop();
        } catch (Exception ex) {
            logger.debug("", ex);
        }
        if (systemManager != null) systemManager.serviceShutDown();
    }

    /**
     * Return a list of end points that we are handling.
     */
    @Override
    public List<String> endPoints() {
        return new ArrayList<>(serviceMapping.keySet());
    }

    public void startReturnHandlerProcessor(ReceiveQueueListener<Response<Object>> listener) {

        responseQueue.startListener(listener);

    }

    public void startWebResponseReturnHandler(ReceiveQueueListener<Response<Object>> listener) {

        webResponseQueue.startListener(listener);

    }

    /**
     * Handles responses coming back from services.
     */
    public void startReturnHandlerProcessor() {

        final SendQueue<Response<Object>> webResponseSendQueue = webResponseQueue.sendQueue();

        responseQueue.startListener(new ReceiveQueueListener<Response<Object>>() {

            @Override
            public void receive(Response<Object> response) {


                final Request<Object> originatingRequest = response.request().originatingRequest();

                if (originatingRequest == null) {
                    callbackManager.handleResponse(response);
                } else if (originatingRequest instanceof HttpRequest || originatingRequest instanceof WebSocketMessage) {
                    webResponseSendQueue.send(response);
                } else {
                    callbackManager.handleResponse(response);
                }
            }

            @Override
            public void empty() {
                webResponseSendQueue.flushSends();

            }

            @Override
            public void limit() {
                webResponseSendQueue.flushSends();

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {
                webResponseSendQueue.flushSends();

            }

            @Override
            public void startBatch() {

            }
        });
    }

    /**
     * Start the client bundle.
     */
    public ServiceBundle startUpCallQueue() {
        methodQueue.startListener(new ReceiveQueueListener<MethodCall<Object>>() {

            long time;

            long lastTimeAutoFlush;

            /**
             * When we receive a method call, we call doCall.
             * @param item item
             */
            @Override
            public void receive(MethodCall<Object> item) {
                doCall(item); //Do call calls forwardEvent but does not flush. Only when the queue is empty do we flush.
            }

            /**
             * If the queue is empty, then go ahead, and flush to each client all incoming requests every 50 milliseconds.
             */
            @Override
            public void empty() {
                time = timer.now();
                if (time > (lastTimeAutoFlush + 50)) {

                    //noinspection Convert2streamapi
                    for (SendQueue<MethodCall<Object>> sendQueue : sendQueues) {
                        sendQueue.flushSends();
                    }
                    lastTimeAutoFlush = time;
                }
            }

        });

        return this;
    }

    @SuppressWarnings("Convert2streamapi")
    public void flush() {

        flushSends();
        for (ServiceFlushable service : servicesToFlush) {
            service.flush();
        }

    }

    public ServiceBundle addServiceQueue(String objectName, ServiceQueue serviceQueue) {


        final String objectAddress = objectName.startsWith("/") ? this.address() + objectName :
                Str.add(this.address(), "/", objectName);

        addServiceService(objectName, objectAddress, serviceQueue);
        return this;
    }

    static class QueueDispatch implements Consumer<MethodCall<Object>> {

        final ServiceQueue serviceQueue;
        final SendQueue<MethodCall<Object>> requests;

        QueueDispatch(ServiceQueue serviceQueue) {
            this.serviceQueue = serviceQueue;
            /* Create an forwardEvent queue for this client. which we access from a single thread. */
            requests = serviceQueue.requests();


        }

        @Override
        public void accept(MethodCall<Object> objectMethodCall) {
            requests.send(objectMethodCall);
        }
    }

}

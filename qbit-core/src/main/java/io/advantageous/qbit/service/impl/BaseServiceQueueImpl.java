/*******************************************************************************
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
  *  __          __  _     _____            _        _
  *  \ \        / / | |   / ____|          | |      | |
  *   \ \  /\  / /__| |__| (___   ___   ___| | _____| |_
  *   \ \/  \/ / _ \ '_ \\___ \ / _ \ / __| |/ / _ \ __|
  *   \  /\  /  __/ |_) |___) | (_) | (__|   <  __/ |_
  *    \/  \/ \___|_.__/_____/ \___/ \___|_|\_\___|\__|
  *       _  _____  ____  _   _
  *      | |/ ____|/ __ \| \ | |
  *      | | (___ | |  | |  \| |
  *  _   | |\___ \| |  | | . ` |
  * | |__| |____) | |__| | |\  |
  * \____/|_____/ \____/|_|_\_|_
  * |  __ \|  ____|/ ____|__   __|
  * | |__) | |__  | (___    | |
  * |  _  /|  __|  \___ \   | |
  * | | \ \| |____ ____) |  | |
  * |_|  \_\______|_____/   |_|___                 _
  * |  \/  (_)              / ____|               (_)
  * | \  / |_  ___ _ __ ___| (___   ___ _ ____   ___  ___ ___
  * | |\/| | |/ __| '__/ _ \\___ \ / _ \ '__\ \ / / |/ __/ _ \
  * | |  | | | (__| | | (_) |___) |  __/ |   \ V /| | (_|  __/
  * |_|  |_|_|\___|_|  \___/_____/ \___|_|    \_/ |_|\___\___|
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
  ******************************************************************************/

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.*;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.NoOpResponseTransformer;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.boon.core.reflection.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static io.advantageous.qbit.QBit.factory;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;

/**
 * @author  rhightower on 2/18/15.
 */
public class BaseServiceQueueImpl implements ServiceQueue {
    private static ThreadLocal<ServiceQueue> serviceThreadLocal = new ThreadLocal<>();
    protected final QBitSystemManager systemManager;
    protected final Logger logger = LoggerFactory.getLogger(ServiceQueueImpl.class);
    protected final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    protected final Object service;
    protected final Queue<Response<Object>> responseQueue;
    protected final Queue<MethodCall<Object>> requestQueue;
    protected final Queue<Event<Object>> eventQueue;

    protected final QueueBuilder requestQueueBuilder;
    protected final QueueBuilder responseQueueBuilder;

    protected final boolean handleCallbacks;
    protected ReentrantLock responseLock = new ReentrantLock();
    protected volatile long lastResponseFlushTime = Timer.timer().now();
    protected ServiceMethodHandler serviceMethodHandler;
    protected SendQueue<Response<Object>> responseSendQueue;
    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    private AfterMethodCall afterMethodCall = new NoOpAfterMethodCall();
    private AfterMethodCall afterMethodCallAfterTransform = new NoOpAfterMethodCall();
    private ReceiveQueueListener<MethodCall<Object>> inputQueueListener = new NoOpInputMethodCallQueueListener();
    private Transformer<Request, Object> requestObjectTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;
    private Transformer<Response<Object>, Response> responseObjectTransformer = new NoOpResponseTransformer();
    private CallbackManager callbackManager;

    public BaseServiceQueueImpl(final String rootAddress,
                                final String serviceAddress,
                                final Object service,
                                final QueueBuilder requestQueueBuilder,
                                final QueueBuilder responseQueueBuilder,
                                final ServiceMethodHandler serviceMethodHandler,
                                final Queue<Response<Object>> responseQueue,
                                final boolean async,
                                final boolean handleCallbacks,
                                final QBitSystemManager systemManager
    ) {

        if (requestQueueBuilder == null) {
            this.requestQueueBuilder = new QueueBuilder();
        } else {
            this.requestQueueBuilder = BeanUtils.copy(requestQueueBuilder);
        }

        if (responseQueueBuilder == null) {
            this.responseQueueBuilder = new QueueBuilder();
        } else {
            this.responseQueueBuilder = BeanUtils.copy(responseQueueBuilder);
        }


        if (responseQueue == null) {
            if (debug) {
                puts("RESPONSE QUEUE WAS NULL CREATING ONE");
            }
            this.responseQueue = this.responseQueueBuilder.setName("Response Queue  " + serviceMethodHandler.address()).build();
        } else {
            this.responseQueue = responseQueue;
        }


        this.responseSendQueue = this.responseQueue.sendQueue();
        this.service = service;
        this.serviceMethodHandler = serviceMethodHandler;
        this.serviceMethodHandler.init(service, rootAddress, serviceAddress, responseSendQueue);
        this.eventQueue = this.requestQueueBuilder.setName("Event Queue" + serviceMethodHandler.address()).build();
        this.handleCallbacks = handleCallbacks;
        this.requestQueue = initRequestQueue(serviceMethodHandler, async);
        this.systemManager = systemManager;




    }

    public static ServiceQueue currentService() {
        return serviceThreadLocal.get();
    }

    @Override
    public ServiceQueue start() {

        start(serviceMethodHandler, true);
        return this;
    }

    public ServiceQueue start(boolean joinEventManager) {
        start(serviceMethodHandler, joinEventManager);
        return this;
    }

    protected Queue<MethodCall<Object>> initRequestQueue(final ServiceMethodHandler serviceMethodHandler, boolean async) {
        Queue<MethodCall<Object>> requestQueue;
        if (async) {
            requestQueue = this.requestQueueBuilder.setName("Send Queue  " + serviceMethodHandler.address()).build();
        } else {
            requestQueue = new Queue<MethodCall<Object>>() {
                @Override
                public ReceiveQueue<MethodCall<Object>> receiveQueue() {

                    return null;
                }

                @Override
                public SendQueue<MethodCall<Object>> sendQueue() {
                    return new SendQueue<MethodCall<Object>>() {
                        @Override
                        public void send(MethodCall<Object> item) {
                            doHandleMethodCall(item, serviceMethodHandler);
                        }

                        @Override
                        public void sendAndFlush(MethodCall<Object> item) {

                            doHandleMethodCall(item, serviceMethodHandler);
                        }

                        @Override
                        public void sendMany(MethodCall<Object>... items) {

                            for (MethodCall<Object> item : items) {

                                doHandleMethodCall(item, serviceMethodHandler);
                            }
                        }

                        @Override
                        public void sendBatch(Collection<MethodCall<Object>> items) {

                            for (MethodCall<Object> item : items) {

                                doHandleMethodCall(item, serviceMethodHandler);
                            }

                        }

                        @Override
                        public void sendBatch(Iterable<MethodCall<Object>> items) {

                            for (MethodCall<Object> item : items) {

                                doHandleMethodCall(item, serviceMethodHandler);
                            }

                        }

                        @Override
                        public boolean shouldBatch() {
                            return false;
                        }

                        @Override
                        public void flushSends() {

                        }
                    };
                }

                @Override
                public void startListener(ReceiveQueueListener<MethodCall<Object>> listener) {

                }

                @Override
                public void stop() {

                }
            };
        }
        return requestQueue;
    }

    public ServiceQueue startCallBackHandler() {
        if (!handleCallbacks) {
            callbackManager = new CallbackManager();
            callbackManager.startReturnHandlerProcessor(this.responseQueue);
            return this;
        } else {
            throw new IllegalStateException("Unable to handle callbacks in a new thread when handleCallbacks is set");
        }
    }

    public BaseServiceQueueImpl requestObjectTransformer(Transformer<Request, Object> requestObjectTransformer) {
        this.requestObjectTransformer = requestObjectTransformer;
        return this;
    }

    public BaseServiceQueueImpl responseObjectTransformer(Transformer<Response<Object>, Response> responseObjectTransformer) {
        this.responseObjectTransformer = responseObjectTransformer;
        return this;
    }

    /**
     * This method is where all of the action is.
     *
     * @param methodCall           methodCall
     * @param serviceMethodHandler handler
     */
    private void doHandleMethodCall(MethodCall<Object> methodCall,
                                    final ServiceMethodHandler serviceMethodHandler) {
        if (debug) {
            logger.debug("ServiceImpl::doHandleMethodCall() METHOD CALL" + methodCall);
        }
        if (callbackManager != null) {
            callbackManager.registerCallbacks(methodCall);
        }
        inputQueueListener.receive(methodCall);
        final boolean continueFlag[] = new boolean[1];
        methodCall = beforeMethodProcessing(methodCall, continueFlag);
        if (continueFlag[0]) {
            if (debug) puts("ServiceImpl::doHandleMethodCall() before handling stopped processing");
            return;
        }
        Response<Object> response = serviceMethodHandler.receiveMethodCall(methodCall);
        if (debug) {
            puts("ServiceImpl::receive() \nRESPONSE\n" + response + "\nFROM CALL\n" + methodCall + " name " + methodCall.name() + "\n\n");
        }
        if (response != ServiceConstants.VOID) {

            if (!afterMethodCall.after(methodCall, response)) {
                return;
            }
            response = responseObjectTransformer.transform(response);

            if (!afterMethodCallAfterTransform.after(methodCall, response)) {
                return;
            }
            responseLock.lock();
            try {
                responseSendQueue.send(response);
            } finally {
                responseLock.unlock();
            }

        }
    }

    private void start(final ServiceMethodHandler serviceMethodHandler,
                       final boolean joinEventManager) {

        final ReceiveQueue<Response<Object>> responseReceiveQueue =
                this.handleCallbacks ?
                        responseQueue.receiveQueue() : null;

        if (handleCallbacks) {
            this.callbackManager = new CallbackManager();
        }

        final ReceiveQueue<Event<Object>> eventReceiveQueue =
                eventQueue.receiveQueue();

        serviceThreadLocal.set(BaseServiceQueueImpl.this);

        if (!(service instanceof EventManager)) {
            if (joinEventManager) {
                serviceContext().joinEventManager();
            }
        }
        serviceMethodHandler.queueInit();
        flushEventManagerCalls();
        serviceThreadLocal.set(null);
        requestQueue.startListener(new ReceiveQueueListener<MethodCall<Object>>() {

            @Override
            public void receive(MethodCall<Object> methodCall) {
                doHandleMethodCall(methodCall, serviceMethodHandler);
            }

            @Override
            public void empty() {
                handle();
                inputQueueListener.empty();
                serviceMethodHandler.empty();
            }

            @Override
            public void startBatch() {
                inputQueueListener.startBatch();
                serviceMethodHandler.queueStartBatch();
            }

            @Override
            public void limit() {
                handle();
                inputQueueListener.limit();
                serviceMethodHandler.limit();
            }

            @Override
            public void shutdown() {
                handle();
                inputQueueListener.shutdown();
                serviceMethodHandler.shutdown();
            }

            @Override
            public void idle() {
                handle();
                if (inputQueueListener != null) {
                    inputQueueListener.idle();
                }
                serviceMethodHandler.idle();
            }

            /** Such a small method with so much responsibility. */
            public void handle() {
                manageResponseQueue();
                /* Handles the CallBacks if you have configured the service
                to handle its own callbacks.
                Callbacks can be handled in a separate thread or the same
                thread the manages the service.
                 */
                if (handleCallbacks) {
                    Response<Object> response = responseReceiveQueue.poll();
                    while (response != null) {
                        callbackManager.handleResponse(response);
                        response = responseReceiveQueue.poll();
                    }
                }
                /* Handles the event processing. */
                Event<Object> event = eventReceiveQueue.poll();
                while (event != null) {
                    serviceMethodHandler.handleEvent(event);
                    event = eventReceiveQueue.poll();
                }
                flushEventManagerCalls();
            }

        });
    }

    private void flushEventManagerCalls() {
        final EventManager eventManager = factory().eventManagerProxy();
        if (eventManager != null) {
            ServiceProxyUtils.flushServiceProxy(eventManager);
            factory().clearEventManagerProxy();
        }
    }

    private void manageResponseQueue() {
        long now = Timer.timer().now();
        if (now - lastResponseFlushTime > 50) {
            lastResponseFlushTime = now;
            responseLock.lock();
            try {
                responseSendQueue.flushSends();
            } finally {
                responseLock.unlock();
            }
        }
    }

    private MethodCall<Object> beforeMethodProcessing(MethodCall<Object> methodCall, boolean[] continueFlag) {
        if (!beforeMethodCall.before(methodCall)) {
            continueFlag[0] = false;
        }
        if (requestObjectTransformer != null && requestObjectTransformer != ServiceConstants.NO_OP_ARG_TRANSFORM) {
            final Object arg = requestObjectTransformer.transform(methodCall);
            methodCall = MethodCallBuilder.transformed(methodCall, arg);
        }
        if (beforeMethodCallAfterTransform != null && beforeMethodCallAfterTransform != ServiceConstants.NO_OP_BEFORE_METHOD_CALL) {
            if (!beforeMethodCallAfterTransform.before(methodCall)) {
                continueFlag[0] = false;
            }
        }
        return methodCall;
    }

    @Override
    public SendQueue<MethodCall<Object>> requests() {
        return requestQueue.sendQueue();
    }

    @Override
    public SendQueue<MethodCall<Object>> requestsWithAutoFlush(int flushInterval, TimeUnit timeUnit) {
        return requestQueue.sendQueueWithAutoFlush(flushInterval, timeUnit);
    }

    @Override
    public ReceiveQueue<Response<Object>> responses() {
        return responseQueue.receiveQueue();
    }

    @Override
    public String name() {
        return serviceMethodHandler.name();
    }

    @Override
    public String address() {
        return serviceMethodHandler.address();
    }

    @Override
    public void stop() {

        try {
            if (requestQueue != null) requestQueue.stop();
        } catch (Exception ex) {
            if (debug) logger.debug("Unable to stop request queue", ex);
        }


        try {
            if (responseQueue != null) responseQueue.stop();
        } catch (Exception ex) {
            if (debug) logger.debug("Unable to stop response queues", ex);
        }

        if (systemManager != null) this.systemManager.serviceShutDown();
    }

    @Override
    public Collection<String> addresses(String address) {
        return this.serviceMethodHandler.addresses();
    }

    @Override
    public void flush() {
        lastResponseFlushTime = 0;
        manageResponseQueue();
    }

    public Object service() {
        return service;
    }


    public <T> T createProxyWithAutoFlush(Class<T> serviceInterface, int interval, TimeUnit timeUnit) {

        final SendQueue<MethodCall<Object>> methodCallSendQueue = requestQueue.sendQueueWithAutoFlush(interval, timeUnit);
        methodCallSendQueue.start();
        return proxy(serviceInterface, methodCallSendQueue);

    }


    public <T> T createProxyWithAutoFlush(final Class<T> serviceInterface,
                                          final PeriodicScheduler periodicScheduler,
                                          final int interval, final TimeUnit timeUnit) {

        final SendQueue<MethodCall<Object>> methodCallSendQueue =
                requestQueue.sendQueueWithAutoFlush(periodicScheduler, interval, timeUnit);
        methodCallSendQueue.start();
        return proxy(serviceInterface, methodCallSendQueue);

    }

    public <T> T createProxy(Class<T> serviceInterface) {

        final SendQueue<MethodCall<Object>> methodCallSendQueue = requestQueue.sendQueue();
        return proxy(serviceInterface, methodCallSendQueue);
    }

    private <T> T proxy(Class<T> serviceInterface, final SendQueue<MethodCall<Object>> methodCallSendQueue) {

        final String uuid = UUID.randomUUID().toString();
        InvocationHandler invocationHandler = new InvocationHandler() {

            private long messageId = 0;
            private long timestamp = Timer.timer().now();
            private int times = 10;

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

                if (method.getName().equals("toString")) {

                    return sputs("PROXY OBJECT", address());
                }
                if (method.getName().equals("clientProxyFlush")) {
                    methodCallSendQueue.flushSends();
                    return null;
                }

                if (method.getName().equals("stop")) {
                    methodCallSendQueue.stop();
                    return null;
                }
                messageId++;
                times--;
                if (times == 0) {
                    timestamp = Timer.timer().now();
                    times = 10;
                } else {
                    timestamp++;
                }
                final MethodCallLocal call = new MethodCallLocal(method.getName(), uuid, timestamp, messageId, args);
                methodCallSendQueue.send(call);
                return null;
            }
        };
        final Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceInterface, ClientProxy.class}, invocationHandler
        );
        return (T) o;
    }

    @Override
    public SendQueue<Event<Object>> events() {
        return this.eventQueue.sendQueueWithAutoFlush(50, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "Service{" +
                "debug=" + debug +
                ", service=" + service.getClass().getSimpleName() +
                '}';
    }

    static class MethodCallLocal implements MethodCall<Object> {

        private final String name;
        private final long timestamp;
        private final Object[] arguments;

        private final String uuid;
        private final long messageId;

        public MethodCallLocal(String name, final String uuid, long timestamp, long messageId, Object[] args) {
            this.name = name;
            this.timestamp = timestamp;
            this.arguments = args;
            this.uuid = uuid;
            this.messageId = messageId;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String address() {
            return name;
        }

        @Override
        public String returnAddress() {
            return uuid;
        }

        @Override
        public MultiMap<String, String> params() {
            return null;
        }

        @Override
        public MultiMap<String, String> headers() {
            return null;
        }

        @Override
        public boolean hasParams() {
            return false;
        }

        @Override
        public boolean hasHeaders() {
            return false;
        }

        @Override
        public long timestamp() {
            return timestamp;
        }

        @Override
        public boolean isHandled() {
            return false;
        }

        @Override
        public void handled() {
        }

        @Override
        public String objectName() {
            return "";
        }

        @Override
        public Request<Object> originatingRequest() {
            return null;
        }

        @Override
        public long id() {
            return messageId;
        }

        @Override
        public Object body() {
            return arguments;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}

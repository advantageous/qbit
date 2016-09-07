/**
 * ****************************************************************************
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * __          __  _     _____            _        _
 * \ \        / / | |   / ____|          | |      | |
 * \ \  /\  / /__| |__| (___   ___   ___| | _____| |_
 * \ \/  \/ / _ \ '_ \\___ \ / _ \ / __| |/ / _ \ __|
 * \  /\  /  __/ |_) |___) | (_) | (__|   <  __/ |_
 * \/  \/ \___|_.__/_____/ \___/ \___|_|\_\___|\__|
 * _  _____  ____  _   _
 * | |/ ____|/ __ \| \ | |
 * | | (___ | |  | |  \| |
 * _   | |\___ \| |  | | . ` |
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
 * <p>
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 * http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 * http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 * http://rick-hightower.blogspot.com/2015/01/quick-startClient-qbit-programming.html
 * http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 * http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 * ****************************************************************************
 */

package io.advantageous.qbit.service.impl;

import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.boon.service.impl.BoonInvocationHandlerForSendQueue;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.*;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.transforms.NoOpResponseTransformer;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.Timer;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.advantageous.qbit.QBit.factory;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;

/**
 * @author rhightower on 2/18/15.
 */
public class BaseServiceQueueImpl implements ServiceQueue {
    protected static final ThreadLocal<ServiceQueue> serviceThreadLocal = new ThreadLocal<>();
    protected final QBitSystemManager systemManager;
    protected final Logger logger = LoggerFactory.getLogger(ServiceQueueImpl.class);
    protected final boolean debug = GlobalConstants.DEBUG && logger.isDebugEnabled();
    protected final Object service;
    protected final Queue<Response<Object>> responseQueue;
    protected final Queue<MethodCall<Object>> requestQueue;
    protected final Queue<Event<Object>> eventQueue;
    protected final QueueBuilder requestQueueBuilder;
    protected final QueueBuilder responseQueueBuilder;
    protected final boolean handleCallbacks;
    protected final ServiceMethodHandler serviceMethodHandler;
    protected final SendQueue<Response<Object>> responseSendQueue;
    private final Factory factory;
    private final BeforeMethodSent beforeMethodSent;
    private final Optional<EventManager> eventManager;
    private final boolean joinEventManager;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final BeforeMethodCall beforeMethodCall;
    private final BeforeMethodCall beforeMethodCallAfterTransform;
    private final AfterMethodCall afterMethodCall;
    private final AfterMethodCall afterMethodCallAfterTransform;
    private final CallbackManager callbackManager;
    private final QueueCallBackHandler queueCallBackHandler;
    protected volatile long lastResponseFlushTime = Timer.timer().now();
    private Transformer<Request, Object> requestObjectTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;
    private Transformer<Response<Object>, Response> responseObjectTransformer = new NoOpResponseTransformer();
    private AtomicBoolean failing = new AtomicBoolean();

    public BaseServiceQueueImpl(final String rootAddress,
                                final String serviceAddress,
                                final Object service,
                                final QueueBuilder requestQueueBuilder,
                                final QueueBuilder responseQueueBuilder,
                                final ServiceMethodHandler serviceMethodHandler,
                                final Queue<Response<Object>> responseQueue,
                                final boolean async,
                                final boolean handleCallbacks,
                                final QBitSystemManager systemManager,
                                final BeforeMethodCall beforeMethodCall,
                                final BeforeMethodCall beforeMethodCallAfterTransform,
                                final AfterMethodCall afterMethodCall,
                                final AfterMethodCall afterMethodCallAfterTransform,
                                final QueueCallBackHandler queueCallBackHandler,
                                final CallbackManager callbackManager,
                                final BeforeMethodSent beforeMethodSent,
                                final EventManager eventManager,
                                final boolean joinEventManager) {

        this.eventManager = Optional.ofNullable(eventManager);

        this.joinEventManager = joinEventManager;

        this.beforeMethodSent = beforeMethodSent;
        this.beforeMethodCall = beforeMethodCall;
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        this.afterMethodCall = afterMethodCall;
        this.afterMethodCallAfterTransform = afterMethodCallAfterTransform;

        this.callbackManager = callbackManager;

        if (queueCallBackHandler == null) {
            this.queueCallBackHandler = new QueueCallBackHandler() {
                @Override
                public void queueLimit() {

                }

                @Override
                public void queueEmpty() {

                }
            };
        } else {
            this.queueCallBackHandler = queueCallBackHandler;
        }
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

            logger.info("RESPONSE QUEUE WAS NULL CREATING ONE for service");
            this.responseQueue = this.responseQueueBuilder.setName("Response Queue  " + serviceMethodHandler.address()).build();
        } else {
            this.responseQueue = responseQueue;
        }


        this.responseSendQueue = this.responseQueue.sendQueueWithAutoFlush(100, TimeUnit.MILLISECONDS);
        this.service = service;
        this.serviceMethodHandler = serviceMethodHandler;
        this.serviceMethodHandler.init(service, rootAddress, serviceAddress, responseSendQueue);
        this.eventQueue = this.requestQueueBuilder.setName("Event Queue" + serviceMethodHandler.address()).build();
        this.handleCallbacks = handleCallbacks;
        this.requestQueue = initRequestQueue(serviceMethodHandler, async);
        this.systemManager = systemManager;

        this.factory = factory();

        this.eventManager.ifPresent(em ->
        {

            em.joinService(BaseServiceQueueImpl.this);
        });


    }

    public static ServiceQueue currentService() {
        return serviceThreadLocal.get();
    }

    /**
     * This method is where all of the action is.
     *
     * @param methodCall           methodCall
     * @param serviceMethodHandler handler
     */
    private boolean doHandleMethodCall(MethodCall<Object> methodCall,
                                       final ServiceMethodHandler serviceMethodHandler) {
        if (debug) {
            logger.debug("ServiceImpl::doHandleMethodCall() METHOD CALL" + methodCall);
        }
        if (callbackManager != null) {

            if (methodCall.hasCallback() && serviceMethodHandler.couldHaveCallback(methodCall.name())) {
                callbackManager.registerCallbacks(methodCall);
            }
        }
        //inputQueueListener.receive(methodCall);
        final boolean continueFlag[] = new boolean[1];
        methodCall = beforeMethodProcessing(methodCall, continueFlag);
        if (continueFlag[0]) {
            if (debug) logger.debug("ServiceImpl::doHandleMethodCall() before handling stopped processing");
            return false;
        }
        Response<Object> response = serviceMethodHandler.receiveMethodCall(methodCall);
        if (response != ServiceConstants.VOID) {

            if (!afterMethodCall.after(methodCall, response)) {
                return false;
            }
            //noinspection unchecked
            response = responseObjectTransformer.transform(response);

            if (!afterMethodCallAfterTransform.after(methodCall, response)) {
                return false;
            }

            if (debug) {
                if (response.body() instanceof Throwable) {

                    logger.error("Unable to handle call ", ((Throwable) response.body()));

                }
            }
            if (!responseSendQueue.send(response)) {
                logger.error("Unable to send response {} for method {} for object {}",
                        response,
                        methodCall.name(),
                        methodCall.objectName());
            }

        }

        return false;
    }

    @Override
    public void start() {

        start(serviceMethodHandler, joinEventManager);

    }

    public ServiceQueue startServiceQueue() {

        start(serviceMethodHandler, joinEventManager);
        return this;
    }

    public ServiceQueue start(boolean joinEventManager) {
        start(serviceMethodHandler, joinEventManager);
        return this;
    }

    @Override
    public Queue<MethodCall<Object>> requestQueue() {
        return this.requestQueue;
    }

    @Override
    public Queue<Response<Object>> responseQueue() {
        return this.responseQueue;
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
                        public boolean send(MethodCall<Object> item) {

                            return doHandleMethodCall(item, serviceMethodHandler);
                        }

                        @Override
                        public void sendAndFlush(MethodCall<Object> item) {

                            doHandleMethodCall(item, serviceMethodHandler);
                        }

                        @SafeVarargs
                        @Override
                        public final void sendMany(MethodCall<Object>... items) {

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

                        @Override
                        public int size() {
                            return 0;
                        }
                    };
                }

                @Override
                public void startListener(ReceiveQueueListener<MethodCall<Object>> listener) {

                }

                @Override
                public void stop() {

                }

                @Override
                public int size() {
                    return 0;
                }
            };
        }
        return requestQueue;
    }

    public ServiceQueue startCallBackHandler() {
        if (!handleCallbacks) {
            /** Need to make this configurable. */
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


    private void start(final ServiceMethodHandler serviceMethodHandler,
                       final boolean joinEventManager) {

        if (started.get()) {
            logger.warn("Service {} already started. It will not start twice.", name());
            return;
        }

        logger.info("Starting service {}", name());
        started.set(true);

        final ReceiveQueue<Response<Object>> responseReceiveQueue =
                this.handleCallbacks ?
                        responseQueue.receiveQueue() : null;


        final ReceiveQueue<Event<Object>> eventReceiveQueue =
                eventQueue.receiveQueue();

        serviceThreadLocal.set(this);

        if (!(service instanceof EventManager)) {
            if (joinEventManager) {
                serviceContext().eventManager().joinService(this);
            }
        }
        flushEventManagerCalls();
        serviceThreadLocal.set(null);
        requestQueue.startListener(new ReceiveQueueListener<MethodCall<Object>>() {

            @Override
            public void init() {

                serviceThreadLocal.set(BaseServiceQueueImpl.this);
                queueCallBackHandler.queueInit();
                serviceMethodHandler.init();
            }

            @Override
            public void receive(MethodCall<Object> methodCall) {
                queueCallBackHandler.beforeReceiveCalled();
                doHandleMethodCall(methodCall, serviceMethodHandler);
                queueCallBackHandler.afterReceiveCalled();
            }

            @Override
            public void empty() {

                serviceThreadLocal.set(BaseServiceQueueImpl.this);
                handle();
                serviceMethodHandler.empty();
                queueCallBackHandler.queueEmpty();
            }

            @Override
            public void startBatch() {
                serviceThreadLocal.set(BaseServiceQueueImpl.this);
                serviceMethodHandler.startBatch();
                queueCallBackHandler.queueStartBatch();
            }

            @Override
            public void limit() {

                serviceThreadLocal.set(BaseServiceQueueImpl.this);
                handle();
                serviceMethodHandler.limit();
                queueCallBackHandler.queueLimit();
            }

            @Override
            public void shutdown() {

                serviceThreadLocal.set(BaseServiceQueueImpl.this);
                handle();
                serviceMethodHandler.shutdown();
                queueCallBackHandler.queueShutdown();

                serviceThreadLocal.set(null);
            }

            @Override
            public void idle() {

                serviceThreadLocal.set(BaseServiceQueueImpl.this);
                handle();
                serviceMethodHandler.idle();
                queueCallBackHandler.queueIdle();
                if (callbackManager != null) {
                    callbackManager.process(0);
                }
                serviceThreadLocal.set(null);
            }


            /** Such a small method with so much responsibility. */
            public void handle() {
                manageResponseQueue();
                handleCallBacks(responseReceiveQueue);
                handleEvents(eventReceiveQueue, serviceMethodHandler);
            }

        });
    }

    private void handleEvents(ReceiveQueue<Event<Object>> eventReceiveQueue, ServiceMethodHandler serviceMethodHandler) {
    /* Handles the event processing. */
        Event<Object> event = eventReceiveQueue.poll();
        while (event != null) {
            serviceMethodHandler.handleEvent(event);
            event = eventReceiveQueue.poll();
        }
        flushEventManagerCalls();
    }

    private void handleCallBacks(ReceiveQueue<Response<Object>> responseReceiveQueue) {
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
    }

    private void flushEventManagerCalls() {
        final EventManager eventManager = factory.eventManagerProxy();
        if (eventManager != null) {
            ServiceProxyUtils.flushServiceProxy(eventManager);
            factory.clearEventManagerProxy();
        }
    }

    private void manageResponseQueue() {
        long now = Timer.timer().now();
        if (now - lastResponseFlushTime > 50) {
            lastResponseFlushTime = now;
            responseSendQueue.flushSends();
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


        started.set(false);
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

        if (systemManager != null) {
            this.systemManager.serviceShutDown();
            this.systemManager.unregisterService(this);
        }


        if (!(service instanceof EventManager)) {
            if (joinEventManager) {
                serviceContext().eventManager().leaveEventBus(this);
            }
        }

        eventManager.ifPresent(em -> em.leaveEventBus(BaseServiceQueueImpl.this));
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

    @Override
    public boolean failing() {
        return failing.get();
    }

    @Override
    public boolean running() {
        return started.get();
    }

    @Override
    public void setFailing() {
        failing.set(true);
    }

    @Override
    public void recover() {
        failing.set(false);
    }

    public Object service() {
        return service;
    }


    public <T> T createProxyWithAutoFlush(Class<T> serviceInterface, int interval, TimeUnit timeUnit) {


        final SendQueue<MethodCall<Object>> methodCallSendQueue = requestQueue.sendQueueWithAutoFlush(interval, timeUnit);
        methodCallSendQueue.start();
        return proxy(serviceInterface, methodCallSendQueue);

    }

    @Override
    public <T> T createProxyWithAutoFlush(Class<T> serviceInterface, Duration duration) {
        return createProxyWithAutoFlush(serviceInterface, (int) duration.getDuration(), duration.getTimeUnit());
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

    private <T> void validateInterface(Class<T> serviceInterface) {
        if (!serviceInterface.isInterface()) {
            throw new IllegalStateException("Service Interface must be an interface " + serviceInterface.getName());
        }
        final ClassMeta<T> classMeta = ClassMeta.classMeta(serviceInterface);

        final Method[] declaredMethods = classMeta.cls().getDeclaredMethods();

        for (Method m : declaredMethods) {
            if (!(m.getReturnType() == void.class || PromiseHandle.class.isAssignableFrom(m.getReturnType()))) {
                throw new IllegalStateException("Async interface can only return void or a Promise " + serviceInterface.getName());
            }
        }

    }

    private <T> T proxy(final Class<T> serviceInterface,
                        final SendQueue<MethodCall<Object>> methodCallSendQueue) {

        validateInterface(serviceInterface);

        if (!started.get()) {
            logger.debug("ServiceQueue::create(...), A proxy is being asked for a service that is not started ", name());
        }
        final InvocationHandler invocationHandler = new BoonInvocationHandlerForSendQueue(methodCallSendQueue,
                serviceInterface, serviceInterface.getSimpleName(), beforeMethodSent);
        final Object o = Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class[]{serviceInterface, ClientProxy.class}, invocationHandler
        );
        //noinspection unchecked
        return (T) o;
    }

    @Override
    public SendQueue<Event<Object>> events() {
        return this.eventQueue.sendQueueWithAutoFlush(50, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "ServiceQueue{" +
                "service=" + service.getClass().getSimpleName() +
                '}';
    }


}

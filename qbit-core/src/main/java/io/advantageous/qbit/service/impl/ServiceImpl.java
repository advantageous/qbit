package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.message.*;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.transforms.NoOpResponseTransformer;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import org.boon.core.reflection.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;


public class ServiceImpl implements Service {

    private final Logger logger = LoggerFactory.getLogger(ServiceImpl.class);
    private final boolean debug = logger.isDebugEnabled();

    private final Object service;
    private ServiceMethodHandler serviceMethodHandler;


    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;

    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;

    private AfterMethodCall afterMethodCall = new NoOpAfterMethodCall();

    private AfterMethodCall afterMethodCallAfterTransform = new NoOpAfterMethodCall();

    private ReceiveQueueListener<MethodCall<Object>> inputQueueListener = new NoOpInputMethodCallQueueListener();

    private final Queue<Response<Object>> responseQueue;

    private final Queue<MethodCall<Object>> requestQueue;

    private Transformer<Request, Object> requestObjectTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;

    private Transformer<Response<Object>, Response> responseObjectTransformer = new NoOpResponseTransformer();


    private SendQueue<Response<Object>> responseSendQueue;

    final QueueBuilder queueBuilder;


    private CallbackManager callbackManager;


    public ServiceImpl(final String rootAddress,
                       final String serviceAddress,
                       final Object service,
                       final QueueBuilder queueBuilder,
                       final ServiceMethodHandler serviceMethodHandler,
                       final Queue<Response<Object>> responseQueue,
                       final boolean async) {



        this.service = service;
        this.serviceMethodHandler = serviceMethodHandler;

        serviceMethodHandler.init(service, rootAddress, serviceAddress);



        if (queueBuilder==null) {
            this.queueBuilder = new QueueBuilder();
        } else {
            this.queueBuilder = BeanUtils.copy(queueBuilder);
        }



        if (responseQueue == null) {

            if (debug) {
                logger.debug("RESPONSE QUEUE WAS NULL CREATING ONE");
            }

            this.responseQueue = this.queueBuilder.setName("Response Queue  " + serviceMethodHandler.address()).build();

        } else {
            this.responseQueue = responseQueue;
        }

        responseSendQueue = this.responseQueue.sendQueue();

        serviceMethodHandler.initQueue(responseSendQueue);


        if (async) {
            requestQueue = this.queueBuilder.setName("Send Queue  " + serviceMethodHandler.address()).build();

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

        if (async) {
            start(serviceMethodHandler);
        }


    }



    public Service startCallBackHandler() {
        callbackManager = new CallbackManager();
        callbackManager.startReturnHandlerProcessor(this.responseQueue);
        return this;

    }


    public ServiceImpl requestObjectTransformer(Transformer<Request, Object> requestObjectTransformer) {
        this.requestObjectTransformer = requestObjectTransformer;
        return this;
    }

    public ServiceImpl responseObjectTransformer(Transformer<Response<Object>, Response> responseObjectTransformer) {
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
                                    final ServiceMethodHandler serviceMethodHandler
                                    ) {
        if (debug) {
            logger.debug("ServiceImpl::doHandleMethodCall() METHOD CALL" + methodCall );
        }

        if (callbackManager!=null) {
            callbackManager.registerCallbacks(methodCall);
        }
        inputQueueListener.receive(methodCall);

        final boolean continueFlag[] = new boolean[1];

        methodCall = beforeMethodProcessing(methodCall, continueFlag);

        if (continueFlag[0]) {
            if (debug) logger.info("ServiceImpl::doHandleMethodCall() before handling stopped processing");
            return;
        }

        Response<Object> response = serviceMethodHandler.receiveMethodCall(methodCall);

        if (debug) {
            logger.debug("ServiceImpl::receive() \nRESPONSE\n" + response + "\nFROM CALL\n" + methodCall + "\n\n");
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





    protected ReentrantLock responseLock  = new ReentrantLock();
    protected volatile long lastResponseFlushTime = Timer.timer().now();

    private void start(final ServiceMethodHandler serviceMethodHandler
                       ) {



        requestQueue.startListener(new ReceiveQueueListener<MethodCall<Object>>() {
            @Override
            public void receive(MethodCall<Object> methodCall) {
                doHandleMethodCall(methodCall, serviceMethodHandler);
            }

            @Override
            public void empty() {

                manageResponseQueue();
                if (inputQueueListener != null) {
                    inputQueueListener.empty();
                }
                serviceMethodHandler.empty();
            }

            @Override
            public void limit() {

                manageResponseQueue();
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

                manageResponseQueue();
                if (inputQueueListener != null) {
                    inputQueueListener.idle();
                }
                serviceMethodHandler.idle();
            }
        });
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
    public ReceiveQueue<Response<Object>> responses() {
        return responseQueue.receiveQueue();
    }

    @Override
    public ReceiveQueue<Event> events() {
        return null;
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
        requestQueue.stop();
        responseQueue.stop();
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


    static class MethodCallLocal implements MethodCall<Object>{

        private final String name;
        private final long timestamp;
        private final Object[] arguments;

        public MethodCallLocal(String name, long timestamp, Object[] args) {
            this.name = name;
            this.timestamp = timestamp;
            this.arguments = args;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String address() {
            return "";
        }

        @Override
        public String returnAddress() {
            return "";
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
            return timestamp;
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

    public <T> T createProxy(Class<T> serviceInterface) {


        final SendQueue<MethodCall<Object>> methodCallSendQueue = requestQueue.sendQueue();


        InvocationHandler invocationHandler = new InvocationHandler() {

            long timestamp = Timer.timer().now();
            int times = 10;

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {



                if (method.getName().equals("clientProxyFlush")) {

                    methodCallSendQueue.flushSends();
                    return null;
                }
                times--;
                if (times == 0){
                    timestamp = Timer.timer().now();
                    times = 10;
                } else {
                    timestamp++;
                }

                final MethodCallLocal call = new MethodCallLocal(method.getName(), timestamp, args);
                methodCallSendQueue.send(call);
                return null;
            }
        };

        final Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceInterface, ClientProxy.class}, invocationHandler
        );


        return (T) o;


    }



}

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.*;
import io.advantageous.qbit.Timer;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.queue.impl.BasicQueue;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.method.impl.MethodCallImpl;
import io.advantageous.qbit.transforms.NoOpRequestTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServiceBundleImpl implements ServiceBundle {

    private Map<String, SendQueue<MethodCall<Object>>> serviceMapping = new ConcurrentHashMap<>();

    private Set<Service> services = new ConcurrentHashSet<>(10);

    private final Logger logger = LoggerFactory.getLogger(ServiceBundleImpl.class);

    final BasicQueue<MethodCall<Object>> methodQueue;

    final SendQueue<MethodCall<Object>> methodSendQueue;

    private Set<SendQueue<MethodCall<Object>>> sendQueues = new ConcurrentHashSet<>(10);

    private Queue<Response<Object>> responseQueue;

    private final String address;

    private Factory factory;

    private class HandlerKey {
        final String returnAddress;
        final long messageId;

        private HandlerKey(String returnAddress, long messageId) {
            this.returnAddress = returnAddress;
            this.messageId = messageId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final HandlerKey that = (HandlerKey) o;
            return messageId == that.messageId
                    && !(returnAddress != null
                    ? !returnAddress.equals(that.returnAddress)
                    : that.returnAddress != null);
        }

        @Override
        public int hashCode() {
            int result = returnAddress != null ? returnAddress.hashCode() : 0;
            result = 31 * result + (int) (messageId ^ (messageId >>> 32));
            return result;
        }
    }

    private Map<HandlerKey, Callback<Object>> handlers = new ConcurrentHashMap<>();

    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;

    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;

    private NoOpRequestTransform argTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;

    private TreeSet<String> addressesByDescending = new TreeSet<>(
            new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            }
    );

    private TreeSet<String> seenAddressesDescending = new TreeSet<>(
            new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            }
    );

    public ServiceBundleImpl(String address, int batchSize, int pollRate, Factory factory) {
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }

        this.address = address;

        this.factory = factory;
        this.responseQueue = new BasicQueue<>("Response Queue " + address, pollRate,
                TimeUnit.MILLISECONDS, batchSize);

        this.methodQueue = new BasicQueue<>("Send Queue " + address, pollRate, TimeUnit.MILLISECONDS, batchSize);

        methodSendQueue = methodQueue.sendQueue();

        start();
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public void addService(Object object) {
        addService(null, object);
    }

    @Override
    public void addService(String serviceAddress, Object object) {

        if (GlobalConstants.DEBUG) {
            logger.info(ServiceBundleImpl.class.getName(), serviceAddress, object);
        }

        final Service service = factory.createService(address, serviceAddress,
                object, responseQueue);

        services.add(service);

        final SendQueue<MethodCall<Object>> requests = service.requests();

        if (serviceAddress != null && !serviceAddress.isEmpty()) {
            serviceMapping.put(serviceAddress, requests);
        }
        serviceMapping.put(service.name(), requests);

        sendQueues.add(requests);

        final Collection<String> addresses = service.addresses(this.address);

        if (GlobalConstants.DEBUG) {
            logger.info(ServiceBundleImpl.class.getName(), "addresses", addresses);
        }

        for (String addr : addresses) {
            addressesByDescending.add(addr);
            SendQueue<MethodCall<Object>> methodCallSendQueue = serviceMapping.get(service.name());
            serviceMapping.put(addr, methodCallSendQueue);
        }
    }

    @Override
    public ReceiveQueue<Response<Object>> responses() {
        return responseQueue.receiveQueue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void call(MethodCall<Object> methodCall) {
        if (GlobalConstants.DEBUG) {
            logger.info(ServiceBundleImpl.class.getName(), "::call()",
                    methodCall.name(),
                    methodCall.address(),
                    "\n", methodCall);
        }
        final Object object = methodCall.body();
        if (object instanceof List) {
            final List list = (List) object;
            for (Object arg : list) {
                if (arg instanceof Callback) {
                    registerHandlerCallbackForClient(methodCall, (Callback) arg);
                }
            }
        } else if (object instanceof Object[]) {
            final Object[] array = (Object[]) object;
            for (Object arg : array) {
                if (arg instanceof Callback) {
                    registerHandlerCallbackForClient(methodCall, ((Callback) arg));
                }
            }
        }
        methodSendQueue.send(methodCall);
    }

    private void registerHandlerCallbackForClient(final MethodCall<Object> methodCall,
                                                  final Callback<Object> handler) {
        handlers.put(new HandlerKey(methodCall.returnAddress(), methodCall.id()), handler);
    }

    public void startReturnHandlerProcessor() {
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
                        //TODO: handle non-throwable response
                    }
                } else {
                    handler.accept(response.body());
                }
            }

            @Override
            public void empty() {

            }

            @Override
            public void limit() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {

            }
        });
    }

    @Override
    public <T> T createLocalProxy(Class<T> serviceInterface, String myService) {
        //return factory.createP;
        return null;
    }

    private void doCall(MethodCall<Object> methodCall) {

        if (GlobalConstants.DEBUG) {
            logger.info(ServiceBundleImpl.class.getName(), "::doCall()",
                    methodCall.name(),
                    methodCall.address(),
                    "\n", methodCall);
        }

        boolean[] continueFlag = new boolean[1];
        methodCall = beforeMethodCall(methodCall, continueFlag);

        if (!continueFlag[0]) {
            logger.info(ServiceBundleImpl.class.getName(), "::doCall()",
                    "Flag from before call handling does not want to continue");
        }

        SendQueue<MethodCall<Object>> sendQueue = null;

        if (methodCall.address() != null && !methodCall.address().isEmpty()) {
            sendQueue = handleByAddressCall(methodCall);
        } else if (methodCall.objectName() != null && !methodCall.objectName().isEmpty()) {
            sendQueue = serviceMapping.get(methodCall.objectName());
        }

        if (sendQueue == null) {
            throw new IllegalStateException("there is no object at this address: " + methodCall);
        }
        sendQueue.send(methodCall);
    }

    private SendQueue<MethodCall<Object>> handleByAddressCall(final MethodCall<Object> methodCall) {
        SendQueue<MethodCall<Object>> sendQueue;
        final String callAddress = methodCall.address();
        sendQueue = serviceMapping.get(callAddress);

        if (sendQueue == null) {

            String addr;

            /* Check the ones we are using to reduce search time. */
            addr = seenAddressesDescending.higher(callAddress);
            if (addr != null && callAddress.startsWith(addr)) {
                sendQueue = serviceMapping.get(addr);
                return sendQueue;
            }

            /* if it was not in one of the ones we are using check the rest. */
            addr = addressesByDescending.higher(callAddress);

            if (addr != null && callAddress.startsWith(addr)) {
                sendQueue = serviceMapping.get(addr);

                if (sendQueue != null) {
                    seenAddressesDescending.add(addr);
                }
            }
        }
        return sendQueue;
    }

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

    private MethodCall<Object> transformBeforeMethodCall(MethodCall<Object> methodCall) {
        if (argTransformer == null || argTransformer == ServiceConstants.NO_OP_ARG_TRANSFORM) {
            return methodCall;
        }
        Object arg = this.argTransformer.transform(methodCall);
        return MethodCallImpl.transformed(methodCall, arg);
    }

    @Override
    public void flushSends() {
        if (GlobalConstants.DEBUG) {
            logger.info(ServiceBundleImpl.class.getName(), "::flushSends()");
        }
        this.methodSendQueue.flushSends();
    }

    public void stop() {
        if (GlobalConstants.DEBUG) {
            logger.info(ServiceBundleImpl.class.getName(), "::stop()");
        }
        methodQueue.stop();
        for (Service service : services) {
            service.stop();
        }
    }

    @Override
    public List<String> endPoints() {
        return new ArrayList<>(serviceMapping.keySet());
    }

    private void start() {
        methodQueue.startListener(new ReceiveQueueListener<MethodCall<Object>>() {

            long time;

            long lastTimeAutoFlush;

            @Override
            public void receive(MethodCall<Object> item) {
                doCall(item);
            }

            @Override
            public void empty() {
                time = Timer.timer().now();
                if (time > (lastTimeAutoFlush + 50)) {

                    for (SendQueue<MethodCall<Object>> sendQueue : sendQueues) {
                        sendQueue.flushSends();
                    }
                    lastTimeAutoFlush = time;
                }
            }

            @Override
            public void limit() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {

            }
        });
    }
}

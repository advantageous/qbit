package org.qbit.service.impl;

import org.boon.*;
import org.boon.collections.ConcurrentHashSet;
import org.boon.concurrent.Timer;
import org.boon.core.Handler;
import org.boon.core.HandlerWithErrorHandling;
import org.boon.datarepo.ObjectEditor;
import org.qbit.Factory;
import org.qbit.GlobalConstants;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.Queue;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.ReceiveQueueListener;
import org.qbit.queue.SendQueue;
import org.qbit.queue.impl.BasicQueue;
import org.qbit.service.BeforeMethodCall;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;
import org.qbit.service.method.impl.MethodCallImpl;
import org.qbit.transforms.NoOpRequestTransform;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.boon.Boon.putl;
import static org.boon.Exceptions.die;

public class ServiceBundleImpl implements ServiceBundle {

    private Map<String, SendQueue<MethodCall<Object>>> serviceMapping
            = new ConcurrentHashMap<>();


    private Set<Service> services = new ConcurrentHashSet<>(10);


    private Logger logger = Boon.logger(ServiceBundleImpl.class);

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

            HandlerKey that = (HandlerKey) o;

            if (messageId != that.messageId) return false;
            if (returnAddress != null ? !returnAddress.equals(that.returnAddress) : that.returnAddress != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = returnAddress != null ? returnAddress.hashCode() : 0;
            result = 31 * result + (int) (messageId ^ (messageId >>> 32));
            return result;
        }
    }

    private Map<HandlerKey, Handler> handlers = new ConcurrentHashMap<>();

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
            address = Str.slc(address, 0, -1);
        }

        this.address = address;

        this.factory = factory;
        this.responseQueue =  new BasicQueue<>("Response Queue " + address, pollRate,
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
            logger.info(Boon.className(this), serviceAddress, object);
        }


        final Service service = factory.createService(address, serviceAddress,
                 object, responseQueue);

        services.add(service);

        final SendQueue<MethodCall<Object>> requests = service.requests();

        if (!Str.isEmpty(serviceAddress)) {
            serviceMapping.put(serviceAddress, requests);
        }
        serviceMapping.put(service.name(), requests);

        sendQueues.add(requests);

        final Collection<String> addresses = service.addresses(this.address);


        if (GlobalConstants.DEBUG) {
            logger.info(Boon.className(this), "addresses", addresses);
        }



        for (String addr : addresses) {


            addressesByDescending.add(addr);
            SendQueue<MethodCall<Object>> methodCallSendQueue =
                    serviceMapping.get(service.name());

            serviceMapping.put(addr, methodCallSendQueue);
        }



    }

    @Override
    public ReceiveQueue<Response<Object>> responses() {
        return responseQueue.receiveQueue();
    }



    @Override
    public void call(MethodCall<Object> methodCall) {

        if (GlobalConstants.DEBUG) {
            logger.info(Boon.className(this), "::call()",
                    methodCall.name(),
                    methodCall.address(),
                    "\n", methodCall);
        }

        Object object = methodCall.body();

        if (object instanceof List) {
            List list = (List)object;


            for (int index = 0; index < list.size(); index++) {

                Object arg = list.get(index);
                if (arg instanceof Handler) {
                    registerHandlerCallbackForClient(methodCall, (Handler) arg);
                }
            }

        } else if (object instanceof Object[]) {
            Object[] array = (Object[]) object;

            for (int index = 0; index < array.length; index++) {

                Object arg = array[index];
                if (arg instanceof Handler) {

                    registerHandlerCallbackForClient(methodCall, (Handler) arg);
                }
            }
        }

        methodSendQueue.send(methodCall);

    }

    private void registerHandlerCallbackForClient(MethodCall<Object> methodCall, Handler arg) {

        if (arg instanceof HandlerWithErrorHandling) {
            final HandlerWithErrorHandling clientHandler = (HandlerWithErrorHandling) arg;

            final Handler<Object> handler = new HandlerWithErrorHandling<Object>() {
                @Override
                public void handle(Object event) {
                    clientHandler.handle(event);
                }

                @Override
                public Handler<Throwable> errorHandler() {
                    return clientHandler.errorHandler();
                }
            };

            handlers.put(new HandlerKey(methodCall.returnAddress(), methodCall.id()), handler);

        } else {
            final Handler clientHandler = (Handler) arg;

            final Handler<Object> handler = new Handler<Object>() {
                @Override
                public void handle(Object event) {
                    clientHandler.handle(event);
                }
            };

            handlers.put(new HandlerKey(methodCall.returnAddress(), methodCall.id()), handler);

        }


    }

    public void startReturnHandlerProcessor() {
        responseQueue.startListener(new ReceiveQueueListener<Response<Object>>() {
            @Override
            public void receive(Response<Object> response) {

                final Handler handler = handlers.get(
                        new HandlerKey(response.returnAddress(), response.id()));

                if (handler instanceof HandlerWithErrorHandling) {
                    HandlerWithErrorHandling handling = (HandlerWithErrorHandling) handler;
                    if (response.wasErrors()) {
                        logger.info("Service threw an exception address", response.address(),
                                "\n return address", response.returnAddress(), "\n message id",
                                response.id());

                        handling.errorHandler().handle(response.body());
                    } else {
                        handler.handle(response.body());
                    }
                } else {

                    if (response.wasErrors()) {

                        if (response.body() instanceof Throwable) {
                            Throwable t = (Throwable) response.body();
                            logger.error(t, "Service threw an exception address", response.address(),
                                    "\n return address", response.returnAddress(), "\n message id",
                                    response.id());

                        } else {
                            logger.error("Service threw an exception address", response.address(),
                                    "\n return address", response.returnAddress(), "\n message id",
                                    response.id());

                        }

                    } else {
                        handler.handle(response.body());
                    }
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
    public <T> T createLocalProxyWithReturnAddress(Class<T> serviceInterface, String serviceName, String returnAddressArg) {
        return factory.createLocalProxyWithReturnAddress(serviceInterface, serviceName, returnAddressArg, this);
    }

    @Override
    public <T> T createLocalProxy(Class<T> serviceInterface, String serviceName) {
        return factory.createLocalProxy(serviceInterface, serviceName, this);
    }

    private void doCall(MethodCall<Object> methodCall) {

        if (GlobalConstants.DEBUG) {
            logger.info(Boon.className(this), "::doCall()",
                    methodCall.name(),
                    methodCall.address(),
                    "\n", methodCall);
        }

        boolean [] continueFlag = new boolean[1];
        methodCall = beforeMethodCall(methodCall, continueFlag);

        if (!continueFlag[0]) {
            logger.info(Boon.className(this), "::doCall()",
                    "Flag from before call handling does not want to continue");
        }

        SendQueue<MethodCall<Object>> sendQueue = null;

        if (  !Str.isEmpty(methodCall.address())  ) {
            sendQueue = handleByAddressCall(methodCall);
        } else if (  !Str.isEmpty(methodCall.objectName())  ) {
            sendQueue = serviceMapping.get(methodCall.objectName());
        }

        if (GlobalConstants.DEBUG && sendQueue==null) {

            putl(serviceMapping.keySet());
            die("SEND QUEUE IS NULL FOR METHOD", methodCall,
                    "\n",  serviceMapping.keySet());

            return;

        }

        sendQueue.send(methodCall);

    }

    private SendQueue<MethodCall<Object>> handleByAddressCall(MethodCall<Object> methodCall) {
        SendQueue<MethodCall<Object>> sendQueue;
        final String callAddress = methodCall.address();



        sendQueue = serviceMapping.get(callAddress);

        if (sendQueue==null) {

            String addr;


            /* Check the ones we are using to reduce search time. */
            addr = seenAddressesDescending.higher(callAddress);
            if (addr !=null && callAddress.startsWith(addr)) {
                sendQueue = serviceMapping.get(addr);
                return sendQueue;
            }


            /* if it was not in one of the ones we are using check the rest. */
            addr = addressesByDescending.higher(callAddress);


            if (addr!=null && callAddress.startsWith(addr)) {
                sendQueue = serviceMapping.get(addr);

                if (sendQueue!=null) {
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

        }else {
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
            logger.info(Boon.className(this), "::flushSends()");
        }

        this.methodSendQueue.flushSends();
    }

    public void stop() {


        if (GlobalConstants.DEBUG) {
            logger.info(Boon.className(this), "::stop()");
        }

        methodQueue.stop();

        for (Service service : services) {
            service.stop();
        }

    }

    @Override
    public List<String> endPoints() {
        return Lists.list(serviceMapping.keySet());
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

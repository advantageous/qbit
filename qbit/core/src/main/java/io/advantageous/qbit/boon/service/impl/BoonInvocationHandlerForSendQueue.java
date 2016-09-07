package io.advantageous.qbit.boon.service.impl;

import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.reakt.Reakt;
import io.advantageous.qbit.util.Timer;
import io.advantageous.reakt.Callback;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.advantageous.boon.core.Str.sputs;

public class BoonInvocationHandlerForSendQueue implements InvocationHandler {

    private final String serviceName;
    private final String returnAddress;
    private final BeforeMethodSent beforeMethodSent;
    private final SendQueue<MethodCall<Object>> sendQueue;
    private long generatedMessageId;
    private long timestamp;
    private int times;
    private Map<String, Boolean> methodMetaMap = new HashMap<>();
    private Map<String, Boolean> promiseMap = new HashMap<>();

    public BoonInvocationHandlerForSendQueue(SendQueue<MethodCall<Object>> sendQueue,
                                             Class<?> serviceInterface,
                                             String serviceName,
                                             BeforeMethodSent beforeMethodSent) {
        this.serviceName = serviceName;
        this.returnAddress = serviceInterface.getName() + "::" + UUID.randomUUID().toString();
        this.sendQueue = sendQueue;
        this.beforeMethodSent = beforeMethodSent != null ? beforeMethodSent : new BeforeMethodSent() {
        };

        for (Method method : serviceInterface.getMethods()) {
            promiseMap.put(method.getName(), PromiseHandle.class.isAssignableFrom(method.getReturnType()));
            methodMetaMap.put(method.getName(), hasReaktCallback(method.getParameterTypes()));
        }
        timestamp = Timer.timer().now();
        times = 10;
    }

    private Boolean hasReaktCallback(Class<?>[] parameterTypes) {
        for (Class<?> cls : parameterTypes) {
            if (cls == Callback.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        switch (method.getName()) {
            case "clientProxyFlush":
            case "flush":
                sendQueue.flushSends();
                return null;
            case "stop":
                sendQueue.stop();
                return null;
            case "toString":
                return sputs("{Local Proxy", serviceName, "}");
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return proxy.hashCode();

        }


        if (isPromise(method)) {

            final MethodCallBuilder methodCallBuilder = createMethodBuilder(method, args);

            return new InvokePromiseWithSendQueue(sendQueue, methodCallBuilder, beforeMethodSent);
        } else {
            return doInvoke(method, args);
        }
    }

    private Object doInvoke(Method method, Object[] args) {
        if (isReaktMethodCall(method)) {
            convertToReaktCallbacks(args);
        }
        final MethodCallBuilder methodCallBuilder = createMethodBuilder(method, args);
        beforeMethodSent.beforeMethodSent(methodCallBuilder);
        final MethodCall<Object> call = methodCallBuilder.build();
        sendQueue.send(call);
        return null;
    }

    private void generateTimeStamp() {
        times--;
        if (times == 0) {
            timestamp = Timer.timer().now();
            times = 10;
        } else {
            timestamp++;
        }
    }

    private MethodCallBuilder createMethodBuilder(Method method, Object[] args) {

        long messageId = generatedMessageId++;
        generateTimeStamp();


        final String name = method.getName();
        return MethodCallBuilder.methodCallBuilder()
                .setLocal(true)
                .setAddress(name)
                .setName(name)
                .setReturnAddress(returnAddress)
                .setTimestamp(timestamp).setId(messageId)
                .setBodyArgs(args);
    }


    private Boolean isPromise(Method method) {
        return promiseMap.get(method.getName());
    }

    private boolean isReaktMethodCall(Method method) {
        return methodMetaMap.get(method.getName());
    }


    private void convertToReaktCallbacks(Object[] args) {
        for (int index = 0; index < args.length; index++) {
            Object object = args[index];
            if (object instanceof Callback) {
                args[index] = Reakt.convertCallback(((Callback) object));
            }
        }
    }
}

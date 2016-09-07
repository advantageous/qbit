package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.primitive.CharBuf;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.reakt.Reakt;
import io.advantageous.qbit.service.EndPoint;
import io.advantageous.qbit.util.Timer;
import io.advantageous.reakt.Callback;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.advantageous.boon.core.Str.sputs;

class BoonInvocationHandlerForEndPoint implements InvocationHandler {

    private final String serviceName;
    private final String host;
    private final int port;
    private final AtomicBoolean connected;
    private final EndPoint endPoint;
    private final BeforeMethodSent beforeMethodSent;
    private final String objectAddress;
    private final String returnAddress;
    private final ThreadLocal<CharBuf> addressCreatorBufRef;
    private final AtomicLong generatedMessageId;
    private long timestamp;
    private int times;
    private Map<String, Boolean> methodMetaMap = new HashMap<>();

    private Map<String, Boolean> promiseMap = new HashMap<>();

    BoonInvocationHandlerForEndPoint(AtomicLong generatedMessageId, Class<?> serviceInterface, String serviceName, String host, int port,
                                     AtomicBoolean connected, EndPoint endPoint,
                                     BeforeMethodSent beforeMethodSent, String objectAddress, String returnAddress,
                                     ThreadLocal<CharBuf> addressCreatorBufRef) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.connected = connected;
        this.endPoint = endPoint;
        this.beforeMethodSent = beforeMethodSent != null ? beforeMethodSent : new BeforeMethodSent() {
        };
        this.objectAddress = objectAddress;
        this.returnAddress = returnAddress;
        this.addressCreatorBufRef = addressCreatorBufRef;
        this.generatedMessageId = generatedMessageId;


        for (Method method : serviceInterface.getMethods()) {
            promiseMap.put(method.getName(), PromiseHandle.class.isAssignableFrom(method.getReturnType()) );
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
            case "port":
                return port;
            case "host":
                return host;
            case "silentClose":
                try {
                    assert endPoint != null;
                    endPoint.stop();
                } catch (Exception ex) {
                    //silentClose
                }
            case "flush":
            case "clientProxyFlush":
                assert endPoint != null;
                endPoint.flush();
                return null;
            case "toString":
                return port == 0 ? sputs("{Local Proxy", serviceName, "}") :
                        sputs("{Remote Proxy", serviceName, host, port, "}");
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return proxy.hashCode();
            case "connected":
                return connected.get();

        }


        if (isPromise(method)) {

            final MethodCallBuilder methodCallBuilder = createMethodBuilder(method, args);
            return new InvokePromiseWithEndPoint(endPoint, methodCallBuilder, beforeMethodSent);
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
        endPoint.call(call);
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

        long messageId = generatedMessageId.incrementAndGet();
        generateTimeStamp();
        final String address = createAddress(method);
        return MethodCallBuilder.methodCallBuilder()
                .setId(messageId)
                .setAddress(address)
                .setObjectName(serviceName)
                .setReturnAddress(returnAddress)
                .setName(method.getName())
                .setTimestamp(timestamp)
                .setBody(args);
    }

    private String createAddress(Method method) {
        final CharBuf addressBuf = addressCreatorBufRef.get();

        addressBuf.recycle();

        addressBuf.add(objectAddress).add("/").add(method.getName());

        return addressBuf.toString();
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

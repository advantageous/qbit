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

package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.primitive.CharBuf;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.client.RemoteTCPClientProxy;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.reakt.Reakt;
import io.advantageous.qbit.service.EndPoint;
import io.advantageous.qbit.util.Timer;
import io.advantageous.reakt.Callback;
import io.advantageous.reakt.Invokable;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.impl.BasePromise;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import static io.advantageous.boon.core.Str.sputs;


/**
 * created by Richard on 10/1/14.
 *
 * @author Rick Hightower
 */
public class BoonServiceProxyFactory implements ServiceProxyFactory {
    private static AtomicLong generatedMessageId = new AtomicLong();
    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    Object context = Sys.contextToHold();
    public BoonServiceProxyFactory() {
    }

    @Override
    public <T> T createProxyWithReturnAddress(final Class<T> serviceInterface,
                                              final String serviceName,
                                              final String host,
                                              final int port,
                                              final AtomicBoolean connected,
                                              String returnAddressArg,
                                              final EndPoint endPoint,
                                              final BeforeMethodSent beforeMethodSent) {

        final String objectAddress = endPoint != null ? Str.add(endPoint.address(), "/", serviceName) : "";


        if (Str.isEmpty(returnAddressArg)) {
            returnAddressArg = Str.add(objectAddress, "/" + UUID.randomUUID());
        }

        final String returnAddress = returnAddressArg;

        final ThreadLocal<CharBuf> addressCreatorBufRef = new ThreadLocal<CharBuf>() {
            @Override
            protected CharBuf initialValue() {
                return CharBuf.createCharBuf(255);
            }
        };


        final InvocationHandler invocationHandler = createInvocationHandler(serviceInterface, serviceName, host, port, connected,
                endPoint, beforeMethodSent, objectAddress, returnAddress, addressCreatorBufRef);


        if (port == 0) {
            //noinspection unchecked
            return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                    new Class[]{serviceInterface, ClientProxy.class}, invocationHandler);
        } else {
            //noinspection unchecked
            return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                    new Class[]{serviceInterface, RemoteTCPClientProxy.class}, invocationHandler);
        }


    }


    private InvocationHandler createInvocationHandler(Class<?> serviceInterface,
                                                      final String serviceName,
                                                      final String host,
                                                      final int port,
                                                      final AtomicBoolean connected,
                                                      final EndPoint endPoint,
                                                      final BeforeMethodSent beforeMethodSent,
                                                      final String objectAddress,
                                                      final String returnAddress,
                                                      final ThreadLocal<CharBuf> addressCreatorBufRef) {

        return new BoonInvocationHandler(serviceInterface, serviceName, host, port, connected, endPoint,
                beforeMethodSent, objectAddress, returnAddress, addressCreatorBufRef);
    }


    @Override
    public <T> T createProxy(Class<T> serviceInterface, String serviceName, EndPoint endPoint, BeforeMethodSent beforeMethodSent) {
        return createProxyWithReturnAddress(serviceInterface, serviceName, "local", 0, new AtomicBoolean(true), "", endPoint, beforeMethodSent);
    }


    private void convertToReaktCallbacks(Object[] args) {
        for (int index = 0; index < args.length; index++) {
            Object object = args[index];
            if (object instanceof Callback) {
                args[index] = Reakt.convertCallback(((Callback) object));
            }
        }
    }

    class InvokePromise  extends BasePromise<Object> implements Invokable {

        private final MethodCallBuilder methodCallBuilder;
        private final EndPoint endPoint;

        InvokePromise(EndPoint endPoint, MethodCallBuilder methodCallBuilder) {
            this.endPoint = endPoint;
            this.methodCallBuilder = methodCallBuilder;
        }

        @Override
        public void invoke() {
            methodCallBuilder.setCallback(Reakt.convertPromise(this));
            endPoint.call(methodCallBuilder.build());
        }

        @Override
        public boolean isInvokable() {
            return true;
        }
    }

    class BoonInvocationHandler implements InvocationHandler {

        private final String serviceName;
        private final String host;
        private final int port;
        private final AtomicBoolean connected;
        private final EndPoint endPoint;
        private final BeforeMethodSent beforeMethodSent;
        private final String objectAddress;
        private final String returnAddress;
        private final ThreadLocal<CharBuf> addressCreatorBufRef;
        private long timestamp;
        private int times;
        private Map<String, Boolean> methodMetaMap = new HashMap<>();

        private Map<String, Boolean> promiseMap = new HashMap<>();

        BoonInvocationHandler(Class<?> serviceInterface, String serviceName, String host, int port,
                              AtomicBoolean connected, EndPoint endPoint,
                              BeforeMethodSent beforeMethodSent, String objectAddress, String returnAddress,
                              ThreadLocal<CharBuf> addressCreatorBufRef) {
            this.serviceName = serviceName;
            this.host = host;
            this.port = port;
            this.connected = connected;
            this.endPoint = endPoint;
            this.beforeMethodSent = beforeMethodSent;
            this.objectAddress = objectAddress;
            this.returnAddress = returnAddress;
            this.addressCreatorBufRef = addressCreatorBufRef;

            for (Method method : serviceInterface.getMethods()) {
                promiseMap.put(method.getName(), method.getReturnType() == Promise.class);
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
                return new InvokePromise(endPoint, methodCallBuilder);
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
    }
}

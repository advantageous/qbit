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

import io.advantageous.boon.Str;
import io.advantageous.boon.primitive.CharBuf;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.client.RemoteTCPClientProxy;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.service.EndPoint;
import io.advantageous.qbit.util.Timer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.advantageous.boon.Boon.sputs;

/**
 * Created by Richard on 10/1/14.
 *
 * @author Rick Hightower
 */
public class BoonServiceProxyFactory implements ServiceProxyFactory {

    private static volatile long generatedMessageId;
    private final Factory factory;


    public BoonServiceProxyFactory(Factory factory) {
        this.factory = factory;
    }

    @Override
    public <T> T createProxyWithReturnAddress(final Class<T> serviceInterface,
                                              final String serviceName,
                                              final String host,
                                              final int port,
                                              final AtomicBoolean connected,
                                              String returnAddressArg,
                                              final EndPoint endPoint) {

        final String objectAddress = endPoint != null ? Str.add(endPoint.address(), "/", serviceName) : "";


        if ( !Str.isEmpty(returnAddressArg) ) {
            returnAddressArg = Str.add(objectAddress, "/" + UUID.randomUUID());
        }

        final String returnAddress = returnAddressArg;

        final ThreadLocal<CharBuf> addressCreatorBufRef = new ThreadLocal<CharBuf>() {
            @Override
            protected CharBuf initialValue() {
                return CharBuf.createCharBuf(255);
            }
        };


        InvocationHandler invocationHandler = new InvocationHandler() {

            long timestamp = Timer.timer().now();
            int times = 10;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {




                switch (method.getName()) {
                    case "port":
                        return port;
                    case "host":
                        return host;
                    case "flush":
                    case "clientProxyFlush":
                        endPoint.flush();
                        return null;
                    case "toString":
                        return port == 0 ? sputs("{Local Proxy", serviceName, "}") :
                                sputs("{Remote Proxy", serviceName, host, port, "}");
                    case "connected":
                        return connected.get();

                }


                long messageId = generatedMessageId++;

                times--;
                if ( times == 0 ) {
                    timestamp = Timer.timer().now();
                    times = 10;
                } else {
                    timestamp++;
                }


                final CharBuf addressBuf = addressCreatorBufRef.get();

                addressBuf.recycle();

                addressBuf.add(objectAddress).add("/").add(method.getName());

                final String address = addressBuf.toString();


                final MethodCall<Object> call =
                        factory.createMethodCallToBeEncodedAndSent(messageId, address,
                                returnAddress, serviceName, method.getName(), timestamp, args, null);

                endPoint.call(call);
                return null;
            }
        };



        if (port == 0) {
            return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                    new Class[]{serviceInterface, ClientProxy.class}, invocationHandler);
        } else {
            return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                    new Class[]{serviceInterface, RemoteTCPClientProxy.class}, invocationHandler);
        }


    }

    @Override
    public <T> T createProxy(Class<T> serviceInterface, String serviceName, EndPoint endPoint) {
        return createProxyWithReturnAddress(serviceInterface, serviceName, "local", 0, new AtomicBoolean(true), "", endPoint);
    }
}

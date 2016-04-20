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
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.client.RemoteTCPClientProxy;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.service.EndPoint;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


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

        return new BoonInvocationHandlerForEndPoint(generatedMessageId, serviceInterface, serviceName, host, port, connected, endPoint,
                beforeMethodSent, objectAddress, returnAddress, addressCreatorBufRef);
    }


    @Override
    public <T> T createProxy(Class<T> serviceInterface, String serviceName, EndPoint endPoint, BeforeMethodSent beforeMethodSent) {
        return createProxyWithReturnAddress(serviceInterface, serviceName, "local", 0, new AtomicBoolean(true), "", endPoint, beforeMethodSent);
    }



}

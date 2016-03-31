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

package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.dispatchers.RoundRobinServiceWorkerBuilder;
import io.advantageous.qbit.service.dispatchers.ServiceMethodDispatcher;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A service bundle is a collection of services.
 * The service bundle does the routing of calls based on addresses
 * to a particular client.
 * <p>
 * It can handle local and remote clients.
 * created by Richard on 9/26/14.
 *
 * @author rhightower
 */
public interface ServiceBundle extends EndPoint, Startable {

    String address();


    @SuppressWarnings("UnusedReturnValue")
    ServiceBundle addServiceObject(String address, Object object);


    @SuppressWarnings("UnusedReturnValue")
    default ServiceBundle addServiceQueue(String address, ServiceQueue serviceQueue) {

        throw new IllegalStateException("Not implemented");
    }

    default ServiceBundle addServiceConsumer(String address, Consumer<MethodCall<Object>> service) {
        this.addServiceObject(address, service);
        return this;
    }

    default ServiceBundle addRoundRobinService(final String address, final int numServices,
                                               final Supplier<Object> serviceInstanceSupplier) {

        RoundRobinServiceWorkerBuilder roundRobinServiceWorkerBuilder = RoundRobinServiceWorkerBuilder
                .roundRobinServiceWorkerBuilder().setWorkerCount(numServices);

        roundRobinServiceWorkerBuilder.setServiceObjectSupplier(serviceInstanceSupplier);

        ServiceMethodDispatcher serviceMethodDispatcher = roundRobinServiceWorkerBuilder.build();
        serviceMethodDispatcher.start();

        this.addServiceConsumer(address, serviceMethodDispatcher);
        return this;
    }


    ServiceBundle addService(Object object);


    Queue<Response<Object>> responses();

    SendQueue<MethodCall<Object>> methodSendQueue();

    void flushSends();


    void stop();

    List<String> endPoints();

    void startReturnHandlerProcessor(ReceiveQueueListener<Response<Object>> listener);

    default void startWebResponseReturnHandler(ReceiveQueueListener<Response<Object>> listener) {
    }

    void startReturnHandlerProcessor();

    default ServiceBundle startUpCallQueue() {
        return this;
    }


    default void start() {
        startUpCallQueue();
        startReturnHandlerProcessor();

    }


    default ServiceBundle startServiceBundle() {
        start();
        return this;
    }

    <T> T createLocalProxy(Class<T> serviceInterface, String myService);

    default <T> T createOneWayLocalProxy(Class<T> serviceInterface, String myService) {
        return createLocalProxy(serviceInterface, myService);
    }

}

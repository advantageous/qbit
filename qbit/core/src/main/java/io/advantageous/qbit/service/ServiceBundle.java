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
import io.advantageous.qbit.queue.QueueCallBackHandler;
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


    /**
     * Add a service to the bundle
     * @param serviceObject service object
     * @return this, fluent
     */
    ServiceBundle addService(Object serviceObject);

    /**
     * Add a service to the bundle with an address and one or more queue callback handlers.
     * @param address address
     * @param serviceObject serviceObject
     * @param queueCallBackHandlers queueCallBackHandlers
     * @return this, fluent
     */
    default ServiceBundle addServiceObjectWithQueueCallBackHandlers(String address, Object serviceObject,
                                                                   QueueCallBackHandler... queueCallBackHandlers) {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Add a service with no alias address, and one or more queue callback handlers.
     * @param serviceObject service object
     * @param queueCallBackHandlers one or more callback handlers
     * @return this, fluent
     */
    default ServiceBundle addServiceWithQueueCallBackHandlers(Object serviceObject, QueueCallBackHandler... queueCallBackHandlers) {
        throw new IllegalStateException("Not implemented");
    }


    /**
     * Add a service queue directly to the bundle under an address.
     * @param address address
     * @param serviceQueue service queue
     * @return this, fluent
     */
    @SuppressWarnings("UnusedReturnValue")
    default ServiceBundle addServiceQueue(final String address, final ServiceQueue serviceQueue) {

        throw new IllegalStateException("Not implemented");
    }

    /**
     * Add a method call consumer directly to the bundle.
     * @param address address of method call consumer
     * @param service method call consumer acting as a service.
     * @return this, fluent
     */
    default ServiceBundle addServiceConsumer(String address, Consumer<MethodCall<Object>> service) {
        this.addServiceObject(address, service);
        return this;
    }

    /**
     * Add many services using round robin (a service pool), under this address, using this supplier
     * @param address address to register the round robin service pool
     * @param numServices number of services that will be in the pool
     * @param serviceInstanceSupplier the supplier that will create the instances for the pool
     * @return this, fluent.
     */
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

    /**
     * Main response queue for this bundle.
     * @return main response queue.
     */
    Queue<Response<Object>> responses();

    /**
     * Main method send queue for this bundle.
     * The value returned is not thread safe.
     * You will want one value per thread. (ThreadLocal)
     * @return main send queue for this bundle.
     */
    SendQueue<MethodCall<Object>> methodSendQueue();

    /**
     * Flush outstanding sends to all services registered with this bundle.
     */
    void flushSends();

    /** Stop this bundle and all services registered. */
    void stop();

    /**
     * Get a list of addresses managed by this bundle.
     * @return list of object addresses.
     */
    List<String> endPoints();

    /**
     * Starts the return processor
     * @param listener starts the return processor and registers a ReceiveQueueListener.
     */
    void startReturnHandlerProcessor(ReceiveQueueListener<Response<Object>> listener);

    /**
     * Starts a WebResponse Return Handler used for service bundles that work in conjunction with service end points.
     * @param listener
     */
    default void startWebResponseReturnHandler(ReceiveQueueListener<Response<Object>> listener) {
    }

    /**
     * Start the return processor.
     */
    void startReturnHandlerProcessor();

    /**
     * Starts this bundle
     *
     * @return this, fluent
     */
    default ServiceBundle startUpCallQueue() {
        return this;
    }


    /**
     * starts this bundle buy calling startUpCallQueue and startReturnHandlerProcessor (no arg).
     */
    default void start() {
        startUpCallQueue();
        startReturnHandlerProcessor();

    }


    /**
     * Calls start
     * @return this, fluent.
     */
    default ServiceBundle startServiceBundle() {
        start();
        return this;
    }

    /**
     * Create a local proxy (local vs. remote websocket)
     * @param serviceInterface interface of proxy (can support Reakt Callbacks, QBit Callbacks, and return Reakt Promise)
     * @param serviceName name of service (must match a register object address of the service you are trying to call).
     * @param <T> T
     * @return new local proxy
     */
    <T> T createLocalProxy(Class<T> serviceInterface, String serviceName);

    /**
     * Used to create a proxy that only sends and has no returns
     * @param serviceInterface serviceInterface
     * @param myService myService
     * @param <T> T
     * @return new local proxy that is one way (no async returns)
     */
    default <T> T createOneWayLocalProxy(Class<T> serviceInterface, String myService) {
        return createLocalProxy(serviceInterface, myService);
    }

}

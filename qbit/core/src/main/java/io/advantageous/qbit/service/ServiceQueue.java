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

import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by Richard on 7/21/14.
 *
 * @author rhightower
 */
public interface ServiceQueue extends Stoppable, ServiceFlushable {

    Object service();

    /**
     * Queue so we can enqueue method calls onto a client.
     * A client forwardEvent queue is not thread safe. Every thread that uses this client, needs its own SendQueue
     * this allows us to batch calls.
     *
     * @return forwardEvent queue
     */
    SendQueue<MethodCall<Object>> requests();


    SendQueue<MethodCall<Object>> requestsWithAutoFlush(int flushInterval, TimeUnit timeUnit);

    /*
    Queue to forwardEvent events to the service.
     */
    SendQueue<Event<Object>> events();

    /**
     * Queue so we can receive method calls returns from a client
     *
     * @return receive queue
     */
    ReceiveQueue<Response<Object>> responses();

    /**
     * Name of the client
     *
     * @return name
     */
    String name();

    /**
     * Name of the client
     *
     * @return name
     */
    String address();


    /**
     * Stop the client.
     */
    void stop();


    ServiceQueue startCallBackHandler();

    /**
     * Return a list of addresses.
     *
     * @param address address
     * @return addresses
     */
    Collection<String> addresses(String address);


    <T> T createProxy(Class<T> serviceInterface);


    <T> T createProxyWithAutoFlush(Class<T> serviceInterface, int interval, TimeUnit timeUnit);


    <T> T createProxyWithAutoFlush(Class<T> serviceInterface, PeriodicScheduler periodicScheduler,
                                   int interval, TimeUnit timeUnit);

    void flush();


    default ServiceQueue start() {
        return this;
    }


    default ServiceQueue start(boolean joinEventManager) {
        return this;
    }
}

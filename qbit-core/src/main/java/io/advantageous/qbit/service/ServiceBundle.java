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

import java.util.List;

/**
 * A client bundle is a collection of services.
 * The client bundle does the routing of calls based on addresses to a particular client.
 * Created by Richard on 9/26/14.
 *
 * @author rhightower
 */
public interface ServiceBundle extends EndPoint {

    String address();

    void addService(String address, Object object);


    void addService(Object object);

    Queue<Response<Object>> responses();

    SendQueue<MethodCall<Object>> methodSendQueue();

    void flushSends();


    void stop();

    List<String> endPoints();

    void startReturnHandlerProcessor(ReceiveQueueListener<Response<Object>> listener);

    void startReturnHandlerProcessor();

    default void start() {
    }

    <T> T createLocalProxy(Class<T> serviceInterface, String myService);
}

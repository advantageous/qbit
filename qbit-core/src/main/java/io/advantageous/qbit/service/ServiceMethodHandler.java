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

import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;

import java.util.Collection;

/**
 * This is a plugin just for the piece that does the invocation.
 * QBit has a boon implementation of this that uses reflection.
 * One could, for example, plugin an implementation of this that used bytecode generation.
 * <p>
 * Created by Richard on 9/8/14.
 *
 * @author rhightower
 */
public interface ServiceMethodHandler extends ReceiveQueueListener<MethodCall<Object>> {

    void init(Object service, String rootAddress, String serviceAddress);

    Response<Object> receiveMethodCall(MethodCall<Object> methodCall);

    String address();


    String name();

    Collection<String> addresses();

    void initQueue(SendQueue<Response<Object>> responseSendQueue);

    void queueInit();

    void handleEvent(Event<Object> event);

    void queueStartBatch();
}

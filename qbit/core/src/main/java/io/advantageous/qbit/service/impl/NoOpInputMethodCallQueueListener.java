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

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.queue.ReceiveQueueListener;

/**
 * Default receive queue listener. Does nothing.
 * Created by Richard on 8/26/14.
 */
public class NoOpInputMethodCallQueueListener implements ReceiveQueueListener<MethodCall<Object>> {

    @Override
    public void receive(MethodCall<Object> item) {

    }

    @Override
    public void empty() {

    }

    @Override
    public void limit() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void idle() {

    }
}
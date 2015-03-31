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

package io.advantageous.qbit.service.impl.queuecallbacks;

import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.queue.QueueCallBackHandler;

/**
 * Created by rhightower on 2/10/15.
 */
public class DynamicQueueCallbackHandler implements QueueCallBackHandler {


    private final Object service;
    private final MethodAccess queueStartBatch;
    private final MethodAccess queueInit;
    private final MethodAccess queueEmpty;
    private final MethodAccess queueLimit;
    private final MethodAccess queueShutdown;
    private final MethodAccess queueIdle;


    public DynamicQueueCallbackHandler(Object service) {
        final ClassMeta<Class<?>> classMeta;

        this.service = service;
        classMeta = (ClassMeta<Class<?>>) ClassMeta.classMeta(service.getClass());

        queueLimit = classMeta.method("queueLimit");
        queueEmpty = classMeta.method("queueEmpty");
        queueShutdown = classMeta.method("queueShutdown");
        queueIdle = classMeta.method("queueIdle");
        queueInit = classMeta.method("queueInit");
        queueStartBatch = classMeta.method("queueStartBatch");
    }


    @Override
    public void queueLimit() {
        if (queueLimit != null) {
            queueLimit.invoke(service);
        }


    }

    @Override
    public void queueEmpty() {
        if (queueEmpty != null) {
            queueEmpty.invoke(service);
        }

    }

    @Override
    public void queueInit() {
        if (queueInit != null) {
            queueInit.invoke(this.service);
        }
    }

    @Override
    public void queueIdle() {
        if (queueIdle != null) {
            queueIdle.invoke(service);
        }
    }

    @Override
    public void queueShutdown() {
        if (queueShutdown != null) {
            queueShutdown.invoke(service);
        }

    }

    @Override
    public void queueStartBatch() {
        if (queueStartBatch != null) {
            queueStartBatch.invoke(service);
        }

    }


}

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

import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.queue.QueueCallBackHandler;

/**
 * @author rhightower on 2/10/15.
 */
public class AnnotationDrivenQueueCallbackHandler implements QueueCallBackHandler {

    public static final String QUEUE_CALLBACK_ANNOTATION_NAME = "QueueCallback";
    private final Object service;
    private MethodAccess queueStartBatch;
    private MethodAccess queueInit;
    private MethodAccess queueEmpty;
    private MethodAccess queueLimit;
    private MethodAccess queueShutdown;
    private MethodAccess queueIdle;

    public AnnotationDrivenQueueCallbackHandler(Object service) {

        ClassMeta<Class<?>> classMeta;

        classMeta = (ClassMeta<Class<?>>) ClassMeta.classMeta(service.getClass());

        this.service = service;

        final Iterable<MethodAccess> methods = classMeta.methods();

        for (MethodAccess methodAccess : methods) {
            if (methodAccess.hasAnnotation(QUEUE_CALLBACK_ANNOTATION_NAME)) {
                processAnnotationForMethod(methodAccess);
            }

        }

    }

    private void processAnnotationForMethod(final MethodAccess methodAccess) {
        final AnnotationData annotation = methodAccess.annotation(QUEUE_CALLBACK_ANNOTATION_NAME);
        final Object[] values = (Object[]) annotation.getValues().get("value");


        for (Object value : values) {
            final QueueCallbackType queueCallbackType = QueueCallbackType.valueOf(value.toString());

            switch (queueCallbackType) {
                case IDLE:
                    queueIdle = methodAccess;
                    break;
                case SHUTDOWN:
                    queueShutdown = methodAccess;
                    break;
                case LIMIT:
                    queueLimit = methodAccess;
                    break;
                case INIT:
                    queueLimit = methodAccess;
                    break;
                case START_BATCH:
                    queueStartBatch = methodAccess;
                    break;
                case EMPTY:
                    queueEmpty = methodAccess;
                    break;
                case DYNAMIC:
                    switch (methodAccess.name()) {
                        case "queueIdle":
                            queueIdle = methodAccess;
                            break;
                        case "queueShutdown":
                            queueShutdown = methodAccess;
                            break;
                        case "queueLimit":
                            queueLimit = methodAccess;
                            break;
                        case "queueInit":
                            queueInit = methodAccess;
                            break;
                        case "queueStartBatch":
                            queueIdle = methodAccess;
                            break;
                        case "queueEmpty":
                            queueEmpty = methodAccess;
                            break;
                    }
                    break;

            }
        }

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

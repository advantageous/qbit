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

import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.service.AfterMethodCall;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.ServiceMethodHandler;
import io.advantageous.qbit.system.QBitSystemManager;


public class ServiceQueueImpl extends BaseServiceQueueImpl {


    public ServiceQueueImpl(final String rootAddress,
                            final String serviceAddress,
                            final Object service,
                            final QueueBuilder requestQueueBuilder,
                            final QueueBuilder responseQueueBuilder,
                            final ServiceMethodHandler serviceMethodHandler,
                            final Queue<Response<Object>> responseQueue,
                            final boolean async,
                            final boolean handleCallbacks,
                            final QBitSystemManager systemManager,
                            final BeforeMethodCall beforeMethodCall,
                            final BeforeMethodCall beforeMethodCallAfterTransform,
                            final AfterMethodCall afterMethodCall,
                            final AfterMethodCall afterMethodCallAfterTransform,
                            final QueueCallBackHandler handler,
                            final CallbackManager callbackManager,
                            final BeforeMethodSent beforeMethodSent,
                            final EventManager eventManager,
                            final boolean joinEventManager) {
        super(rootAddress, serviceAddress, service, requestQueueBuilder, responseQueueBuilder,
                serviceMethodHandler, responseQueue,
                async, handleCallbacks, systemManager, beforeMethodCall, beforeMethodCallAfterTransform,
                afterMethodCall, afterMethodCallAfterTransform, handler, callbackManager, beforeMethodSent, eventManager,
                joinEventManager);
    }
}

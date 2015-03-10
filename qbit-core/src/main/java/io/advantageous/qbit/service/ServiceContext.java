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

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.service.impl.BaseServiceQueueImpl;

/**
 * Created by rhightower on 2/4/15.
 */
public class ServiceContext {


    static final ServiceContext serviceContext = new ServiceContext();

    public static ServiceContext serviceContext() {
        return serviceContext;
    }


    /**
     * The only time this is valid is during queueInit.
     * This allows a service to get at its Service interface.
     *
     * @return current service queue
     */
    public ServiceQueue currentService() {
        return BaseServiceQueueImpl.currentService();
    }

    public EventManager eventManager() {
        return QBit.factory().systemEventManager();
    }

    public void joinEventManager() {

        final EventManager eventManager = eventManager();
        ServiceQueue serviceQueue = currentService();
        eventManager.joinService(serviceQueue);
    }

    public <T> void send(String channel, T message) {
        eventManager().send(channel, message);
    }
}

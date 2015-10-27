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

package io.advantageous.qbit.boon.events.impl;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventManagerFactory;
import io.advantageous.qbit.service.stats.StatsCollector;

import java.util.Objects;


public class BoonEventManagerFactory implements EventManagerFactory {

    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    Object context = Sys.contextToHold();

    @Override
    public EventManager createEventManager(String name, EventConnector eventConnector, StatsCollector statsCollector) {

        if (Objects.isNull(name)) throw new IllegalArgumentException("Name cannot be null");
        if (Objects.isNull(eventConnector)) throw new IllegalArgumentException("EventConnector cannot be null");
        if (Objects.isNull(statsCollector)) throw new IllegalArgumentException("Stats collector cannot be null");
        return new BoonEventManager(name, eventConnector, statsCollector);

    }
}

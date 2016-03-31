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

package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventBus;
import io.advantageous.qbit.events.EventListener;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.stats.StatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rhightower
 *         on 2/3/15.
 */
public class EventBusImpl implements EventBus {


    final Map<String, ChannelManager<Object>> channelMap = new ConcurrentHashMap<>(20);
    private final Logger logger = LoggerFactory.getLogger(EventBusImpl.class);
    private final EventConnector eventConnector;
    private final StatsCollector stats;
    private final String name;
    long messageCounter = 0;

    public EventBusImpl(final String name,
                        final EventConnector eventConnector,
                        final StatsCollector statsCollector) {
        this.name = name;
        this.eventConnector = eventConnector;
        this.stats = statsCollector;
    }

    @Override
    public <T> void register(String channelName, EventListener<T> listener) {
        //noinspection unchecked
        channel(channelName).add((EventListener<Object>) listener);
    }

    private ChannelManager<Object> channel(String channelName) {
        ChannelManager<Object> channelManager = channelMap.get(channelName);

        if (channelManager == null) {

            //noinspection unchecked
            channelManager = new ChannelManager(name, channelName, stats);
            channelMap.put(channelName, channelManager);
        }
        return channelManager;
    }

    @Override
    public <T> void send(String channel, T event) {

        messageCounter++;
        final EventTransferObject<Object> eventMessage = new EventTransferObject<>(event, messageCounter, channel);

        try {
            eventConnector.forwardEvent(eventMessage);
        } catch (Exception ex) {
            logger.error("EventBus " + name + " :: Error sending event " + eventMessage, ex);
        }

        channel(channel).send(eventMessage);

    }

    @Override
    public <T> void unregister(String channelName, EventListener<T> listener) {
        //noinspection unchecked
        channel(channelName).remove((EventListener<Object>) listener);
    }

    @Override
    public void forwardEvent(final EventTransferObject<Object> event) {

        if (!event.wasReplicated()) {
            eventConnector.forwardEvent(event);
        }
        channel(event.channel()).send(event);
    }

    @Override
    public void flush() {
        eventConnector.flush();
        ServiceProxyUtils.flushServiceProxy(eventConnector);
    }
}

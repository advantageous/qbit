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
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.EventListener;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.ServiceProxyUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rhightower
 * on 2/3/15.
 */
public class EventBusImpl implements EventBus {

    Map<String, Channel<Object>> channelMap = new ConcurrentHashMap<>(20);
    long messageCounter = 0;


    private final EventConnector eventConnector;

    public EventBusImpl(EventConnector eventConnector) {
        this.eventConnector = eventConnector;
    }

    public EventBusImpl() {
        this.eventConnector = event -> {};
    }

    @Override
    public <T> void register(String channelName, EventListener<T> listener) {
        channel(channelName).add( (EventListener<Object>)  ((Object) listener));
    }

    private Channel<Object> channel(String channelName) {
        Channel<Object> channel = channelMap.get(channelName);

        if (channel == null) {

            channel = new Channel(channelName);
            channelMap.put(channelName, channel);
        }
        return channel;
    }

    @Override
    public <T> void send(String channel, T event) {

        messageCounter++;
        final EventTransferObject<Object> eventMessage = new EventTransferObject<>(event, messageCounter, channel);
        eventConnector.forwardEvent(eventMessage);
        channel(channel).send(eventMessage);

    }

    @Override
    public <T> void unregister(String channelName, EventListener<T> listener) {
        channel(channelName).remove( (EventListener<Object>) ((Object) listener));
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

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rhightower on 2/3/15.
 */
public class EventBusImpl implements EventBus {

    Map<String, Channel> channelMap = new ConcurrentHashMap<>(20);
    long messageCounter = 0;

    @Override
    public <T> void register(String channelName, EventListener<T> listener) {
        channel(channelName).add(listener);


    }

    private Channel channel(String channelName) {
        Channel channel = channelMap.get(channelName);

        if (channel == null) {

            channel = new Channel(channelName);
            channelMap.put(channelName, channel);
        }
        return channel;
    }

    @Override
    public <T> void send(String channel, T event) {

        messageCounter++;
        channel(channel).send(new EventImpl<T>(event, messageCounter, channel));

    }

    @Override
    public <T> void unregister(String channelName, EventListener<T> listener) {
        channel(channelName).remove(listener);

    }
}

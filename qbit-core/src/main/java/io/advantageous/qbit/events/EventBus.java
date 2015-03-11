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

package io.advantageous.qbit.events;

import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.ServiceFlushable;

/**
 * Created by rhightower on 2/3/15.
 */
public interface EventBus extends ServiceFlushable{

    /**
     * This method can only be called outside of a service.
     *
     * @param channelName array of channel names
     * @param listener event listener
     * @param <T> T
     */
    <T> void register(String channelName, EventListener<T> listener);


    /**
     * @param channel channel
     * @param event   event
     * @param <T>     T
     */
    <T> void send(String channel, T event);


    /**
     * This method can only be called outside of a service.
     *
     * @param channelName array of channel names
     * @param listener event listener
     * @param <T> T
     */
    <T> void unregister(String channelName, EventListener<T> listener);


    void forwardEvent(EventTransferObject<Object> event);
}

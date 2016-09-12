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

import static io.advantageous.qbit.service.ServiceContext.serviceContext;

import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.service.ServiceQueue;

/**
 * Manages an event bus.
 * Event Manager for managing event buses.
 */
public interface EventManager {

    /**
     * ONLY FOR SERVICES
     * Joins current event manager.
     * This method can only be called from inside a service.
     * It registers for all of the events.
     * It looks for the @Listen annotation to register for the current service.
     * This is usually called in queueInit.
     * EVENTS COME IN ON THE SAME QUEUE AS THE METHOD CALLS
     * (or another queue managed by the same thread depending on the implementation).
     *
     * @param serviceQueue service queue
     */
    void joinService(ServiceQueue serviceQueue);


    default void leaveEventBus(ServiceQueue serviceQueue) {

    }

    default void joinServices(ServiceQueue... serviceQueues) {

        for (ServiceQueue serviceQueue : serviceQueues) {
            joinService(serviceQueue);
        }
    }


    /**
     * Opposite of join. ONLY FOR SERVICES.
     */
    default void leave() {
        final ServiceQueue serviceQueue = serviceContext().currentService();
        leaveEventBus(serviceQueue);
    }

    /**
     * This method can be called outside of a service.
     * It looks for the @Listen annotation to register for certain events.
     *
     * @param listener listener
     */
    void listen(Object listener);


    /**
     * Opposite of register.
     *
     * @param listener listener
     */
    void stopListening(Object listener);


    /**
     * This method can only be called outside of a service.
     *
     * @param channelName array of channel names
     * @param listener    event listener
     * @param <T>         T
     */
    <T> void register(String channelName, EventListener<T> listener);

    /**
     * This method can only be called outside of a service.
     *
     * @param channelName array of channel names
     * @param listener    event listener
     * @param <T>         T
     */
    <T> void unregister(String channelName, EventListener<T> listener);


    /**
     * @param channel channel
     * @param event   event
     * @param <T>     T
     */
    <T> void send(String channel, T event);


    /**
     * @param channel channel
     * @param event   event
     * @param <T>     T
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    <T> void sendArray(String channel, T... event);


    /**
     * Sends object as arguments to a channel method.
     *
     * @param channel channel
     * @param event   event
     * @param <T>     T
     */
    @SuppressWarnings("unchecked")
    <T> void sendArguments(String channel, T... event);

    /**
     * Copies the state of the object and sends this instead of actual object to
     * avoid thread sync issues with object data.
     *
     * @param channel channel
     * @param event   event
     * @param <T>     T
     */
    default <T> void sendCopy(String channel, T event) {
                                   
        T copy = BeanUtils.copy(event);
        this.send(channel, copy);
    }


    void forwardEvent(EventTransferObject<Object> event);

}

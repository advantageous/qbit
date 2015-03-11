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
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceQueue;

/**
 * Event Manager for managing event busses.
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
     * @param serviceQueue service queue
     */
    void joinService(ServiceQueue serviceQueue);


    default void joinServices(ServiceQueue... serviceQueues) {

        for (ServiceQueue serviceQueue : serviceQueues) {
            joinService(serviceQueue);
        }
    }



    /**
     * Opposite of join. ONLY FOR SERVICES.
     */
    void leave();

    /**
     * This method can be called outside of a service.
     * It looks for the @Listen annotation to register for certain events.
     * @param listener listener
     */
    void listen(Object listener);


    /**
     * Opposite of register.
     * @param listener listener
     */
    void stopListening(Object listener);


    /**
     * This method can only be called outside of a service.
     *
     * @param channelName array of channel names
     * @param listener event listener
     * @param <T> T
     */
    <T> void register(String channelName, EventListener<T> listener);

    /**
     * This method can only be called outside of a service.
     *
     * @param channelName array of channel names
     * @param listener event listener
     * @param <T> T
     */
    <T> void unregister(String channelName, EventListener<T> listener);


    /**
     * This method can only be called outside of a service.
     * Registers an output queue to a list of channel names.
     *
     * @param channelName array of channel names
     * @param listener queue that is going to listen
     * @param <T> T
     */
    <T> void subscribe(String channelName, SendQueue<Event<Object>> listener);


    /**
     * This method can only be called outside of a service.
     * Registers an output queue to a consume for P2P style messaging.
     *
     * @param channelName array of channel names
     *
     * @param channelName array of channel names
     * @param listener queue that is going to consume
     * @param <T>     T
     */
    <T> void consume(String channelName, SendQueue<Event<Object>> listener);


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
    <T> void sendArray(String channel, T... event);

    /**
     * Copies the state of the object and sends this instead of actual object to
     * avoid thread sync issues with object data.
     *
     * @param channel channel
     * @param event   event
     * @param <T>     T
     */
    <T> void sendCopy(String channel, T event);



    <T> void forwardEvent(EventTransferObject<Object> event);

}

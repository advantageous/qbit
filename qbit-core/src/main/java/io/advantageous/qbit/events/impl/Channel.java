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

import io.advantageous.qbit.events.EventListener;
import io.advantageous.qbit.message.Event;
import org.boon.Boon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhightower on 2/3/15.
 */
public class Channel<T> {


    private final Logger logger = LoggerFactory.getLogger(Channel.class);

    private final boolean debug = logger.isDebugEnabled();

    private final String name;
    private final List<EventListener<T>> listeners;

    private EventListener<T> consumer;

    public Channel(String name) {
        this.name = name;
        listeners = new ArrayList<>();
        consumer = new NoOpEventConsumer<>();

    }

    public void add(EventListener<T> eventListener) {

        if (eventListener.subscriber()) {
            listeners.add(eventListener);
        } else {
            consumer = eventListener;
        }
    }


    public void remove(EventListener<T> eventListener) {

        if (eventListener.subscriber()) {
            listeners.remove(eventListener);
        } else {

            if (consumer == eventListener) {
                consumer = new NoOpEventConsumer<>();
            }
        }
    }

    public void send(Event<T> event) {


        for (EventListener<T> listener : listeners) {
            try {
                listener.listen(event);
            } catch (Exception ex) {
                logger.error(Boon.sputs("Unable to send event for Channel", name), ex);
            }
        }

        consumer.listen(event);
    }


}

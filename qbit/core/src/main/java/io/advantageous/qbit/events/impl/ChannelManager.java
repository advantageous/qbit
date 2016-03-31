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
import io.advantageous.qbit.service.stats.StatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a channel. A channel is like a communication pipe
 *
 * @param <T> type of channel
 * @author rick hightower
 */
public class ChannelManager<T> {

    private final Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    private final boolean debug = logger.isDebugEnabled();
    private final String name;
    private final List<EventListener<T>> listeners;
    private final StatsCollector stats;
    private final String eventBusName;
    private EventListener<T> consumer;
    private int sentCount;
    private String sentCountStatKey;

    public ChannelManager(final String eventBusName, final String name, StatsCollector statsCollector) {
        logger.info("Channel {} was created", name);
        this.name = name;
        consumer = new NoOpEventConsumer<>();
        this.stats = statsCollector;
        this.eventBusName = eventBusName;

        listeners = new ArrayList<>();

        sentCountStatKey = eventBusName + "." + name;


    }

    /**
     * Add an event listener to the channel
     * There can only be one consumer and many listeners.
     *
     * @param eventListener eventListener
     */
    public void add(final EventListener<T> eventListener) {
        if (eventListener.subscriber()) {
            if (debug) logger.debug("subscription to channel <> from <> ", name, eventListener);
            listeners.add(eventListener);
            stats.recordLevel(eventBusName + "::" + name, listeners.size());
        } else {
            if (debug) logger.debug("consumer to channel <> from <> ", name, eventListener);
            consumer = eventListener;
        }
    }


    /**
     * Remove an event listener from the channel.
     * <p>
     * There can only be one consumer and many listeners.
     *
     * @param eventListener eventListener
     */
    public void remove(EventListener<T> eventListener) {

        if (eventListener.subscriber()) {
            logger.info("remove subscription to channel {} from {} ", name, eventListener);
            listeners.remove(eventListener);
            stats.recordLevel(eventBusName + "::" + name, listeners.size());
        } else {

            if (consumer == eventListener) {
                logger.info("remove consumer to channel {} from {} ", name, eventListener);
                consumer = new NoOpEventConsumer<>();
            }
        }
    }

    public void send(final Event<T> event) {

        if (debug) logger.debug("ChannelManager::send() {}", event);


        sentCount++;
        if (sentCount > 20) {
            stats.recordCount(sentCountStatKey, sentCount);
            sentCount = 0;
        }


        for (EventListener<T> listener : listeners) {
            try {
                listener.listen(event);
            } catch (Exception ex) {
                logger.error("Unable to send event for Channel" + name, ex);
            }
        }

        consumer.listen(event);
    }


}

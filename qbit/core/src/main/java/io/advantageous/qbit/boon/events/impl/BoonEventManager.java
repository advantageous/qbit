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

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.events.*;
import io.advantageous.qbit.events.EventListener;
import io.advantageous.qbit.events.impl.EventBusImpl;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.reflection.ClassMeta.classMeta;
import static io.advantageous.qbit.annotation.AnnotationUtils.*;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;

/**
 * @author rhightower
 *         on 2/3/15.
 */
public class BoonEventManager implements EventManager {


    private final Logger logger = LoggerFactory.getLogger(BoonEventManager.class);

    private final EventBus eventBus;
    private final Map<String, List<Object>> eventMap = new ConcurrentHashMap<>();
    private final List<SendQueue<Event<Object>>> queuesToFlush = new ArrayList<>(100);
    private final HashSet<ServiceQueue> services = new HashSet<>();
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final String name;
    private final StatsCollector stats;
    private final String eventCountStatsKey;
    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    Object context = Sys.contextToHold();
    private int messageCountSinceLastFlush = 0;
    private long flushCount = 0;
    private long lastFlushTime = 0;
    private long now;


    public BoonEventManager(final String name, EventConnector eventConnector, StatsCollector statsCollector) {

        logger.info("Event manager created {} {} {}", name, eventConnector, statsCollector);
        this.name = name;
        this.eventBus = new EventBusImpl(name, eventConnector, statsCollector);
        this.stats = statsCollector;

        eventCountStatsKey = "EventManager." + name.replace(" ", ".");

    }


    private void sendMessages() {

        flushCount++;
        lastFlushTime = now;

        stats.recordCount(eventCountStatsKey, messageCountSinceLastFlush);
        messageCountSinceLastFlush = 0;

        if (debug) {
            logger.debug("EventManager {} flushCount {}", name, flushCount);
        }


        final Set<Map.Entry<String, List<Object>>> entries = eventMap.entrySet();
        for (Map.Entry<String, List<Object>> entry : entries) {
            String channelName = entry.getKey();
            final List<Object> events = entry.getValue();
            for (Object event : events) {
                eventBus.send(channelName, event);
            }
            events.clear();
        }

        //noinspection Convert2streamapi
        for (SendQueue<Event<Object>> sendQueue : queuesToFlush) {
            sendQueue.flushSends();
        }
    }

    @QueueCallback(QueueCallbackType.IDLE)
    private void queueIdle() {

        now = Timer.timer().now();

        if (messageCountSinceLastFlush > 0) {

            sendMessages();
            return;
        }

        eventBus.flush();

    }

    @QueueCallback(QueueCallbackType.LIMIT)
    private void queueLimit() {


        if (messageCountSinceLastFlush > 100_000) {

            now = Timer.timer().now();
            logger.debug("EventManager {}: Sending all " +
                    "messages because we have more than 100K", name);
            sendMessages();
            return;
        }

        now = Timer.timer().now();
        long duration = now - lastFlushTime;

        if (duration > 50 && messageCountSinceLastFlush > 0) {
            if (debug) {
                logger.debug("EventManager {}: Sending all " +
                        "messages because 50 MS elapsed and we have more than 0", name);
            }
            sendMessages();
        }

        eventBus.flush();

    }


    @QueueCallback(QueueCallbackType.EMPTY)
    private void queueEmpty() {


        if (messageCountSinceLastFlush > 100) {

            now = Timer.timer().now();
            sendMessages();
            return;
        }


        now = Timer.timer().now();
        long duration = now - lastFlushTime;

        if (duration > 20 && messageCountSinceLastFlush > 0) {
            sendMessages();
        }

        eventBus.flush();

    }


    @Override
    public void joinService(final ServiceQueue serviceQueue) {


        if (services.contains(serviceQueue)) {
            logger.info("EventManager{}::joinService: Service queue " +
                    "is already a member of this event manager {}", name, serviceQueue.name());
            return;
        }


        services.add(serviceQueue);


        logger.info("EventManager{}::joinService::  {} joined {}", name, serviceQueue.name(), name);


        doListen(serviceQueue.service(), serviceQueue);
    }


    @Override
    public void leave() {
        final ServiceQueue serviceQueue = serviceContext().currentService();
        leaveEventBus(serviceQueue);
    }


    @Override
    public void leaveEventBus(final ServiceQueue serviceQueue) {
        if (serviceQueue == null) {
            throw new IllegalStateException(String.format("EventManager %s:: Must be called from inside of a Service", name));
        }
        stopListening(serviceQueue.service());
        services.remove(serviceQueue);
    }

    @Override
    public void listen(final Object listener) {
        doListen(listener, null);

    }


    private void doListen(final Object listener, final ServiceQueue serviceQueue) {

        if (debug) {
            logger.debug("EventManager {}  registering listener {} with serviceQueue {}",
                    name, listener, serviceQueue);
        }
        final ClassMeta<?> listenerClassMeta = ClassMeta.classMeta(listener.getClass());
        final Iterable<MethodAccess> listenerMethods = listenerClassMeta.methods();


        /* Add methods as listeners if they have a listen annotation. */
        for (final MethodAccess methodAccess : listenerMethods) {
            AnnotationData listenAnnotationData = getListenAnnotation(methodAccess);
            if (listenAnnotationData == null) continue;
            extractEventListenerFromMethod(listener, methodAccess, listenAnnotationData, serviceQueue);
        }


        /* Look for listener channel implementations. */
        final Class<?>[] interfacesFromListener = listenerClassMeta.cls().getInterfaces();

        /* Iterate through interfaces and see if any are marked with the event channel annotation. */
        for (Class<?> interfaceClass : interfacesFromListener) {
            final ClassMeta<?> metaFromListenerInterface = classMeta(interfaceClass);

            final AnnotationData eventChannelAnnotation = metaFromListenerInterface
                    .annotation(AnnotationUtils.EVENT_CHANNEL_ANNOTATION_NAME);
            if (eventChannelAnnotation == null) {
                continue;
            }

            /* If we got this far, then we are dealing with an event channel interface
            so register the methods from this interface as channel listeners.
             */
            final Iterable<MethodAccess> interfaceMethods = metaFromListenerInterface.methods();

            final String classEventBusName = getClassEventChannelName(metaFromListenerInterface, eventChannelAnnotation);


            for (MethodAccess methodAccess : interfaceMethods) {


                /* By default the method name forms part of the event bus name,
                but this can be overridden by the EVENT_CHANNEL_ANNOTATION_NAME annotation on the method.
                 */
                final AnnotationData methodAnnotation = methodAccess.annotation(AnnotationUtils.EVENT_CHANNEL_ANNOTATION_NAME);

                String methodEventBusName = methodAnnotation != null && methodAnnotation.getValues().get("value") != null
                        ? methodAnnotation.getValues().get("value").toString() : null;

                if (Str.isEmpty(methodEventBusName)) {
                    methodEventBusName = methodAccess.name();
                }

                final String channelName = createChannelName(null, classEventBusName, methodEventBusName);


                if (serviceQueue == null) {
                    extractListenerForRegularObject(listener, methodAccess, channelName, false);
                } else {
                    extractListenerForService(serviceQueue, channelName, false);
                }
            }


        }
    }


    private void extractEventListenerFromMethod(final Object listener,
                                                final MethodAccess methodAccess,
                                                final AnnotationData listen,
                                                final ServiceQueue serviceQueue) {

        logger.info("EventManager {} ::extractEventListenerFromMethod  :: " +
                        "{} is listening with method {} using annotation data {} ",
                name, serviceQueue, methodAccess.name(), listen.getValues());

        final String channel = listen.getValues().get("value").toString();

        if (Str.isEmpty(channel)) {
            return;
        }
        final boolean consume = (boolean) listen.getValues().get("consume");


        if (serviceQueue == null) {
            extractListenerForRegularObject(listener, methodAccess, channel, consume);
        } else {
            extractListenerForService(serviceQueue, channel, consume);
        }
    }


    private void extractListenerForService(final ServiceQueue serviceQueue,
                                           final String channel,
                                           final boolean consume) {


        logger.info("EventManager {}:: {} is listening on channel {} and is consuming? {}",
                name, serviceQueue.name(), channel, consume);

        if (consume) {
            this.consume(channel, serviceQueue);
        } else {
            this.subscribe(channel, serviceQueue);
        }
    }


    @SuppressWarnings("Convert2Lambda")
    private void extractListenerForRegularObject(final Object listener,
                                                 final MethodAccess methodAccess,
                                                 final String channel,
                                                 final boolean consume) {

        logger.info("EventManager {}:: {} is listening with method {} on channel {} and is consuming? {}",
                name, listener.getClass().getSimpleName(), methodAccess.name(), channel, consume);
        if (consume) {


            /* Do not use Lambda, this has to be a consume! */
            this.register(channel, new EventConsumer<Object>() {
                @Override
                public void listen(Event<Object> event) {

                    invokeEventMethod(event, methodAccess, listener);
                }
            });
        } else {
            /* Do not use Lambda, this has to be a subscriber! */
            this.register(channel, new EventSubscriber<Object>() {
                @Override
                public void listen(Event<Object> event) {

                    invokeEventMethod(event, methodAccess, listener);
                }
            });
        }
    }

    private void invokeEventMethod(Event<Object> event, MethodAccess methodAccess, Object listener) {
        if (event.body() instanceof Object[]) {
            methodAccess.invokeDynamic(listener, (Object[]) event.body());
        } else if (event.body() instanceof List) {
            final List body = (List) event.body();
            methodAccess.invokeDynamic(listener, body.toArray(new Object[body.size()]));

        } else {
            methodAccess.invokeDynamic(listener, event.body());
        }
    }

    @Override
    public void stopListening(Object listener) {
        final ClassMeta<?> classMeta = ClassMeta.classMeta(listener.getClass());
        final Iterable<MethodAccess> methods = classMeta.methods();

        for (final MethodAccess methodAccess : methods) {
            final AnnotationData listen = getListenAnnotation(methodAccess);
            if (listen == null) continue;
            stopListeningToMethodEventListeners(listener, methodAccess, listen);
        }

    }

    private void stopListeningToMethodEventListeners(Object listener, MethodAccess methodAccess, AnnotationData listen) {
        //I don't know how to do this yet. PUNT.
    }

    @Override
    public <T> void register(String channelName, EventListener<T> listener) {
        eventBus.register(channelName, listener);
    }

    @Override
    public <T> void unregister(String channelName, EventListener<T> listener) {
        eventBus.unregister(channelName, listener);
    }

    @SuppressWarnings("Convert2Lambda")
    private void subscribe(final String channelName, final ServiceQueue serviceQueue) {

        final SendQueue<Event<Object>> sendQueue = serviceQueue.events();

        logger.info("EventManager {}::subscribe() channel name {} sendQueue {}", name, channelName, sendQueue.name());

        queuesToFlush.add(sendQueue);

        final AtomicReference<EventSubscriber<Object>> ref = new AtomicReference<>();

        final EventSubscriber<Object> eventConsumer = new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {

                if (!serviceQueue.running()) {
                    eventBus.unregister(channelName, ref.get());
                    queuesToFlush.remove(sendQueue);
                }
                sendQueue.send(event);
            }
        };

        ref.set(eventConsumer);
        eventBus.register(channelName, eventConsumer);

    }

    @SuppressWarnings("Convert2Lambda")
    private void consume(final String channelName, final ServiceQueue serviceQueue) {

        final SendQueue<Event<Object>> sendQueue = serviceQueue.events();

        logger.info("EventManager {}::consume() channel name {} sendQueue {}", name, channelName, sendQueue.name());

        queuesToFlush.add(sendQueue);

        final AtomicReference<EventConsumer<Object>> ref = new AtomicReference<>();

        final EventConsumer<Object> eventConsumer = new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {

                if (!serviceQueue.running()) {
                    eventBus.unregister(channelName, ref.get());
                    queuesToFlush.remove(sendQueue);
                }
                sendQueue.send(event);
            }
        };

        ref.set(eventConsumer);
        eventBus.register(channelName, eventConsumer);

    }

    @Override
    public <T> void send(final String channel, T event) {
        messageCountSinceLastFlush++;
        events(channel).add(event);
    }

    @SafeVarargs
    @Override
    public final <T> void sendArray(final String channel, T... event) {
        this.send(channel, event);
    }

    @SafeVarargs
    @Override
    public final <T> void sendArguments(final String channel, T... event) {
        this.send(channel, event);
    }


    private List<Object> events(String channel) {
        List<Object> events = this.eventMap.get(channel);

        if (events == null) {
            events = new ArrayList<>(100);
            this.eventMap.put(channel, events);
        }

        return events;
    }

    @Override
    public <T> void sendCopy(String channel, T event) {

        T copy = BeanUtils.copy(event);
        this.send(channel, copy);
    }

    @Override
    public void forwardEvent(final EventTransferObject<Object> event) {
        messageCountSinceLastFlush++;
        eventBus.forwardEvent(event);
    }

    @Override
    public String toString() {
        return name + " " + super.toString();
    }
}

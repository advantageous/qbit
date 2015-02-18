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

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.events.*;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.util.Timer;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.BeanUtils;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.qbit.annotation.AnnotationUtils.getListenAnnotation;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;
import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/3/15.
 */
public class BoonEventManager implements EventManager {

    private final EventBus eventBus = new EventBusImpl();
    private final Map<String, List<Object>> eventMap = new ConcurrentHashMap<>();
    private final List<SendQueue<Event<Object>>> queuesToFlush = new ArrayList<>(100);
    private final boolean debug = GlobalConstants.DEBUG;
    private int messageCountSinceLastFlush = 0;
    private long flushCount = 0;
    private long lastFlushTime = 0;
    private long now;

    private void sendMessages() {

        flushCount++;
        lastFlushTime = now;
        messageCountSinceLastFlush = 0;

        if (debug) {
            puts("BoonEventManager flushCount", flushCount);
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

    }

    @QueueCallback(QueueCallbackType.LIMIT)
    private void queueLimit() {


        if (messageCountSinceLastFlush > 100_000) {

            now = Timer.timer().now();
            sendMessages();
            return;
        }

        now = Timer.timer().now();
        long duration = now - lastFlushTime;

        if (duration > 50 && messageCountSinceLastFlush > 0) {
            sendMessages();
        }

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

        if (duration > 50 && messageCountSinceLastFlush > 0) {
            sendMessages();
        }

    }


    @Override
    public void joinService(Service service) {

        if (debug) {

            puts("Joined", service);
        }

        if (service == null) {
            throw new IllegalStateException("Must be called from inside of a Service");
        }

        doListen(service.service(), service);
    }


    @Override
    public void leave() {

        final Service service = serviceContext().currentService();
        if (service == null) {
            throw new IllegalStateException("Must be called from inside of a Service");
        }


        stopListening(service.service());
    }


    @Override
    public void listen(final Object listener) {
        doListen(listener, null);

    }

    private void doListen(final Object listener, final Service service) {

        if (debug) {
            puts("BoonEventManager registering listener", listener, service);
        }
        final ClassMeta<?> classMeta = ClassMeta.classMeta(listener.getClass());
        final Iterable<MethodAccess> methods = classMeta.methods();

        for (final MethodAccess methodAccess : methods) {
            AnnotationData listen = getListenAnnotation(methodAccess);

            if (listen == null) continue;
            extractEventListenerFromMethod(listener, methodAccess, listen, service);
        }
    }


    private void extractEventListenerFromMethod(final Object listener, final MethodAccess methodAccess, final AnnotationData listen, final Service service) {
        final String channel = listen.getValues().get("value").toString();
        final boolean consume = (boolean) listen.getValues().get("consume");


        if (service == null) {
            extractListenerForRegularObject(listener, methodAccess, channel, consume);
        } else {
            extractListenerForService(service, methodAccess, channel, consume);
        }
    }


    private void extractListenerForService(Service service, final MethodAccess methodAccess, final String channel, final boolean consume) {

        final SendQueue<Event<Object>> events = service.events();
        if (consume) {

            this.subscribe(channel, events);
        } else {

            this.consume(channel, events);
        }
    }


    private void extractListenerForRegularObject(final Object listener, final MethodAccess methodAccess, final String channel, final boolean consume) {
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

    @Override
    public <T> void subscribe(final String channelName, final SendQueue<Event<Object>> sendQueue) {

        queuesToFlush.add(sendQueue);

        eventBus.register(channelName, new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                sendQueue.send(event);
            }
        });
    }

    @Override
    public <T> void consume(final String channelName, final SendQueue<Event<Object>> sendQueue) {


        queuesToFlush.add(sendQueue);

        eventBus.register(channelName, new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                sendQueue.send(event);
            }
        });

    }

    @Override
    public <T> void send(final String channel, T event) {
        messageCountSinceLastFlush++;
        events(channel).add(event);
    }

    @Override
    public <T> void sendArray(String channel, T... event) {

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
    public <T> void sendCopy(String channel, Event<T> event) {

        Event<T> copy = BeanUtils.copy(event);
        this.send(channel, copy);
    }
}

package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.events.*;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceContext;
import io.advantageous.qbit.util.Timer;
import org.boon.core.reflection.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/3/15.
 */
public class BoonEventManager implements EventManager {

    private final EventBus eventBus = new EventBusImpl();
    private final Map<String, List<Object>> eventMap = new ConcurrentHashMap<>();
    private final List<SendQueue<Message<Object>>> queuesToFlush = new ArrayList<>(100);
    private int messageCountSinceLastFlush = 0;
    private long flushCount = 0;
    private long lastFlushTime = 0;

    private final boolean debug = GlobalConstants.DEBUG;

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

        for (SendQueue<Message<Object>> sendQueue : queuesToFlush) {
            sendQueue.flushSends();
        }
    }

    private void queueIdle() {

        now = Timer.timer().now();

        if (messageCountSinceLastFlush > 0) {

            sendMessages();
            return;
        }

    }

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
    public void join() {

        final Service service = ServiceContext.currentService();
        if (service == null) {
            throw new IllegalStateException("Must be called from inside of a Service");
        }

        listen(service.service());
    }


    @Override
    public void leave() {

        final Service service = ServiceContext.currentService();
        if (service == null) {
            throw new IllegalStateException("Must be called from inside of a Service");
        }


        stopListening(service.service());
    }


    /** */
    @Override
    public void listen(Object listener) {


    }

    @Override
    public void stopListening(Object listener) {


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
    public <T> void subscribe(final String channelName, final SendQueue<Message<Object>> sendQueue) {

        queuesToFlush.add(sendQueue);

        eventBus.register(channelName, new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                sendQueue.send(event);
            }
        });
    }

    @Override
    public <T> void consumer(final String channelName, final SendQueue<Message<Object>> sendQueue) {


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

        Event<T> copy =  BeanUtils.copy(event);
        this.send(channel, copy);
    }
}

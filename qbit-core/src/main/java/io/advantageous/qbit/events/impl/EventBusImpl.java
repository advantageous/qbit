package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventBus;
import io.advantageous.qbit.events.EventListener;
import io.advantageous.qbit.message.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rhightower on 2/3/15.
 */
public class EventBusImpl implements EventBus{

    Map<String, Channel> channelMap = new ConcurrentHashMap<>(20);
    long messageCounter = 0;

    @Override
    public <T> void register(String channelName, EventListener<T> listener) {
            channel(channelName).add(listener);


    }

    private Channel channel(String channelName) {
        Channel channel = channelMap.get(channelName);

        if (channel==null) {

            channel = new Channel(channelName);
            channelMap.put(channelName, channel);
        }
        return channel;
    }

    @Override
    public <T> void send(String channel, T event) {

        messageCounter++;
        channel(channel).send(new EventImpl<T>(event, messageCounter));

    }

    @Override
    public <T> void unregister(String channelName, EventListener<T> listener) {
        channel(channelName).remove(listener);

    }
}

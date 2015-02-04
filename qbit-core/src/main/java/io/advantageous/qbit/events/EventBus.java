package io.advantageous.qbit.events;

import io.advantageous.qbit.message.Event;

/**
 * Created by rhightower on 2/3/15.
 */
public interface EventBus {

    /**
     *
     * This method can only be called outside of a service.
     * @param channelName array of channel names
     */
    <T> void register(String channelName, EventListener<T> listener);



    /**
     *
     * @param channel channel
     * @param event event
     * @param <T> T
     */
    <T> void send(String channel, T event);


    /**
     *
     * This method can only be called outside of a service.
     * @param channelName array of channel names
     */
    <T> void unregister(String channelName, EventListener<T> listener);



}

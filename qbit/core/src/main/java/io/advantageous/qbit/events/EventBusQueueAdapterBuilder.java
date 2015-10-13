package io.advantageous.qbit.events;

import io.advantageous.qbit.queue.Queue;

public class EventBusQueueAdapterBuilder {


    private Queue queue;
    private EventManager eventManager;
    private String channel;

    public Queue getQueue() {
        return queue;
    }

    public <T> EventBusQueueAdapterBuilder setQueue(Queue<T> queue) {
        this.queue = queue;
        return this;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public EventBusQueueAdapterBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    public String getChannel() {
        return channel;
    }

    public EventBusQueueAdapterBuilder setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public <T> EventBusQueueAdapter<T> build() {
        return new EventBusQueueAdapter<> ((Queue<T>) getQueue(), getEventManager(), getChannel());
    }
}

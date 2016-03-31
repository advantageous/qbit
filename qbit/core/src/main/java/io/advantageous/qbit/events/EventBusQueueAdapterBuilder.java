package io.advantageous.qbit.events;

import io.advantageous.qbit.queue.Queue;

import java.util.function.Supplier;

public class EventBusQueueAdapterBuilder {


    /**
     * Queue queue.
     */
    private Queue queue;


    private Supplier<Queue> queueSupplier;

    /**
     * Event manager where we are pushing events here.
     */
    private EventManager eventManager;

    /**
     * Event channel where we send events from the queue to.
     */
    private String channel;

    public static EventBusQueueAdapterBuilder eventBusQueueAdapterBuilder() {
        return new EventBusQueueAdapterBuilder();
    }

    public Supplier<Queue> getQueueSupplier() {
        final Queue queue = this.queue;
        if (queueSupplier == null) {
            queueSupplier = new Supplier<Queue>() {
                @Override
                public Queue get() {
                    return queue;
                }
            };
        }
        return queueSupplier;
    }

    public EventBusQueueAdapterBuilder setQueueSupplier(final Supplier<Queue> queueSupplier) {
        this.queueSupplier = queueSupplier;
        return this;
    }

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
        return new EventBusQueueAdapter<>((Supplier<Queue<T>>) (Object) getQueueSupplier(), getEventManager(), getChannel());
    }
}

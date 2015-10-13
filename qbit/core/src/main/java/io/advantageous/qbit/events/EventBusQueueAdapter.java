package io.advantageous.qbit.events;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sends an item from a QBit queue to the event bus.
 * This can be started or the process method could be called periodically.
 * It can be used to channel events from Kafka, JMS, and/or Redis into the QBit world.
 */
public class EventBusQueueAdapter<T> implements Startable, Stoppable{

    private final Queue<T> queue;
    private final EventManager eventManager;
    private final String channel;
    private final Lock lock = new ReentrantLock();


    public EventBusQueueAdapter(final Queue<T> queue,
                                final EventManager eventManager,
                                final String channel) {
        this.queue = queue;
        this.eventManager = eventManager;
        this.channel = channel;
    }

    /**
     * Process. This can be called periodically and it will check to see if there are messages on the queue.
     */
    public void process() {

        final ReceiveQueue<T> receiveQueue = queue.receiveQueue();
        T item = receiveQueue.poll();
        while (item !=null) {

            sendToEventManager(item);
            item = receiveQueue.poll();
        }
    }

    private void sendToEventManager(T item) {
        try {
            if (lock.tryLock()) {
                eventManager.sendArguments(channel, item);
            } else {
                lock.lock();
                eventManager.sendArguments(channel, item);
            }
        }finally {
            lock.unlock();
        }
    }



    /**
     * Start listener. Once this is called messages can come in on a foreign thread.
     */
    public void start() {
        queue.startListener(this::sendToEventManager);
    }


    public void stop() {
        queue.stop();
    }
}

package io.advantageous.qbit.events;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Sends an item from a QBit queue to the event bus.
 * This can be started or the process method could be called periodically.
 * It can be used to channel events from Kafka, JMS, and/or Redis into the QBit world.
 */
public class EventBusQueueAdapter<T> implements Startable, Stoppable{

    /**
     * Queue.
     */
    private Queue<T> queue;
    /**
     * Event Manager.
     */
    private final EventManager eventManager;

    /**
     * Channel to send messages to from the Queue.
     */
    private final String channel;

    /**
     * Lock.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(EventBusQueueAdapter.class);

    /**
     * Debug is on or off.
     */
    private final boolean debug = logger.isDebugEnabled();
    private final Supplier<Queue<T>> queueSupplier;

    /**
     *
     * @param queueSupplier queueSupplier
     * @param eventManager event manager
     * @param channel channel
     */
    public EventBusQueueAdapter(
            final Supplier<Queue<T>> queueSupplier,
            final EventManager eventManager,
                                final String channel) {

        this.queueSupplier = queueSupplier;
        this.queue = queueSupplier.get();
        this.eventManager = eventManager;
        this.channel = channel;
    }

    /**
     * Process. This can be called periodically and it will check to see if there are messages on the queue.
     */
    public void process() {

        final ReceiveQueue<T> receiveQueue = queue.receiveQueue();

        T item;

        do {
           try {
               item = receiveQueue.poll();
           } catch (Exception ex) {
               queue = queueSupplier.get();
               item = null;
           }
           if (item!=null) {
               sendToEventManager(item);
           }
        } while (item !=null);
    }

    /**
     * Send the queue item to the event.
     * @param item item
     */
    private void sendToEventManager(T item) {
        if (debug) {
            logger.debug("EventBusQueueAdapter::sendToEventManager({})", item);
        }
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


    /**
     * Stop the adapter.
     */
    public void stop() {
        queue.stop();
    }
}

package org.qbit.queue.impl;

import org.boon.Boon;
import org.boon.Logger;
import org.qbit.queue.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 8/4/14.
 */
public class BasicQueue<T> implements Queue<T> {


    private final LinkedTransferQueue<Object> queue = new LinkedTransferQueue<>();
    private final int batchSize;

    private final AtomicBoolean stop = new AtomicBoolean();


    private Logger logger = Boon.logger(BasicQueue.class);


    private ReceiveQueueManager<T> receiveQueueManager;
    private ScheduledExecutorService monitor;

    private ScheduledFuture<?> future;
    private final String name;


    private final int waitTime;

    private final TimeUnit timeUnit;


    public BasicQueue(String name,
                      final int waitTime,
                      final TimeUnit timeUnit,
                      int batchSize) {
        this.name = name;
        this.waitTime = waitTime;
        this.timeUnit = timeUnit;

        this.batchSize = batchSize;


        this.receiveQueueManager = new BasicReceiveQueueManager<>();

    }

    /**
     * This returns a new instance of ReceiveQueue<T> every time you call it
     * so call it only once per thread.
     * @return received queue.
     */
    @Override
    public ReceiveQueue<T> receiveQueue() {
        return new BasicReceiveQueue<>(queue, waitTime, timeUnit, batchSize);
    }

    /**
     * This returns a new instance of SendQueue<T> every time you call it
     * so call it only once per thread.
     * @return sendQueue.
     */
    @Override
    public SendQueue<T> sendQueue() {
        return new BasicSendQueue<>(batchSize, this.queue);
    }


    @Override
    public void startListener(final ReceiveQueueListener<T> listener) {


        if (monitor == null) {
            monitor = Executors.newScheduledThreadPool(1,
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable runnable) {
                            Thread thread = new Thread(runnable);
                            thread.setName("QueueListener " + name);
                            return thread;
                        }
                    }
            );
        } else {
            die("Only one BasicQueue listener allowed at a time");
        }


        future = monitor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    manageQueue(listener);
                }catch (Exception ex) {
                    logger.error(ex, "BasicQueue Manager", "Problem running queue manager");
                }
            }

        }, 50, 50, TimeUnit.MILLISECONDS);


    }

    @Override
    public void stop() {
        if (future != null) {


            future.cancel(true);
        }

        if (monitor != null) {
            monitor.shutdownNow();
        }

        stop.set(true);


    }


    private void manageQueue(ReceiveQueueListener<T> listener) {
        this.receiveQueueManager.manageQueue(receiveQueue(), listener, batchSize, stop);

    }

    public static <T> BasicQueue<T> create(Class<T> cls) {
        return new BasicQueue<>("BasicQueue", 10, TimeUnit.MILLISECONDS, 10);
    }


    public static <T> BasicQueue<T> create(Class<T> cls, int batchSize) {
        return new BasicQueue<>("BasicQueue", 10, TimeUnit.MILLISECONDS, batchSize);
    }
}

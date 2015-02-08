package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.queue.*;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.ConstructorAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the base for all the queues we use.
 * <p>
 * Created by Richard on 8/4/14.
 * @param <T> type
 * @author rhightower
 */
public class BasicQueue<T> implements Queue<T> {

    private final BlockingQueue<Object> queue;


    private final boolean checkIfBusy;
    private final int batchSize;
    private final AtomicBoolean stop = new AtomicBoolean();
    private final Logger logger = LoggerFactory.getLogger(BasicQueue.class);
    private final ReceiveQueueManager<T> receiveQueueManager;
    private final String name;
    private final int waitTime;
    private final TimeUnit timeUnit;

    private final boolean  tryTransfer;

    private final boolean debug = GlobalConstants.DEBUG;

    private final int checkEvery;
    private ScheduledExecutorService monitor;
    private ScheduledFuture<?> future;

    public BasicQueue(final String name,
                      final int waitTime,
                      final TimeUnit timeUnit,
                      final int batchSize,
                      final Class<? extends BlockingQueue> queueClass,
                      final boolean checkIfBusy,
                      final int size,
                      final int checkEvery, boolean tryTransfer) {


        this.tryTransfer = tryTransfer;
        this.name = name;
        this.waitTime = waitTime;
        this.timeUnit = timeUnit;
        this.batchSize = batchSize;

        boolean shouldCheckIfBusy;

        this.receiveQueueManager = new BasicReceiveQueueManager<>();


        if (size==-1) {
                this.queue = ClassMeta.classMeta(queueClass).noArgConstructor().create();
        } else {

            final ClassMeta<? extends BlockingQueue> classMeta = ClassMeta.classMeta(queueClass);
            final ConstructorAccess<Object> constructor = classMeta.declaredConstructor(int.class);
            this.queue = (BlockingQueue<Object>) constructor.create(size);
        }


        shouldCheckIfBusy = queue instanceof TransferQueue;

        if (shouldCheckIfBusy && checkIfBusy) {
            this.checkIfBusy = true;
        } else {
            this.checkIfBusy = false;
        }

        this.checkEvery = checkEvery;
    }


    /**
     * This returns a new instance of ReceiveQueue every time you call it
     * so call it only once per thread.
     *
     * @return received queue
     */
    @Override
    public ReceiveQueue<T> receiveQueue() {
        return new BasicReceiveQueue<>(queue, waitTime, timeUnit, batchSize);
    }

    /**
     * This returns a new instance of SendQueue every time you call it
     * so call it only once per thread.
     *
     * @return sendQueue.
     */
    @Override
    public SendQueue<T> sendQueue() {
        return new BasicSendQueue<>(batchSize, this.queue, checkIfBusy, checkEvery, tryTransfer);
    }

    @Override
    public void startListener(final ReceiveQueueListener<T> listener) {
        if (monitor == null) {
            monitor = Executors.newScheduledThreadPool(1,
                    runnable -> {
                        Thread thread = new Thread(runnable);
                        thread.setName("QueueListener " + name);
                        return thread;
                    }
            );
        } else {
            throw new IllegalStateException("Only one BasicQueue listener allowed at a time");
        }

        future = monitor.scheduleAtFixedRate(() -> {
            try {
                manageQueue(listener);
            } catch (Exception ex) {
                logger.error("BasicQueue Manager::Problem running queue manager", ex);
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
}

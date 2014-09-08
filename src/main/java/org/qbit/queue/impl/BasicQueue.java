package org.qbit.queue.impl;

import org.qbit.queue.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 8/4/14.
 */
public class BasicQueue <T> implements Queue<T> {


    private final LinkedTransferQueue<Object> queue = new LinkedTransferQueue<>();
    private final int batchSize;

    AtomicBoolean stop = new AtomicBoolean();


    private  ReceiveQueueManager<T> receiveQueueManager;
    private ScheduledExecutorService monitor;

    private ScheduledFuture<?> future;




    private final SendQueue<T> outputQueue;
    private final ReceiveQueue<T> inputQueue;

    public BasicQueue(int waitTime, TimeUnit timeUnit, int batchSize) {
        this.outputQueue = new BasicSendQueue<>(queue);

        this.batchSize = batchSize;

        this.inputQueue = new BasicReceiveQueue<>(queue, waitTime, timeUnit, batchSize);

        this.receiveQueueManager = new BasicReceiveQueueManager<>();

    }

    @Override
    public ReceiveQueue<T> receive() {
        return inputQueue;
    }

    @Override
    public SendQueue<T> send() {
        return outputQueue;
    }



    @Override
    public void startListener(final ReceiveQueueListener<T> listener) {


        if (monitor == null) {
            monitor = Executors.newScheduledThreadPool(1,
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable runnable) {
                            Thread thread = new Thread(runnable);
                            thread.setName("BasicQueueListener");
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
              manageQueue(listener);
            }

        }, 50, 50, TimeUnit.MILLISECONDS);



    }

    @Override
    public void stop() {
        if (future!=null) {


            future.cancel(true);
        }

        if (monitor!=null) {
            monitor.shutdownNow();
        }

        stop.set(true);



    }


    private void manageQueue(ReceiveQueueListener<T> listener) {
        this.receiveQueueManager.manageQueue(inputQueue, listener, batchSize, stop);

    }

}

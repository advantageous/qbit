package io.advantageous.qbit.queue;

import io.advantageous.qbit.queue.impl.BasicQueue;

import java.util.concurrent.*;

/**
 *
 * Allows for the programmatic construction of a queue.
 *
 * Created by rhightower on 12/14/14.
 */
public class QueueBuilder {

    private int batchSize = 10_000;
    private int pollWait = 10;
    private int size = 1_000;
    private int checkEvery = 100;

    private String name;
    private Class<? extends BlockingQueue> queueClass = ArrayBlockingQueue.class;

    private boolean tryTransfer=false;


    public int getCheckEvery() {
        return checkEvery;
    }

    public QueueBuilder setCheckEvery(int checkEvery) {
        this.checkEvery = checkEvery;
        return this;
    }

    public QueueBuilder setLinkedBlockingQueue() {
        if (size==-1) {
            size = 1_000;
        }
        queueClass = LinkedBlockingQueue.class;
        return this;
    }

    public QueueBuilder setArrayBlockingQueue() {
        if (size==-1) {
            size = 1_000;
        }
        queueClass = ArrayBlockingQueue.class;
        return this;
    }


    public QueueBuilder setLinkTransferQueue() {
        size = -1;
        queueClass = LinkedTransferQueue.class;
        return this;
    }


    public int getSize() {
        return size;
    }

    public QueueBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    public boolean isTryTransfer() {
        return tryTransfer;
    }

    public QueueBuilder setTryTransfer(boolean tryTransfer) {
        this.tryTransfer = tryTransfer;
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public QueueBuilder setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public int getPollWait() {
        return pollWait;
    }

    public QueueBuilder setPollWait(int pollWait) {
        this.pollWait = pollWait;
        return this;

    }

    public String getName() {
        return name;
    }

    public QueueBuilder setName(String name) {
        this.name = name;
        return this;

    }

    public Class<? extends BlockingQueue> getQueueClass() {
        return queueClass;
    }

    public QueueBuilder setQueueClass(Class<? extends BlockingQueue> queueClass) {
        this.queueClass = queueClass;
        return this;

    }



    public <T> Queue<T> build() {
        return new BasicQueue<>(name, pollWait, TimeUnit.MILLISECONDS, batchSize, queueClass, tryTransfer, size, checkEvery);
    }

}

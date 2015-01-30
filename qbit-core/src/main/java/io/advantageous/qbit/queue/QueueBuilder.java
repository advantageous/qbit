package io.advantageous.qbit.queue;

import io.advantageous.qbit.queue.impl.BasicQueue;

import java.util.concurrent.*;

/**
 *
 * Allows for the programmatic construction of a queue.
 *
 * Created by rhightower on 12/14/14.
 */
public class QueueBuilder implements Cloneable{

    public static QueueBuilder queueBuilder() {return new QueueBuilder();}
    private int batchSize = 500;
    private int pollWait = 5;
    private int size = 1_000_000;
    private int checkEvery = 100;

    private String name;
    private Class<? extends BlockingQueue> queueClass = ArrayBlockingQueue.class;

    private boolean tryTransfer=false;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getCheckEvery() {
        return checkEvery;
    }

    public QueueBuilder setCheckEvery(int checkEvery) {
        this.checkEvery = checkEvery;
        return this;
    }

    public QueueBuilder setLinkedBlockingQueue() {
        queueClass = LinkedBlockingQueue.class;
        return this;
    }

    public QueueBuilder setArrayBlockingQueue() {
        if (size==-1) {
            size = 1_000_000;
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




    public <T> Queue<T> build() {
        return new BasicQueue<>(this.getName(), this.getPollWait(), TimeUnit.MILLISECONDS, this.getBatchSize(),
                this.queueClass, this.isTryTransfer(), this.getSize(), this.getCheckEvery());
    }

}

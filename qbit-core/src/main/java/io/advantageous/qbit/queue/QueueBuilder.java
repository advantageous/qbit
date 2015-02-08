package io.advantageous.qbit.queue;

import io.advantageous.qbit.GlobalConstants;
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
    private int batchSize = GlobalConstants.BATCH_SIZE;
    private int pollWait = GlobalConstants.POLL_WAIT;
    private int size = GlobalConstants.NUM_BATCHES;
    private int checkEvery = 100;
    private boolean tryTransfer = false;

    private String name;
    private Class<? extends BlockingQueue> queueClass = ArrayBlockingQueue.class;

    private boolean checkIfBusy =false;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getCheckEvery() {
        return checkEvery;
    }

    public boolean isTryTransfer() {
        return tryTransfer;
    }

    public QueueBuilder setTryTransfer(boolean tryTransfer) {
        this.tryTransfer = tryTransfer;
        return this;
    }

    public Class<? extends BlockingQueue> getQueueClass() {
        return queueClass;
    }

    public void setQueueClass(Class<? extends BlockingQueue> queueClass) {
        this.queueClass = queueClass;
    }

    public QueueBuilder setCheckEvery(int checkEvery) {
        this.checkEvery = checkEvery;
        this.checkIfBusy = true;
        return this;
    }

    public QueueBuilder setLinkedBlockingQueue() {
        queueClass = LinkedBlockingQueue.class;
        return this;
    }

    public QueueBuilder setArrayBlockingQueue() {
        if (size==-1) {
            size = 100_000;
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

    public boolean isCheckIfBusy() {
        return checkIfBusy;
    }

    public QueueBuilder setCheckIfBusy(boolean checkIfBusy) {
        this.checkIfBusy = checkIfBusy;
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
                this.queueClass, this.isCheckIfBusy(), this.getSize(), this.getCheckEvery(), this.isTryTransfer());
    }

}

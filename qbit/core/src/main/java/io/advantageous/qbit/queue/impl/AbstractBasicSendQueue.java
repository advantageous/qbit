package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.SendQueue;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import static io.advantageous.qbit.queue.impl.SenderHelperMethods.objectArray;
import static io.advantageous.qbit.queue.impl.SenderHelperMethods.objectArrayFromIterable;

public abstract class AbstractBasicSendQueue <T> implements SendQueue<T> {

    protected final BlockingQueue<Object> queue;
    protected final Queue<T> owner;
    protected final int batchSize;
    private final Logger logger;
    protected int checkEveryStarted = 0;
    protected int index;
    protected final String name;
    protected final Object[] queueLocal;

    public AbstractBasicSendQueue(final BlockingQueue<Object> queue, Queue<T> owner,
                                  final int batchSize,
                                  final String name,
                                  final Logger logger) {
        this.queue = queue;
        this.owner = owner;
        this.batchSize = batchSize;
        this.name = name;
        this.queueLocal = new Object[batchSize];
        this.logger = logger;
                
    }


    public boolean shouldBatch() {
        return true;
    }


    @Override
    public boolean send(T item) {
        checkStarted();
        boolean ableToSend = flushIfOverBatch();
        queueLocal[index] = item;
        index++;
        return ableToSend;
    }

    protected abstract boolean flushIfOverBatch();
    protected abstract boolean sendArray(Object[] items);
    protected abstract boolean sendLocalQueue();

    private void checkStarted() {

        if (checkEveryStarted % 100 == 0) {
            if (!owner.started()) {
                logger.warn("BasicSendQueue:: name {} send queue NOT STARTED", name);
            }
        }

        checkEveryStarted++;
    }

    @Override
    public void sendAndFlush(T item) {
        checkStarted();
        send(item);
        flushSends();
    }

    @SafeVarargs
    @Override
    public final void sendMany(T... items) {
        checkStarted();
        flushSends();
        sendArray(items);
    }


    @Override
    public void sendBatch(Iterable<T> items) {
        checkStarted();
        flushSends();
        final Object[] array = objectArrayFromIterable(items);
        sendArray(array);
    }

    @Override
    public void sendBatch(Collection<T> items) {
        checkStarted();
        flushSends();
        final Object[] array = objectArray(items);
        sendArray(array);

    }


    @Override
    public int hashCode() {
        return queue.hashCode();
    }

    @Override
    public String name() {
        return name;
    }


    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void flushSends() {
        if (index > 0) {
            sendLocalQueue();
        }
    }

}

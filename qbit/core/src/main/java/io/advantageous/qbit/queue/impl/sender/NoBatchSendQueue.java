package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.SendQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.LinkedTransferQueue;


public class NoBatchSendQueue<T> implements SendQueue<T> {


    protected final LinkedTransferQueue<Object> queue;
    protected final Queue<T> owner;
    private final Logger logger = LoggerFactory.getLogger(NoBatchSendQueue.class);
    protected int checkEveryStarted = 0;
    protected int index;
    protected final String name;

    public NoBatchSendQueue(final LinkedTransferQueue<Object> queue,
                            final Queue<T> owner,
                                  final String name) {
        this.queue = queue;
        this.owner = owner;
        this.name = name;

    }


    public boolean shouldBatch() {
        return false;
    }


    @Override
    public final boolean send(T item) {
        checkStarted();
        return queue.offer(item);
    }


    private void checkStarted() {

        if (checkEveryStarted % 100 == 0) {
            if (!owner.started()) {
                logger.warn("{} :: name {} send queue NOT STARTED",
                        this.getClass().getSimpleName(), name);
            }
        }

        checkEveryStarted++;
    }

    @Override
    public final void sendAndFlush(T item) {
        checkStarted();
        send(item);
    }

    @SafeVarargs
    @Override
    public final void sendMany(T... items) {
        checkStarted();
        for (T item : items) {
            send(item);
        }
    }


    @Override
    public final void sendBatch(Iterable<T> items) {
        checkStarted();
        for (T item : items) {
            send(item);
        }
    }

    @Override
    public final void sendBatch(Collection<T> items) {
        checkStarted();
        for (T item : items) {
            send(item);
        }
    }


    @Override
    public final int hashCode() {
        return queue.hashCode();
    }

    @Override
    public final String name() {
        return name;
    }


    @Override
    public final int size() {
        return queue.size();
    }

    @Override
    public final void flushSends() {
    }

}

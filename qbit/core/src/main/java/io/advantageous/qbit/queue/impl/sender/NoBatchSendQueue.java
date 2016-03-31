package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.SendQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.LinkedTransferQueue;


public class NoBatchSendQueue<T> implements SendQueue<T> {


    protected final LinkedTransferQueue<Object> queue;
    protected final Queue<T> owner;
    protected final String name;
    private final Logger logger = LoggerFactory.getLogger(NoBatchSendQueue.class);
    private final boolean checkStart = Sys.sysProp("QBIT_CHECK_START", false);
    private final int checkStartWarnEvery = Sys.sysProp("QBIT_CHECK_START_WARN_EVERY", 100);
    private final boolean checkQueueSize = Sys.sysProp("QBIT_CHECK_QUEUE_SIZE", false);
    private final int checkQueueSizeWarnIfOver = Sys.sysProp("QBIT_CHECK_QUEUE_SIZE_WARN_IF_OVER", 10);
    protected int checkEveryStarted = 0;
    protected int index;

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


        if (checkQueueSize) {
            if (queue.size() > checkQueueSizeWarnIfOver) {
                logger.warn("{} :: name {} queue is filling up", this.getClass().getSimpleName(), name);
            }
        }

        if (checkStart) {

            if (checkEveryStarted % checkStartWarnEvery == 0) {

                if (!owner.started()) {
                    logger.warn("{} :: name {} send queue NOT STARTED", this.getClass().getSimpleName(), name);
                }
            }

            checkEveryStarted++;
        }
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

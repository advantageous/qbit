package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.SendQueue;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import static io.advantageous.qbit.queue.impl.sender.SenderHelperMethods.*;

public abstract class AbstractBasicSendQueue<T> implements SendQueue<T> {

    protected final BlockingQueue<Object> queue;
    protected final Queue<T> owner;
    protected final int batchSize;
    protected final String name;
    private final Logger logger;
    private final boolean checkStart = Sys.sysProp("QBIT_CHECK_START", false);
    private final int checkStartWarnEvery = Sys.sysProp("QBIT_CHECK_START_WARN_EVERY", 100);
    private final boolean checkQueueSize = Sys.sysProp("QBIT_CHECK_QUEUE_SIZE", false);
    private final int checkQueueSizeWarnIfOver = Sys.sysProp("QBIT_CHECK_QUEUE_SIZE_WARN_IF_OVER", 10);
    protected int checkEveryStarted = 0;
    protected int index;
    protected Object[] queueLocal;

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

    protected abstract boolean flushIfOverBatch();

    protected abstract boolean sendArray(Object[] items);

    public boolean shouldBatch() {
        return true;
    }


    @Override
    public final boolean send(T item) {
        checkStarted();
        boolean ableToSend = flushIfOverBatch();
        queueLocal[index] = item;
        index++;
        return ableToSend;
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
    public final void sendBatch(Iterable<T> items) {
        checkStarted();
        flushSends();
        final Object[] array = objectArrayFromIterable(items);
        sendArray(array);
    }

    @Override
    public final void sendBatch(Collection<T> items) {
        checkStarted();
        flushSends();
        final Object[] array = objectArrayFromCollection(items);
        sendArray(array);

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
        sendLocalQueue();
    }

    protected final boolean sendLocalQueue() {

        if (index > 0) {
            boolean ableToSend;

            final Object[] copy = fastObjectArraySlice(queueLocal, 0, index);
            ableToSend = sendArray(copy);
            Arrays.fill(queueLocal, null);
            index = 0;
            return ableToSend;
        } else {
            return true;
        }
    }
}

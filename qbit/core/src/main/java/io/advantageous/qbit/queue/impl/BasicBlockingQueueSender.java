package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.UnableToEnqueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TransferQueue;

import static io.advantageous.qbit.queue.impl.SenderHelperMethods.fastObjectArraySlice;

public class BasicBlockingQueueSender<T> extends AbstractBasicSendQueue <T> {

    private final Logger logger = LoggerFactory.getLogger(BasicBlockingQueueSender.class);
    private final UnableToEnqueueHandler unableToEnqueueHandler;


    public BasicBlockingQueueSender(
            final String name,
            final int batchSize,
            final BlockingQueue<Object> queue,
            final boolean checkBusy,
            final UnableToEnqueueHandler unableToEnqueueHandler,
            final Queue<T> owner) {

        super(queue, owner, batchSize, name + "| BQ SEND QUEUE", LoggerFactory.getLogger(BasicBlockingQueueSender.class));


        this.unableToEnqueueHandler = unableToEnqueueHandler;
        if (queue instanceof TransferQueue && checkBusy) {
            throw new IllegalStateException("Should never pass transfer queue");
        }
    }


    protected final  boolean flushIfOverBatch() {
        return index < batchSize || sendLocalQueue();
    }


    protected final boolean sendLocalQueue() {

        final Object[] copy = fastObjectArraySlice(queueLocal, 0, index);
        boolean ableToSend = sendArray(copy);
        index = 0;
        return ableToSend;
    }

    protected final  boolean sendArray(final Object[] array) {
            if (!queue.offer(array)) {
                logger.error("Unable to send to queue {} " +
                                " Size of queue {} ",
                        name,   queue.size());
                return unableToEnqueueHandler.unableToEnqueue(queue, name);
            } else {
                return true;
            }
    }


}

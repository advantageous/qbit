package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.UnableToEnqueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TransferQueue;

public class BasicBlockingQueueSender<T> extends AbstractBasicSendQueue<T> {

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


    protected final boolean flushIfOverBatch() {
        return index < batchSize || sendLocalQueue();
    }


    protected final boolean sendArray(final Object[] array) {

        if (array.length == 0) {
            throw new IllegalStateException("Array length is 0");
        }
        if (!queue.offer(array)) {
            logger.error("Unable to send to queue {} " +
                            " Size of queue {} ",
                    name, queue.size());
            return unableToEnqueueHandler.unableToEnqueue(queue, name, array);
        } else {
            return true;
        }
    }


}

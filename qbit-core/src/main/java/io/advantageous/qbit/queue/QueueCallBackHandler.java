package io.advantageous.qbit.queue;

/**
 * Created by rhightower on 2/10/15.
 */
public interface QueueCallBackHandler {

    /**
     * Queue has reached its limit, can be the same as batch size for queue.
     * This is for periodic flushing to IO or CPU intensive services to improve throughput.
     * Larger batches can equate to a lot less thread sync for the hand-off.
     */
    void queueLimit();

    /**
     * Notification that there is nothing else in the queue.
     */
    void queueEmpty();


    /** Callback for when the queue has started. */
    default void queueInit() {}

    /** Callback for when the queue is idle. */
    default void queueIdle() {}

    /** Callback for when the queue is shutdown. */
    default void queueShutdown(){}

    /** Callback for when the queue has just received some message.
     * idle can mean you are asleep with nothing to do.
     * startBatch can mean you just woke up.
     **/
    default void queueStartBatch() {}

}

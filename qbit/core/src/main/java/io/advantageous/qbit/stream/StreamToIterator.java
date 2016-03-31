package io.advantageous.qbit.stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Turns a non-blocking reactive stream into a blocking iterator.
 * This is used to support an easier but less performance-oriented programming model.
 *
 * @param <T> T
 */
public class StreamToIterator<T> implements Iterator<T> {


    /**
     * Passes items from the non-blocking stream to the client thread where it can block.
     */
    private final BlockingQueue<Object> blockingQueue = new LinkedTransferQueue<>(); //Unbounded
    /**
     * Message to denote that the stream is finished.
     */
    private final Object COMPLETE = new Object();
    /**
     * Message to denote that the stream passed an exception, which we will wrap an re-throw.
     */
    private final Object ERROR = new Object();
    /**
     * Denotes that the stream sent the close message or sent an error.
     */
    private boolean complete;
    /**
     * Holds the next item in the queue.
     */
    private T next;

    /**
     * Creates Iterator that wraps the pulisher with a blocking interface.
     *
     * @param publisher pulisher that we are adapting as a blocking Iterator
     */
    private StreamToIterator(final Publisher<T> publisher, final int requestSize) {
        publisher.subscribe(new Subscriber<T>() {
            private Subscription subscription;
            private int count = requestSize;

            @Override
            public void onSubscribe(Subscription s) {

                this.subscription = s;
                this.subscription.request(requestSize);
            }

            @Override
            public void onNext(T item) {

                blockingQueue.offer(item);

                count--;
                if (count <= 0) {
                    count = requestSize;
                    subscription.request(requestSize);//this is settable
                }

            }

            @Override
            public void onError(final Throwable error) {

                blockingQueue.offer(ERROR);
                blockingQueue.offer(error);
            }

            @Override
            public void onComplete() {

                blockingQueue.offer(COMPLETE);

            }
        });
    }

    /**
     * @param publisher pulisher that we are adapting as a blocking Iterator
     * @param <T>       T
     * @return Iterator that wraps the pulisher with a blocking interface.
     */
    public static <T> Iterator<T> toIterator(final Publisher<T> publisher) {
        return new StreamToIterator<>(publisher, 2);
    }

    /**
     * @param publisher           pulisher that we are adapting as a blocking Iterator
     * @param numRequestsPipeline how many messages we will accept up to
     * @param <T>                 T
     * @return Iterator that wraps the pulisher with a blocking interface.
     */
    public static <T> Iterator<T> toIteratorWithRequestsPipelineSize(final Publisher<T> publisher,
                                                                     final int numRequestsPipeline) {
        return new StreamToIterator<>(publisher, numRequestsPipeline);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     * <p>
     * Checks to see if the stream is closed or has sent an error.
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {

        /* If we are in the completed state let the end user know. */
        if (complete) {
            return false;
        }

        /* If we called hasNext twice, do not poll the queue. */
        if (next != null) {
            return true;
        }

        /* Get the item from the queue, and block until we do. */
        Object item = null;

        while (item == null) {
            try {
                item = blockingQueue.take();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

        /* Check to see if the complete message was sent.
        * If so mark this iterator has completed and set the next item to null.
        */
        if (item == COMPLETE) {
            complete = true;
            next = null;
            return false;
        } else {
            /* Store the next item. */
            next = (T) item;
            return true;
        }

    }


    /**
     * Returns the next element in the iteration.
     * This will throw a {@code StreamException} wrapping the error sent from the stream.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public T next() {

        /* If the item is null and we are complete then calling this is a programming error. */
        if (next == null && complete) {
            throw new NoSuchElementException("The stream has completed. There are no more items.");
        }

        /* If the item is an error, throw the error wrapped by a runtime error. */
        if (next == ERROR) {
            try {
                final Object error = blockingQueue.take();
                complete = true;
                throw new StreamException("Exception from stream", (Throwable) error);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new StreamException("Stream sent error but unable to get error from stream", e);
            }
        } else {
            /* Give them the next item but don't store the next item any longer. */
            T theNext = next;
            next = null;
            return theNext;
        }
    }

    /**
     * Used to wrap the original exception from the stream so we can rethrow it.
     */
    public static class StreamException extends RuntimeException {
        public StreamException(String reason, Throwable throwable) {
            super(reason, throwable);
        }
    }
}

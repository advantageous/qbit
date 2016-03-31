package io.advantageous.qbit.stream;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class QueueToStreamUnicast<T> implements Publisher<T> {


    private final Queue<T> queue;
    private final Runnable onSubscriptionEmpty;


    public QueueToStreamUnicast(final Queue<T> queue) {
        this.queue = queue;
        onSubscriptionEmpty = () -> {

        };
    }


    public QueueToStreamUnicast(final Queue<T> queue, final Runnable runnable) {
        this.queue = queue;
        this.onSubscriptionEmpty = runnable;
    }

    @Override
    public void subscribe(final Subscriber<? super T> subscriber) {

        final SubscriptionImpl subscription = new SubscriptionImpl();

        try {
            this.queue.startListener(new ReceiveQueueListener<T>() {

                long sendThisMany = 0;

                //If a Subscription is cancelled its Subscriber MUST eventually stop being signaled.
                boolean stop = false;

                @Override
                public void empty() {

                    initStreamState();
                }

                private void initStreamState() {
                    sendThisMany = subscription.count(sendThisMany);
                    stop = subscription.stop.get();
                }

                @Override
                public void limit() {

                    initStreamState();
                }

                @Override
                public void shutdown() {

                    subscriber.onComplete();
                }

                @Override
                public void idle() {
                    initStreamState();
                }

                @Override
                public void startBatch() {
                    initStreamState();
                }

                @Override
                public void init() {
                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void receive(T item) {


                    /**
                     * The total number of onNext signals sent by a Publisher to a Subscriber
                     * MUST be less than or equal to the total number of
                     * elements requested by that Subscriber's Subscription at all times.
                     */
                    waitForCountsIfNeeded();

                    /** If a Subscription is cancelled its Subscriber
                     * MUST eventually stop being signaled.*/
                    if (stop) {
                        queue.stop();
                    }

                    subscriber.onNext(item);
                    sendThisMany--;

                }

                /** This could be pluggable so you can do a spin wait. */
                private void waitForCountsIfNeeded() {
                    if (sendThisMany == 0) {
                        onSubscriptionEmpty.run();
                        for (int index = 0; index < 100_000; index++) {
                            initStreamState();
                            if (sendThisMany > 0 || stop) break;
                            //spin no sleep
                        }
                        for (int index = 0; index < 10_000; index++) {
                            initStreamState();
                            if (sendThisMany > 0 || stop) break;
                            Thread.yield();
                            //yield no sleep
                        }
                        for (int index = 0; index < 100; index++) {
                            initStreamState();
                            if (sendThisMany > 0 || stop) break;
                            Sys.sleep(1);
                            //Sleep but just briefly
                        }
                        for (int index = 0; index < 100; index++) {
                            initStreamState();
                            if (sendThisMany > 0 || stop) break;
                            Sys.sleep(5);
                            //Sleep but longer
                        }
                        /** Wait forever .*/
                        while (sendThisMany == 0) {
                            initStreamState();
                            if (sendThisMany > 0 || stop) break;
                            Sys.sleep(15);
                            //Sleep long and do this until we get a count
                        }
                    }
                }
            });
        } catch (Exception ex) {
            subscriber.onError(ex);
        }
    }

    class SubscriptionImpl implements Subscription {

        private final LinkedTransferQueue<Long> requests = new LinkedTransferQueue<>();
        private final AtomicBoolean stop = new AtomicBoolean();

        @Override
        public void request(long n) {
            requests.offer(n);
        }

        @Override
        public void cancel() {
            stop.set(true);
        }

        public long count(long count) {

            Long requested = requests.poll();

            while (requested != null) {
                count += requested;
                requested = requests.poll();
            }

            return count;
        }

    }
}

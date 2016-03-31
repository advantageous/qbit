package io.advantageous.qbit.stream;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueToStreamRoundRobin<T> implements Publisher<T> {


    private final Queue<T> queue;
    private final ArrayBlockingQueue<SubscriptionImpl> newSubscriptionQueue = new ArrayBlockingQueue<>(1000);
    private final AtomicBoolean started = new AtomicBoolean();
    private final Runnable onSubscriptionEmpty;


    public QueueToStreamRoundRobin(final Queue<T> queue) {
        this.queue = queue;

        this.onSubscriptionEmpty = () -> {
        };
    }


    public QueueToStreamRoundRobin(final Queue<T> queue, final Runnable runnable) {
        this.queue = queue;
        this.onSubscriptionEmpty = runnable;
    }

    @Override
    public void subscribe(final Subscriber<? super T> subscriber) {


        final SubscriptionImpl subscription = new SubscriptionImpl(subscriber);
        try {
            newSubscriptionQueue.put(subscription);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        if (!started.get()) {
            if (started.compareAndSet(false, true)) {
                start(subscriber);
            }
        }

    }

    private void start(
            final Subscriber<? super T> subscriber) {

        try {
            this.queue.startListener(new ReceiveQueueListener<T>() {


                private final List<SubscriptionImpl> subscriptions = new ArrayList<>();

                int index = 0;

                SubscriptionImpl subscription;


                //If a Subscription is cancelled its Subscriber MUST eventually stop being signaled.
                boolean stop = false;

                @Override
                public void empty() {

                    initStreamState();
                }

                private void initStreamState() {

                    SubscriptionImpl poll = newSubscriptionQueue.poll();
                    while (poll != null) {
                        poll.subscriber.onSubscribe(poll);
                        subscriptions.add(poll);
                        poll = newSubscriptionQueue.poll();
                    }

                    if (index >= subscriptions.size()) {
                        index = 0;
                    }
                    subscription = subscriptions.get(index);
                    final long requestCount = subscription.requestCount();
                    stop = subscription.stop.get();

                    if (requestCount == 0L) {
                        long totalCount = 0L;
                        for (SubscriptionImpl s : subscriptions) {
                            totalCount += s.requestCount();
                        }
                        if (totalCount == 0L) {
                            onSubscriptionEmpty.run();
                        }
                    }
                    index++;
                }

                @Override
                public void limit() {

                    initStreamState();
                }

                @Override
                public void shutdown() {

                    subscriptions.forEach(subscription1 -> subscription1.subscriber.onComplete());
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

                    subscription.subscriber.onNext(item);
                    subscription.requestCount--;

                }

                /** This could be pluggable so you can do a spin wait. */
                private void waitForCountsIfNeeded() {
                    if (subscription.requestCount == 0) {
                        for (int index = 0; index < 100_000; index++) {
                            initStreamState();
                            if (subscription.requestCount > 0 || stop) break;
                            //spin no sleep
                        }
                        for (int index = 0; index < 10_000; index++) {
                            initStreamState();
                            if (subscription.requestCount > 0 || stop) break;
                            Thread.yield();
                            //yield no sleep
                        }
                        for (int index = 0; index < 100; index++) {
                            initStreamState();
                            if (subscription.requestCount > 0 || stop) break;
                            Sys.sleep(1);
                            //Sleep but just briefly
                        }
                        for (int index = 0; index < 100; index++) {
                            initStreamState();
                            if (subscription.requestCount > 0 || stop) break;
                            Sys.sleep(5);
                            //Sleep but longer
                        }
                        /** Wait forever .*/
                        while (subscription.requestCount == 0) {
                            initStreamState();
                            if (subscription.requestCount > 0 || stop) break;
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

        final LinkedTransferQueue<Long> requests = new LinkedTransferQueue<>();

        final Subscriber<? super T> subscriber;

        final AtomicBoolean stop = new AtomicBoolean();


        long requestCount;

        SubscriptionImpl(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            requests.offer(n);
        }

        @Override
        public void cancel() {
            stop.set(true);
        }

        public long requestCount() {

            Long requested = requests.poll();

            while (requested != null) {
                requestCount += requested;
                requested = requests.poll();
            }

            return requestCount;
        }

    }
}

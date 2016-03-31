package io.advantageous.qbit.stream;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.SendQueue;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QueueToStreamUnicastTest {


    @Test
    public void test() throws InterruptedException {
        final Queue<Trade> queue = QueueBuilder.queueBuilder().build();

        final QueueToStreamUnicast<Trade> stream = new QueueToStreamUnicast<>(queue);

        final CopyOnWriteArrayList<Trade> trades = new CopyOnWriteArrayList<>();
        final AtomicBoolean stop = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(1);

        stream.subscribe(new Subscriber<Trade>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(10);
                subscription.set(s);
            }

            @Override
            public void onNext(Trade trade) {

                trades.add(trade);

                if (trades.size() == 10) {
                    latch.countDown();
                }
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
            }

            @Override
            public void onComplete() {
                stop.set(true);
                stopLatch.countDown();
            }
        });

        final SendQueue<Trade> tradeSendQueue = queue.sendQueue();

        for (int index = 0; index < 20; index++) {
            tradeSendQueue.send(new Trade("TESLA", 100L + index));
        }
        tradeSendQueue.flushSends();
        Sys.sleep(100);
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(10, trades.size());

        assertEquals(false, stop.get());

        assertNotNull(subscription.get());


        subscription.get().cancel();

        stopLatch.await(10, TimeUnit.SECONDS);


        assertEquals(true, stop.get());

    }

    private class Trade {
        final String name;
        final long amount;

        private Trade(String name, long amount) {
            this.name = name;
            this.amount = amount;
        }
    }

}
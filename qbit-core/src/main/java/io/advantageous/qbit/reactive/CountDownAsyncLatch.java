package io.advantageous.qbit.reactive;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Async Latch
 * Created by rhightower on 3/22/15.
 */
public class CountDownAsyncLatch {


    public static CountDownAsyncLatch countDownLatch(final int count, final Runnable allDone) {
        return new CountDownAsyncLatch(count, allDone);
    }

    private final Runnable allDone;
    private final AtomicInteger count = new AtomicInteger();


    public CountDownAsyncLatch(final int count, final Runnable allDone) {

        this.count.set(count);
        this.allDone = allDone;

    }

    public void countDown() {
        int c = count.get();
        if (c <= 0) {
            return;
        }

        while (!count.compareAndSet(c, c-1)) {

            c = count.get();

        }

        if ((c - 1) == 0) {
            allDone.run();
        }
    }
}

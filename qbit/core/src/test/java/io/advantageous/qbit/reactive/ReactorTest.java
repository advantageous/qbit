package io.advantageous.qbit.reactive;

import io.advantageous.qbit.util.TestTimer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ReactorTest {


    @Test
    public void testTimeOutCallback() throws Exception {


        final TestTimer testTimer = new TestTimer();
        final Reactor reactor = ReactorBuilder.reactorBuilder().setTimer(testTimer).build();
        final AtomicBoolean test = new AtomicBoolean();
        final AtomicInteger count = new AtomicInteger();
        testTimer.setTime();

        AsyncFutureCallback<Object> callback = reactor.callbackBuilder().setTimeoutTimeUnit(TimeUnit.SECONDS).setTimeoutDuration(1).setOnTimeout(() -> {
            count.incrementAndGet();
            test.set(true);
        }).setCallback(Object.class, o -> {
        })
                .build();

        reactor.process();

        /* No timeouts yet. */
        assertFalse(test.get());
        assertEquals(0, count.get());
        assertFalse(callback.isTimedOut());

        /* Move five seconds into the future. */
        testTimer.seconds(5);


        reactor.process();
        reactor.process();

        /* Now there should be one timeout. */
        assertTrue(test.get());
        assertEquals(1, count.get());

        assertTrue(callback.isTimedOut());

    }


    @Test
    public void testNormalCallback() throws Exception {


        final TestTimer testTimer = new TestTimer();
        final Reactor reactor = ReactorBuilder.reactorBuilder().setTimer(testTimer).build();
        final AtomicBoolean timeoutFlag = new AtomicBoolean();
        final AtomicInteger timeOutCounter = new AtomicInteger();
        final AtomicBoolean callbackFlag = new AtomicBoolean();
        final AtomicInteger callbackCounter = new AtomicInteger();


        testTimer.setTime();

        AsyncFutureCallback<Object> callback = reactor.callbackBuilder().setTimeoutTimeUnit(TimeUnit.SECONDS).setTimeoutDuration(1).setOnTimeout(() -> {
            timeOutCounter.incrementAndGet();
            timeoutFlag.set(true);
        }).setCallback(Object.class, o -> {
            callbackFlag.set(true);
            callbackCounter.incrementAndGet();
        })
                .build();

        reactor.process();

        /* No timeouts yet and no callbacks. */
        assertFalse(timeoutFlag.get());
        assertFalse(callbackFlag.get());
        assertEquals(0, timeOutCounter.get());
        assertEquals(0, callbackCounter.get());
        assertFalse(callback.isTimedOut());


        /* Pretend like we have been called. */
        callback.accept("callback arg");

        /* Move five seconds into the future. */
        testTimer.seconds(5);


        reactor.process();
        reactor.process();

        /* Now there should be one callback and no timeouts. */
        assertFalse(timeoutFlag.get());
        assertTrue(callbackFlag.get());

        assertEquals(0, timeOutCounter.get());
        assertEquals(1, callbackCounter.get());
        assertFalse(callback.isTimedOut());
        assertTrue(callback.isDone());
        assertNotNull(callback.get());
        assertEquals("callback arg", callback.get());
    }


    @Test
    public void testErrorCallback() throws Exception {


        final TestTimer testTimer = new TestTimer();
        final Reactor reactor = ReactorBuilder.reactorBuilder().setTimer(testTimer).build();
        final AtomicBoolean timeoutFlag = new AtomicBoolean();
        final AtomicInteger timeOutCounter = new AtomicInteger();
        final AtomicBoolean callbackFlag = new AtomicBoolean();
        final AtomicInteger callbackCounter = new AtomicInteger();
        final AtomicBoolean errorFlag = new AtomicBoolean();
        final AtomicInteger errorCounter = new AtomicInteger();


        testTimer.setTime();

        AsyncFutureCallback<Object> callback = reactor.callbackBuilder().setTimeoutTimeUnit(TimeUnit.SECONDS).setTimeoutDuration(1).setOnTimeout(() -> {
            timeOutCounter.incrementAndGet();
            timeoutFlag.set(true);
        }).setCallback(Object.class, o -> {
            callbackFlag.set(true);
            callbackCounter.incrementAndGet();
        }).setOnError(throwable -> {
            errorCounter.incrementAndGet();
            errorFlag.set(true);
        })
                .build();

        reactor.process();

        /* No timeouts yet and no callbacks. */
        assertFalse(timeoutFlag.get());
        assertFalse(callbackFlag.get());
        assertEquals(0, timeOutCounter.get());
        assertEquals(0, callbackCounter.get());
        assertFalse(callback.isTimedOut());


        /* Pretend like service threw an exception. */
        callback.onError(new Exception("callback exception"));

        /* Move five seconds into the future. */
        testTimer.seconds(5);


        reactor.process();
        reactor.process();

        /* Now there should be one callback and no timeouts. */
        assertFalse(timeoutFlag.get());
        assertFalse(callbackFlag.get());
        assertTrue(errorFlag.get());

        assertEquals(0, timeOutCounter.get());
        assertEquals(0, callbackCounter.get());
        assertEquals(1, errorCounter.get());

        assertFalse(callback.isTimedOut());
        assertTrue(callback.isDone());
        assertFalse(callback.isCancelled());

        try {
            assertNotNull(callback.get());
        } catch (Exception ex) {
            assertTrue(ex.getMessage().endsWith("callback exception"));

        }
    }

    @Test
    public void testOneShotTest() throws Exception {


        TestTimer testTimer = new TestTimer();
        Reactor reactor = ReactorBuilder.reactorBuilder().setTimer(testTimer).build();
        final AtomicBoolean test = new AtomicBoolean();
        final AtomicInteger count = new AtomicInteger();

        testTimer.setTime();


        /* Add a task that repeats every ten seconds. */
        reactor.addOneShotAfterTask(10, TimeUnit.SECONDS, () -> {
            test.set(true);
            count.incrementAndGet();
        });


        /* Nothing should have run, we have not called process. */
        assertFalse(test.get());
        assertEquals(0, count.get());


        testTimer.seconds(12);
        reactor.process();


        /* We should have one count and test should be true. */
        assertTrue(test.get());
        assertEquals(1, count.get());

        /* Run again. No time change.*/
        test.set(false);


        testTimer.seconds(12);
        reactor.process();

        assertFalse(test.get());
        assertEquals(1, count.get());

    }


    @Test
    public void testTimedTasks() throws Exception {


        TestTimer testTimer = new TestTimer();
        Reactor reactor = ReactorBuilder.reactorBuilder().setTimer(testTimer).build();
        final AtomicBoolean test = new AtomicBoolean();
        final AtomicInteger count = new AtomicInteger();

        testTimer.setTime();


        /* Add a task that repeats every ten seconds. */
        reactor.addRepeatingTask(10, TimeUnit.SECONDS, () -> {
            test.set(true);
            count.incrementAndGet();
        });


        /* Nothing should have run, we have not called process. */
        assertFalse(test.get());
        assertEquals(0, count.get());


        reactor.process();


        /* We should have one count and test should be true. */
        assertTrue(test.get());
        assertEquals(1, count.get());

        /* Run again. No time change.*/
        test.set(false);


        reactor.process();

        /* We should have one count and test should be false because it is the same time. */
        assertFalse(test.get());
        assertEquals(1, count.get());

        /*Now advance 15 seconds. */


        reactor.process();



        /* We should have two count and test should be true because
        we are 15 seconds in the future. */
        assertFalse(test.get());
        assertEquals(1, count.get());


    }
}
package io.advantageous.qbit.util;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.concurrent.ExecutorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.concurrent.ScheduledExecutorBuilder.scheduledExecutorBuilder;

/**
 * @author rhightower
 */
public class Timer {

    private final Logger logger = LoggerFactory.getLogger(Timer.class);

    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();

    private final AtomicLong time = new AtomicLong(System.nanoTime() / 1_000_000);

    private  ExecutorContext executorContext;

    private static AtomicReference<Timer> timeHolder = new AtomicReference<>();

    public static Timer timer() {
        if (timeHolder.get() == null) {

            if (timeHolder.compareAndSet(timeHolder.get(), new Timer())) {
                timeHolder.get().start();
            }

        }
        return timeHolder.get();
    }

    public void stop() {

        if (debug) {
            logger.debug("timer stopped");
        }

        if (executorContext!=null) {
            executorContext.stop();
        }
        executorContext = null;
    }


    private void start() {

        if (debug) {
            logger.debug("timer started");
        }

        if (executorContext!=null) {
            throw new IllegalStateException("You can't start a timer twice");
        }

        executorContext = scheduledExecutorBuilder().setPriority(Thread.MAX_PRIORITY)
                .setThreadName("Timer OutputQueue Manager").setDaemon(true)
                .setInitialDelay(50).setPeriod(50).setRunnable(new Runnable() {
            @Override
            public void run() {
                manageTimer();
            }
        }).build();
        executorContext.start();


    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void manageTimer() {
        int count = 0;
        while (true) {
            count++;
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            time.addAndGet(5);
            if (count > 100) {
                time.set(System.nanoTime() / 1_000_000);
                count = 0;
            }
        }
    }

    public long time() {
        return time.get();
    }

    public long now() {
        return time.get();
    }

}

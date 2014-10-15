package io.advantageous.qbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Timer {

    private final Logger logger = LoggerFactory.getLogger(Timer.class);

    private final AtomicLong time = new AtomicLong(System.nanoTime() / 1_000_000);

    private ScheduledExecutorService monitor;

    private ScheduledFuture<?> future;

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
        future.cancel(true);
        monitor.shutdownNow();
        monitor = null;
    }

    private void start() {

        if (monitor == null)
            monitor = Executors.newScheduledThreadPool(1,
                    runnable -> {
                        Thread thread = new Thread(runnable);
                        thread.setPriority(Thread.MAX_PRIORITY);
                        thread.setName("Timer OutputQueue Manager");
                        return thread;
                    }
            );

        future = monitor.scheduleAtFixedRate(() -> {
            try {
                manageTimer();
            } catch (Exception ex) {
                logger.error("can't manage timeHolder", ex);
            }
        }, 50, 50, TimeUnit.MILLISECONDS);

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

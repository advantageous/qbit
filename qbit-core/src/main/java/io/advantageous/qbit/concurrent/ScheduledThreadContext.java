package io.advantageous.qbit.concurrent;

import io.advantageous.qbit.GlobalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by rhightower on 2/13/15.
 */
public class ScheduledThreadContext implements ExecutorContext{
    private final Logger logger = LoggerFactory.getLogger(ScheduledThreadContext.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();


    private ScheduledExecutorService monitor;
    private ScheduledFuture<?> future;
    private final Runnable runnable;
    private final int initialDelay;
    private final int period;
    private final TimeUnit unit;
    private final String threadName;
    private final String description;




    public ScheduledThreadContext(final Runnable runnable,
                                  final int initialDelay,
                                  final int period,
                                  final TimeUnit unit,
                                  final String threadName,
                                  final String description) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.runnable = runnable;
        this.threadName = threadName;
        this.description = description;
    }

    @Override
    public void start() {

        if (monitor!=null) {
            throw new IllegalStateException("Must be stopped before it can be started");
        }
        monitor = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName(threadName);
                    return thread;
                }
        );
        /** This wants to be configurable. */
        future = monitor.scheduleAtFixedRate(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error("Problem running: " + description, ex);
            }
        }, initialDelay, period, unit);
    }

    @Override
    public void stop() {


        try {
            if (future != null) {
                future.cancel(true);
                future = null;
            }

        }catch (Exception ex) {

            logger.error("Problem stopping: " + description + "- Can't cancel timer", ex);
        }

        try {

            if (monitor != null) {
                monitor.shutdown();
                monitor = null;
            }
        }catch (Exception ex) {


            logger.error("Problem stopping: " + description + "- Can't shutdown monitor", ex);
        }

        future = null;
        monitor = null;

    }
}

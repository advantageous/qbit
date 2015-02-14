package io.advantageous.qbit.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Created by rhightower on 2/13/15.
 */
public class ScheduledExecutorBuilder {

    public static ScheduledExecutorBuilder scheduledExecutorBuilder() {
        return new ScheduledExecutorBuilder();
    }

    private int initialDelay=50;
    private int period=50;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private String threadName;
    private String description;
    private  Runnable runnable;
    private int priority = Thread.NORM_PRIORITY;
    private  boolean daemon;

    public boolean isDaemon() {
        return daemon;
    }

    public ScheduledExecutorBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public ScheduledExecutorBuilder setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public ScheduledExecutorBuilder setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public ScheduledExecutorBuilder setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
        return this;
    }

    public int getPeriod() {
        return period;
    }

    public ScheduledExecutorBuilder setPeriod(int period) {
        this.period = period;
        return this;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public ScheduledExecutorBuilder setUnit(TimeUnit unit) {
        this.unit = unit;
        return this;
    }

    public String getThreadName() {
        return threadName;
    }

    public ScheduledExecutorBuilder setThreadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ScheduledExecutorBuilder setDescription(String description) {
        this.description = description;
        return this;
    }


    public ExecutorContext build() {
        return new ScheduledThreadContext(this.getRunnable(), this.getInitialDelay(),
                this.getPeriod(), this.getUnit(), this.getThreadName(), this.getDescription(), priority, daemon
        );
    }
}

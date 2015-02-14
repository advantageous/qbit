package io.advantageous.qbit.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Created by rhightower on 2/13/15.
 */
public class ScheduledExecutorBuilder {

    public static ScheduledExecutorBuilder scheduledExecutorBuilder() {
        return new ScheduledExecutorBuilder();
    }

    private int initialDelay;
    private int period;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private String threadName;
    private String description;
    private  Runnable runnable;

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
                this.getPeriod(), this.getUnit(), this.getThreadName(), this.getDescription()  );
    }
}

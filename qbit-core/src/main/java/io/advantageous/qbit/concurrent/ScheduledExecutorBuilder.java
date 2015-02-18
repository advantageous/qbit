/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Created by rhightower on 2/13/15.
 */
public class ScheduledExecutorBuilder {

    private int initialDelay = 50;
    private int period = 50;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private String threadName;
    private String description;
    private Runnable runnable;
    private int priority = Thread.NORM_PRIORITY;
    private boolean daemon;

    public static ScheduledExecutorBuilder scheduledExecutorBuilder() {
        return new ScheduledExecutorBuilder();
    }

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

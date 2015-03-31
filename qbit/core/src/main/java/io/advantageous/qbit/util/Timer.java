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

    private static AtomicReference<Timer> timeHolder = new AtomicReference<>();
    private final Logger logger = LoggerFactory.getLogger(Timer.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final AtomicLong time = new AtomicLong(System.nanoTime() / 1_000_000);
    private ExecutorContext executorContext;

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

        if (executorContext != null) {
            executorContext.stop();
        }
        executorContext = null;
    }


    private void start() {

        if (debug) {
            logger.debug("timer started");
        }

        if (executorContext != null) {
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

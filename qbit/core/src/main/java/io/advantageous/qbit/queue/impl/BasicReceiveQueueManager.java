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

package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.concurrent.ExecutorContext;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.ReceiveQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.advantageous.qbit.concurrent.ScheduledExecutorBuilder.scheduledExecutorBuilder;


/**
 * created by Richard on 9/8/14.
 *
 * @author rhightower
 */
public class BasicReceiveQueueManager<T> implements ReceiveQueueManager<T> {


    private final Logger logger = LoggerFactory.getLogger(BasicReceiveQueueManager.class);
    private final boolean debug = logger.isDebugEnabled();
    private final String name;
    private final AtomicBoolean stop = new AtomicBoolean();
    private ExecutorContext executorContext;
    private QueueInfo<T> queueInfo;

    public BasicReceiveQueueManager(final String name) {
        this.name = name;
    }

    @Override
    public void start() {

        this.executorContext = scheduledExecutorBuilder()
                //.setDaemon(true) TODO #444 https://github.com/advantageous/qbit/issues/444
                .setThreadName("QueueListener|" + name)
                .setInitialDelay(50)
                .setPeriod(50).setRunnable(this::manageQueue)
                .build();

        executorContext.start();
    }

    private void manageQueue() {

        if (queueInfo == null) {
            return;
        }

        final String name = queueInfo.name;
        final ReceiveQueue<T> inputQueue = queueInfo.inputQueue;
        final ReceiveQueueListener<T> listener = queueInfo.listener;
        final int limit = queueInfo.limit;


        listener.init();

        T item = inputQueue.poll(); //Initialize things.

        int count = 0;

        /* Continues forever or until someone calls stop. */
        while (true) {

            if (item != null) {
                listener.startBatch();
            }

            /* Collect a batch of items as long as no item is null. */
            while (item != null) {
                count++;
                /* Notify listener that we have an item. */
                listener.receive(item);

                /* If the batch size has hit the max then we need to call limit. */
                if (count >= limit) {
                    if (debug) {
                        logger.debug("BasicReceiveQueueManager {} limit reached batch size = {}", name, limit);
                    }
                    /* Notify that a limit has been met and reset the count to 0. */
                    listener.limit();
                    if (stop.get()) {
                        listener.shutdown();
                        return;
                    }
                    count = 0;
                }
                /* Grab the next item from the queue. */
                item = inputQueue.poll();
                count++;
            }

            count = 0;

            /* Notify listener that the queue is empty. */
            listener.empty();


            /* Get the next item, but wait this time since the queue was empty.
            * This pauses the queue handling so we don't eat up all of the CPU.
            * */
            item = inputQueue.pollWait();

            if (item == null) {
                if (stop.get()) {
                    listener.shutdown();
                    return;
                }
                /* Idle means we yielded and then waited a full wait time, so idle might be a good time to do clean up
                or timed tasks.
                 */
                listener.idle();
            }
        }

    }

    @Override
    public void stop() {

        stop.set(true);
        if (this.executorContext != null) {
            executorContext.stop();
        }
    }

    @Override
    public void addQueueToManage(final String name,
                                 final ReceiveQueue<T> inputQueue,
                                 final ReceiveQueueListener<T> listener,
                                 final int batchSize) {

        queueInfo = new QueueInfo<>(name, inputQueue, listener, batchSize);

    }

    private static final class QueueInfo<T> {
        final String name;
        final ReceiveQueue<T> inputQueue;
        final ReceiveQueueListener<T> listener;
        final int limit;

        private QueueInfo(String name, ReceiveQueue<T> inputQueue,
                          ReceiveQueueListener<T> listener, int limit) {
            this.name = name;
            this.inputQueue = inputQueue;
            this.listener = listener;
            this.limit = limit;
        }
    }
}

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

import io.advantageous.qbit.queue.ReceiveQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.boon.Boon.puts;


/**
 * This is not thread safe.
 * You use BasicQueue to createWithWorkers this in the one thread that you are going to use it.
 * Created by Richard on 9/8/14.
 *
 * @author rhightower
 */
class BasicReceiveQueue<T> implements ReceiveQueue<T> {

    private final long waitTime;
    private final TimeUnit timeUnit;
    private final int batchSize;
    private final BlockingQueue<Object> queue;
    private Object[] lastQueue = null;
    private int lastQueueIndex;

    public BasicReceiveQueue(BlockingQueue<Object> queue, long waitTime, TimeUnit timeUnit, int batchSize) {
        this.queue = queue;
        this.waitTime = waitTime;
        this.timeUnit = timeUnit;
        this.batchSize = batchSize;
    }

    @Override
    public T pollWait() {

        if (lastQueue != null) {

            return getItemFromLocalQueue();
        }

        try {

            Object o = queue.poll(waitTime, timeUnit);
            return extractItem(o);
        } catch (InterruptedException e) {
            return null;
        }
    }


    private T getItemFromLocalQueue() {

        if (lastQueue.length==0) {
            return null;
        }

        T item = (T) lastQueue[lastQueueIndex];
        lastQueueIndex++;

        if (lastQueueIndex == lastQueue.length) {
            lastQueueIndex = 0;
            lastQueue = null;
        }
        return item;

    }


    @Override
    public T poll() {

        if (lastQueue != null) {

            return getItemFromLocalQueue();
        }

        Object o = queue.poll();
        return extractItem(o);

    }

    @Override
    public T take() {

        if (lastQueue != null) {

            return getItemFromLocalQueue();
        }

        try {
            Object o = queue.take();
            return extractItem(o);

        } catch (InterruptedException e) {
            Thread.interrupted();
            return null;
        }
    }

    private T extractItem(Object o) {

        if (o instanceof Object[]) {

            lastQueue = (Object[]) o;
            //uts("batch size", lastQueue.length);
            return getItemFromLocalQueue();
        } else {
            return (T) o;
        }
    }

    @Override
    public Iterable<T> readBatch(int max) {

        T item = this.poll();
        if (item == null) {
            return Collections.emptyList();
        } else {
            List<T> batch = new ArrayList<>();
            batch.add(item);
            while ((item = this.poll()) != null) {
                batch.add(item);
            }
            return batch;
        }
    }

    @Override
    public Iterable<T> readBatch() {
        return readBatch(batchSize);
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }
}

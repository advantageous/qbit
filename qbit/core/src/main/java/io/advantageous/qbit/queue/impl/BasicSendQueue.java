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

import io.advantageous.qbit.queue.QueueException;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.queue.UnableToEnqueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

/**
 * This is not thread safe.
 * Create a new for every thread by calling BasicQueue.sendQueue().
 * <p>
 * created by Richard on 9/8/14.
 *
 * @author rhightower
 */
public class BasicSendQueue<T> implements SendQueue<T> {

    private final Logger logger = LoggerFactory.getLogger(BasicSendQueue.class);
    //private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();


    private final BlockingQueue<Object> queue;

    private final TransferQueue<Object> transferQueue;

    private final Object[] queueLocal;
    private final int checkBusyEvery;
    private final boolean tryTransfer;
    private final boolean checkBusy;
    private final int batchSize;
    private final TimeUnit timeUnit;
    private final UnableToEnqueueHandler unableToEnqueueHandler;
    private int index;
    private int checkEveryCount = 0;
    private final String name;
    private final int enqueueTimeout;



    public BasicSendQueue(
            final String name,
            final int batchSize,
            final BlockingQueue<Object> queue,
            final boolean checkBusy,
            final int checkBusyEvery,
            final boolean tryTransfer,
            final TimeUnit timeUnit,
            final int enqueueTimeout,
            final UnableToEnqueueHandler unableToEnqueueHandler) {

        this.timeUnit = timeUnit;
        this.enqueueTimeout = enqueueTimeout;
        this.name = name + "|SEND QUEUE";
        this.tryTransfer = tryTransfer;
        this.batchSize = batchSize;
        this.queue = queue;
        queueLocal = new Object[batchSize];
        this.unableToEnqueueHandler = unableToEnqueueHandler;
        if (queue instanceof TransferQueue && checkBusy) {
            //noinspection unchecked
            transferQueue = ((TransferQueue) queue);
            this.checkBusy = true;
        } else {
            this.checkBusy = false;
            transferQueue = null;
        }
        this.checkBusyEvery = checkBusyEvery;
    }

    static Object[] objectArray(final Iterable iter) {
        if (iter instanceof Collection) {
            final Collection collection = (Collection) iter;
            return collection.toArray(new Object[collection.size()]);
        } else {
            return objectArray(list(iter));
        }
    }

    static <V> List<V> list(final Iterable<V> iterable) {
        final List<V> list = new ArrayList<>();
        for (V o : iterable) {
            list.add(o);
        }
        return list;
    }

    static Object[] fastObjectArraySlice(final Object[] array,
                                         @SuppressWarnings("SameParameterValue") final int start,
                                         final int end) {
        final int newLength = end - start;
        final Object[] newArray = new Object[newLength];
        System.arraycopy(array, start, newArray, 0, newLength);
        return newArray;
    }

    public boolean shouldBatch() {

        //noinspection SimplifiableIfStatement
        if (checkBusy) {
            return !transferQueue.hasWaitingConsumer();

        }

        return true;//might be other ways to determine this like flow control, not implemented yet.

    }

    @Override
    public boolean send(T item) {
        boolean ableToSend = flushIfOverBatch();
        queueLocal[index] = item;
        index++;
        return ableToSend;
    }

    @Override
    public void sendAndFlush(T item) {

        send(item);
        flushSends();
    }

    @SafeVarargs
    @Override
    public final void sendMany(T... items) {
        flushSends();
        sendArray(items);
    }

    @Override
    public void sendBatch(Iterable<T> items) {
        flushSends();
        final Object[] array = objectArray(items);
        sendArray(array);
    }

    @Override
    public void sendBatch(Collection<T> items) {
        flushSends();
        final Object[] array = objectArray(items);
        sendArray(array);

    }

    private boolean flushIfOverBatch() {

        if (index >= batchSize) {
            return sendLocalQueue();
        } else if (checkBusy) {
            checkEveryCount++;
            if (checkEveryCount > this.checkBusyEvery) {
                checkEveryCount = 0;
                if (transferQueue.hasWaitingConsumer()) {
                    return sendLocalQueue();
                }
            }
        }
        return true;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void flushSends() {
        if (index > 0) {
            sendLocalQueue();
        }
    }

    private boolean sendLocalQueue() {

        final Object[] copy = fastObjectArraySlice(queueLocal, 0, index);
        boolean ableToSend = sendArray(copy);
        index = 0;
        return ableToSend;
    }

    private boolean sendArray(final Object[] array) {

        if (checkBusy && tryTransfer) {
            if (!transferQueue.tryTransfer(array)) {
                return transferQueue.offer(array);
            }
        } else if (checkBusy) {
            return transferQueue.offer(array);
        } else {
            try {
                if (!queue.offer(array, enqueueTimeout, timeUnit)) {
                    logger.error("Unable to send to queue {} timeout is {} {}" +
                                    " Size of queue {} ",
                            name, enqueueTimeout, timeUnit, queue.size());


                    return unableToEnqueueHandler.unableToEnqueue(queue, name);
                } else {
                    return true;
                }
            } catch (InterruptedException e) {
                logger.error("Unable to send to queue {} timeout is {} {}" +
                                " Size of queue {} ",
                        name, enqueueTimeout, timeUnit, queue.size());
                throw new QueueException("Unable to send to queue " + name, e);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }

    @Override
    public String name() {
        return name;
    }
}

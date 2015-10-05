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

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.SendQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TransferQueue;

import static io.advantageous.qbit.queue.impl.SenderHelperMethods.objectArray;
import static io.advantageous.qbit.queue.impl.SenderHelperMethods.objectArrayFromIterable;


/**
 * This is not thread safe.
 * Create a new for every thread by calling BasicQueue.sendQueue().
 * <p>
 * created by Richard on 10/5/15.
 *
 * @author rhightower
 */
public class BasicSendQueueWithTransferQueue<T> implements SendQueue<T> {

    private final Logger logger = LoggerFactory.getLogger(BasicSendQueueWithTransferQueue.class);
    private final TransferQueue<Object> queue;
    private final Object[] queueLocal;
    private final int checkBusyEvery;
    private final int batchSize;
    private int index;
    private int checkEveryCount = 0;
    private final String name;
    private final Queue<T> owner;
    private int checkEveryStarted = 0;


    public BasicSendQueueWithTransferQueue(
            final String name,
            final int batchSize,
            final TransferQueue<Object> queue,
            final int checkBusyEvery,
            final Queue<T> owner) {

        this.name = name + "|SEND QUEUE";
        this.batchSize = batchSize;
        this.queue = queue;
        this.owner = owner;
        this.queueLocal = new Object[batchSize];
        this.checkBusyEvery = checkBusyEvery;
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
        return !queue.hasWaitingConsumer();
    }

    @Override
    public boolean send(T item) {
        checkStarted();
        boolean ableToSend = flushIfOverBatch();
        queueLocal[index] = item;
        index++;
        return ableToSend;
    }

    private void checkStarted() {
        if (checkEveryStarted % 100 == 0) {
            if (!owner.started()) {
                logger.warn("BasicSendQueue:: name {} send queue NOT STARTED", name);
            }
        }
        checkEveryStarted++;
    }

    @Override
    public void sendAndFlush(T item) {
        checkStarted();
        send(item);
        flushSends();
    }

    @SafeVarargs
    @Override
    public final void sendMany(T... items) {
        checkStarted();
        flushSends();
        sendArray(items);
    }

    @Override
    public void sendBatch(Iterable<T> items) {
        checkStarted();
        flushSends();
        final Object[] array = objectArrayFromIterable(items);
        sendArray(array);
    }

    @Override
    public void sendBatch(Collection<T> items) {
        checkStarted();
        flushSends();
        final Object[] array = objectArray(items);
        sendArray(array);

    }

    private boolean flushIfOverBatch() {

        if (index >= batchSize) {
            return sendLocalQueue();
        }


        checkEveryCount++;
        if (checkEveryCount > this.checkBusyEvery) {
            checkEveryCount = 0;
            if (queue.hasWaitingConsumer()) {
                return sendLocalQueue();
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
        return queue.offer(array);
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

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

import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.ConstructorAccess;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.queue.impl.sender.BasicBlockingQueueSender;
import io.advantageous.qbit.queue.impl.sender.BasicSendQueueWithTransferQueue;
import io.advantageous.qbit.queue.impl.sender.BasicSendQueueWithTryTransfer;
import io.advantageous.qbit.queue.impl.sender.NoBatchSendQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * This is the base for all the queues we use.
 * <p>
 * created by Richard on 8/4/14.
 *
 * @param <T> type
 * @author rhightower
 */
public class BasicQueue<T> implements Queue<T> {

    private final BlockingQueue<Object> queue;
    private final int batchSize;
    private final Logger logger = LoggerFactory.getLogger(BasicQueue.class);
    private final boolean debug = logger.isDebugEnabled();
    private final int limit;
    private final String name;
    private final int pollTimeWait;
    private final TimeUnit pollTimeTimeUnit;
    private final AtomicBoolean stop = new AtomicBoolean(true);
    private final Supplier<SendQueue<T>> sendQueueSupplier;
    private ReceiveQueueManager<T> receiveQueueManager;


    public BasicQueue(final String name,
                      final int waitTime,
                      @SuppressWarnings("SameParameterValue") final TimeUnit timeUnit,
                      final int batchSize,
                      final Class<? extends BlockingQueue> queueClass,
                      final boolean checkIfBusy,
                      final int size,
                      final int checkEvery,
                      final boolean tryTransfer,
                      final UnableToEnqueueHandler unableToEnqueueHandler,
                      final int limit) {

        logger.debug("Queue created {} {} limit {} size {} checkEvery {} tryTransfer {} waitTime {} limit {}",
                name, queueClass, batchSize, size, checkEvery, tryTransfer, waitTime, limit);


        this.name = name;
        this.pollTimeWait = waitTime;
        this.pollTimeTimeUnit = timeUnit;
        this.batchSize = batchSize;
        this.limit = limit;

        if (size == -1) {

            //noinspection unchecked
            this.queue = ClassMeta.classMeta(queueClass).noArgConstructor().create();
        } else {

            final ClassMeta<? extends BlockingQueue> classMeta = ClassMeta.classMeta(queueClass);
            if (queueClass != LinkedTransferQueue.class) {
                if (debug) logger.debug("Not a LinkedTransfer queue");
                final ConstructorAccess<Object> constructor = classMeta.declaredConstructor(int.class);
                //noinspection unchecked
                this.queue = (BlockingQueue<Object>) constructor.create(size);
            } else {
                final ConstructorAccess<? extends BlockingQueue> constructorAccess = classMeta.noArgConstructor();
                //noinspection unchecked
                this.queue = (BlockingQueue<Object>) constructorAccess.create();
            }
        }


        if (this.batchSize == 1) {

            if (queue instanceof LinkedTransferQueue) {
                sendQueueSupplier = () -> new NoBatchSendQueue<>((LinkedTransferQueue<Object>) queue, this, name);
            } else {
                throw new IllegalStateException("If batch size 1 queue must be a linked transfer queue");
            }
        } else if (queue instanceof LinkedTransferQueue) {

            if (tryTransfer) {
                sendQueueSupplier = () -> new BasicSendQueueWithTryTransfer<>(name, batchSize, (TransferQueue<Object>) queue,
                        checkEvery, BasicQueue.this);
            } else {
                sendQueueSupplier = () -> new BasicSendQueueWithTransferQueue<>(name, batchSize, ((TransferQueue<Object>) queue),
                        checkEvery, BasicQueue.this);
            }
        } else {
            sendQueueSupplier = () -> new BasicBlockingQueueSender<>(name, batchSize, queue,
                    checkIfBusy, unableToEnqueueHandler, BasicQueue.this);
        }


        logger.info("Queue done creating {} limit {} checkEvery {} tryTransfer {}" +
                        "pollTimeWait/polltime {}",
                this.name, this.batchSize, checkEvery, tryTransfer,
                this.pollTimeWait);


    }


    /**
     * This returns a new instance of ReceiveQueue every time you call it
     * so call it only once per thread.
     *
     * @return received queue
     */
    @Override
    public ReceiveQueue<T> receiveQueue() {
        if (debug) logger.debug("ReceiveQueue requested for {}", name);
        return new BasicReceiveQueue<>(queue, pollTimeWait, pollTimeTimeUnit, limit);
    }

    /**
     * This returns a new instance of SendQueue every time you call it
     * so call it only once per thread.
     *
     * @return sendQueue.
     */
    @Override
    public SendQueue<T> sendQueue() {
        if (debug) logger.debug("SendQueue requested for {}", name);
        return sendQueueSupplier.get();
    }


    @Override
    public void startListener(final ReceiveQueueListener<T> listener) {
        this.receiveQueueManager = new BasicReceiveQueueManager<>(name);
        stop.set(false);
        logger.info("Starting queue listener for  {} {}", name, listener);
        this.receiveQueueManager.addQueueToManage(name, this.receiveQueue(), listener, limit);
        this.receiveQueueManager.start();
    }

    @Override
    public void stop() {
        logger.info("Stopping queue  {}", name);
        stop.set(true);
        if (receiveQueueManager != null) {
            receiveQueueManager.stop();
        }

        if (queue != null) {
            queue.clear();
        }
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean started() {
        return !stop.get();
    }

    @Override
    public String toString() {
        return "BasicQueue{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public String name() {
        return name;
    }
}

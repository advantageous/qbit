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

package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.qbit.queue.Queue;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TransferQueue;

/**
 * This is not thread safe.
 * Create a new for every thread by calling BasicQueue.sendQueue().
 * <p>
 * created by Richard on 10/5/15.
 *
 * @author rhightower
 */
public class BasicSendQueueWithTryTransfer<T> extends AbstractBasicSendQueue<T> {


    private final TransferQueue<Object> queue;
    private final int checkBusyEvery;
    private int checkEveryCount = 0;


    public BasicSendQueueWithTryTransfer(
            final String name,
            final int batchSize,
            final TransferQueue<Object> queue,
            final int checkBusyEvery,
            final Queue<T> owner) {

        super(queue, owner, batchSize, name + "| TQT SEND QUEUE", LoggerFactory.getLogger(BasicSendQueueWithTryTransfer.class));


        this.queue = queue;
        this.checkBusyEvery = checkBusyEvery;
    }


    public final boolean shouldBatch() {
        return !queue.hasWaitingConsumer();
    }

    protected final boolean flushIfOverBatch() {

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

    protected final boolean sendArray(final Object[] array) {

        return queue.tryTransfer(array) || queue.offer(array);
    }

}

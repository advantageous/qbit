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

package io.advantageous.qbit.queue;

import io.advantageous.qbit.Input;

/**
 * Created by Richard on 7/18/14.
 * Simplifies queue handler loop code by abstracting queue operations.
 * <p>
 * The nitty gritty of polling the queue which can vary from one implementation to another is abstracted
 * These are all callback method to notify you when the queue has an item, when the queue is empty, etc.
 *
 * @author Richard Hightower
 */
public interface ReceiveQueueListener<T> extends Input {

    /**
     * Notifies a queue listener that an item has been received
     *
     * @param item item
     */
    void receive(T item);

    /**
     * Notifies the queue listener that currently the queue is empty.
     * This is good for batch operations. This could mean the queue is empty or we reached our max batch size limit.
     */
    default void empty() {
    }

    /**
     * Notifies the queue listener that we processed up to batch size.
     * This is good for batch operations. This could mean the queue is empty or we reached our max batch size limit.
     */
    default void limit() {
    }

    /**
     * Notifies the queue listener that currently the queue is closed for business.
     */
    default void shutdown() {
    }



    /**
     * This means we did not find an item. We waited for an item as well and there was still not an item in the queue
     * This would be a good time to do some clean up.
     */
    default void idle() {
    }

    default void startBatch() {
    }


}

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
 * Represents an receiveQueue queue.
 * This could be a TCP/IP connection.
 * Reading a file from the file system. Etc.
 * A read operation on a database.
 * <p>
 * This is a receive queue.
 *
 * @author Richard Hightower
 */
public interface ReceiveQueue<T> extends Input {

    /**
     * Gets the next item. If the item is null,
     * means the timeout has been reached.
     *
     * @return value from poll
     */
    T pollWait();


    /**
     * Gets the next item. If the item is null the queue currently has no items.
     *
     * @return value from poll
     */
    T poll();

    /**
     * Wait for the next item.
     *
     * @return value from take
     */
    T take();

    /**
     * Read in a batch of items.
     *
     * @param max max number you want from batch
     * @return batch of values
     */
    Iterable<T> readBatch(int max);


    /**
     * Read in a batch of items.
     *
     * @return batch of values
     */
    Iterable<T> readBatch();
}

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

/**
 * Represents a queue manager.
 * Queues are split up into receivers views and send views to facilitate batching.
 * Created by Richard on 8/4/14.
 *
 * @author rhightower
 */
public interface Queue<T> {

    /**
     * This returns a thread safe receive queue. Pulling an item off of the queue makes it unavailable to other thread.
     *
     * @return receive queue
     */
    ReceiveQueue<T> receiveQueue();

    /**
     * This returns a NON-thread safe send queue.
     * It is not thread safe so that you can batch sends to minimize thread hand-off
     * and to maximize IO throughput. Each call to this method returns a send queue
     * that can only be access from one thread.
     * You get MT behavior by having a SendQueue per thread.
     *
     * @return send queue
     */
    SendQueue<T> sendQueue();

    /**
     * This starts up a listener which will listen to items on the
     * receive queue. It will notify when the queue is empty, when the queue is idle, when the queue is shutdown, etc.
     * <p>
     * An idle queue is an indication that it is a good time to do periodic cleanup, etc.
     *
     * @param listener listener
     */
    void startListener(ReceiveQueueListener<T> listener);

    /**
     * Stop the listener.
     */
    void stop();
}

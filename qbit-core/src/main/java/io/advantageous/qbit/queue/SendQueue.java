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

import io.advantageous.qbit.Output;

import java.util.Collection;

/**
 * This provides a non-thread safe access to an output queue which allows batching of messages to other threads to
 * minimize thread coordination.
 * <p>
 * Created by Richard on 7/18/14.
 *
 * @author rhightower
 */
public interface SendQueue<T> extends Output {
    void send(T item);

    void sendAndFlush(T item);

    void sendMany(T... item);

    void sendBatch(Collection<T> item);

    void sendBatch(Iterable<T> item);

    boolean shouldBatch();

    void flushSends();


}

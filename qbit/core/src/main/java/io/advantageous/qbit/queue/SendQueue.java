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
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;

import java.util.Collection;

/**
 * This provides a non-thread safe access to an output queue which allows batching of messages to other threads to
 * minimize thread coordination.
 * <p>
 * created by Richard on 7/18/14.
 *
 * @author rhightower
 */
public interface SendQueue<T> extends Output, Startable, Stoppable {
    default boolean send(T item) {
        return true;
    }

    default void sendAndFlush(T item) {
        send(item);
        flushSends();
    }

    @SuppressWarnings("unchecked")
    default void sendMany(T... items) {
        for (T i : items) {
            send(i);
        }
    }

    default void sendBatch(Collection<T> items) {
        for (T i : items) {
            send(i);
        }
    }

    default void sendBatch(Iterable<T> items) {
        for (T i : items) {
            send(i);
        }
    }

    default boolean shouldBatch() {
        return true;
    }

    default void flushSends() {

    }

    default int size() {
        return 0;
    }

    default String name() {
        return "NO OP";
    }


}

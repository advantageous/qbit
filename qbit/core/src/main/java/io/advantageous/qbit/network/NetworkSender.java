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

package io.advantageous.qbit.network;

/**
 * WebSocket like thing that receives messages.
 * Could be mapped to non-websocket implementations.
 * created by rhightower on 2/14/15.
 */
public interface NetworkSender {

    void sendText(String message);

    default void sendBytes(byte[] message) {
        throw new UnsupportedOperationException();
    }

    default void close() {
    }

    default void open(NetSocket netSocket) {
    }
}

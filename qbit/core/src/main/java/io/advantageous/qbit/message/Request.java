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

package io.advantageous.qbit.message;

/**
 * This is an abstraction for an incoming client request.
 * <p>
 * created by Richard on 7/21/14.
 *
 * @author Rick Hightower
 */
public interface Request<T> extends Message<T> {

    String address();

    String returnAddress();


    default Request<Object> originatingRequest() {
        return null;
    }


    boolean hasParams();

    boolean hasHeaders();

    long timestamp();

    boolean isHandled();

    void handled();

    default boolean hasCallback() {
        return false;
    }
}

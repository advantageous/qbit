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

package io.advantageous.qbit.service;

import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Extends the JDK Consumer to provide a default error handler for RPC callbacks.
 * Note: This was boon Handler but we switched to JDK 8 Consumer style callback.
 * <p>
 * Created by gcc on 10/14/14.
 */

public interface Callback<T> extends Consumer<T> {

    default void onError(Throwable error) {

        LoggerFactory.getLogger(Callback.class)
                .error(error.getMessage(), error);
    }
}


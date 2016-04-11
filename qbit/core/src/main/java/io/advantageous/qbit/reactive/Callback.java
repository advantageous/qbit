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

package io.advantageous.qbit.reactive;

import org.slf4j.LoggerFactory;

/**
 * Extends the JDK Consumer to provide a default error handler for RPC callbacks.
 * Note: This was boon Handler but we switched to JDK 8 Consumer style callbackWithTimeout.
 * <p>
 * created by gcc on 10/14/14.
 * Was called Handler and created by Rick Hightower quite a bit before 10/14/14
 */

public interface Callback<T> {


    /**
     * Client View (client of the service)
     * Called when there is an error
     *
     * @param error error
     */
    default void onError(final Throwable error) {

        LoggerFactory.getLogger(Callback.class)
                .error(error.getMessage(), error);
    }

    /**
     * Service View (service)
     * Return an error message
     *
     * @param error error
     */
    default void returnError(final String error) {
        onError(new IllegalStateException(error));
    }


    /**
     * Service View (service)
     * Return an error message.
     * Added to make migration to Reakt easier.
     *
     * @param error error
     */
    default void reject(final String error) {
        onError(new IllegalStateException(error));
    }


    /**
     * Service View (service)
     * Return an error message.
     * Added to make migration to Reakt easier.
     *
     * @param error error
     */
    default void reject(final Throwable error) {
        onError(error);
    }

    /**
     * Service View (service)
     * Return an error message.
     * Added to make migration to Reakt easier.
     *
     * @param error error
     */
    default void reject(final String errorMessage, final Throwable error) {
        onError(new IllegalStateException(errorMessage, error));
    }


    /**
     * Called if there is a timeout.
     */
    default void onTimeout() {

    }

    /**
     * Client View (client of the service)
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);


    /**
     * Service View (service)
     *
     * @param thisReturn the value to return.
     */
    default void returnThis(T thisReturn) {
        accept(thisReturn);
    }


    /**
     * Service View (service)
     * Added to make migration to Reakt easier.
     *
     * @param thisReturn the value to return.
     */
    default void reply(T thisReturn) {
        accept(thisReturn);
    }


    /**
     * Service View (service)
     * Added to make migration to Reakt easier.
     *
     * @param thisReturn the value to return.
     */
    default void resolve(T thisReturn) {
        accept(thisReturn);
    }
}


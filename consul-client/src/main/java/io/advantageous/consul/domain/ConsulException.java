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
package io.advantageous.consul.domain;

import static io.advantageous.boon.Boon.sputs;

/**
 * Consul Exception
 */
public class ConsulException extends RuntimeException {

    public static void dieWithException(Exception ex, Object... args){
        throw new ConsulException( sputs(args), ex);
    }


    public static void die(Object... args){
        throw new ConsulException( sputs(args));
    }


    /**
     * Constructs an instance of this class.
     *
     * @param message The exception message.
     */
    public ConsulException(String message) {
        super(message);
    }

    /**
     * Constructs an instance of this class.
     *
     * @param message The exception message.
     * @param throwable The wrapped {@link java.lang.Throwable} object.
     */
    public ConsulException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

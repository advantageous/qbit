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
 * A registered service attempted to
 * check in.  This condition indicates an agent has been restarted and left
 * the cluster.
 *
 * Services should registerService again if this is thrown.
 */
public class NotRegisteredException extends ConsulException {

    public static void notRegistered(Object... args){
        throw new NotRegisteredException( sputs(args));
    }


    public NotRegisteredException(String message) {
        super(message);
    }

    public NotRegisteredException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

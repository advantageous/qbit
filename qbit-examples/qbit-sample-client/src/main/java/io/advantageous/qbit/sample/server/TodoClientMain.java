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

package io.advantageous.qbit.sample.server;

/**
 * Created by rhightower on 11/5/14.
 */
public class TodoClientMain {

    public static void main(final String... args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("websocket")) {
                TodoWebSocketClient.main(args);
            } else if (args[0].equalsIgnoreCase("rest")) {
                TodoRESTClient.main(args);
            }
        } else {
            TodoWebSocketClient.main(args);
        }
    }

}



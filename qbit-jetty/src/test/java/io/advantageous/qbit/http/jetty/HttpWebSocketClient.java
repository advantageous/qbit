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

package io.advantageous.qbit.http.jetty;


import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.boon.core.Sys;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;


/**
 * @author rhightower on 2/13/15.
 */
public class HttpWebSocketClient {

    public static void main(String... args) {

        final HttpClient httpClient = httpClientBuilder()
                .setPort(9999).build().start();


        final WebSocket webSocket = httpClient.createWebSocket("/hello");

        webSocket.setTextMessageConsumer(message ->
                        puts("\n\n\n", message, "\n\n")
        );

        webSocket.openAndWait();

        webSocket.sendText("Hello");

        Sys.sleep(100000);
    }
}

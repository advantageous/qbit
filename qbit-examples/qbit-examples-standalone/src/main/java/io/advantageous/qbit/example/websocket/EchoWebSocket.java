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

package io.advantageous.qbit.example.websocket;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.websocket.WebSocket;
import org.boon.core.Sys;

import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.server.HttpServerBuilder.httpServerBuilder;

/**
 * Created by rhightower on 2/16/15.
 */
public class EchoWebSocket {


    public static void main(String... args) {


        /* Create an HTTP server. */
        HttpServer httpServer = httpServerBuilder()
                .setPort(8080).build();

        /* Setup WebSocket Server support. */
        httpServer.setWebSocketOnOpenConsumer(webSocket -> {
            webSocket.setTextMessageConsumer(message -> {
                webSocket.sendText("ECHO " + message);
            });
        });

        /* Start the server. */
        httpServer.start();

        /** CLIENT. */

        /* Setup an httpClient. */
        HttpClient httpClient = httpClientBuilder()
                .setHost("localhost").setPort(8080).build();
        httpClient.start();

        /* Setup the client websocket. */
        WebSocket webSocket = httpClient
                .createWebSocket("/websocket/rocket");

        webSocket.setTextMessageConsumer(message -> {
            System.out.println(message);
        });
        webSocket.openAndWait();

        /* Send some messages. */
        webSocket.sendText("Hi mom");
        webSocket.sendText("Hello World!");


        Sys.sleep(1000);
        webSocket.close();
        httpClient.stop();
        httpServer.stop();
    }

}
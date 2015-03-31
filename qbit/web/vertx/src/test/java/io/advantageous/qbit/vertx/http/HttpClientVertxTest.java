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

package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.http.server.websocket.WebSocketMessageBuilder;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;


public class HttpClientVertxTest extends TimedTesting {

    AtomicBoolean requestReceived;
    AtomicBoolean responseReceived;
    HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
    HttpClient client;
    HttpServer server;

    public void connect(int port) {

        client = new HttpClientBuilder().setPort(port).build();

        server = new HttpServerBuilder().setPort(port).build();

        requestReceived = new AtomicBoolean();
        responseReceived = new AtomicBoolean();

    }

    @Test
    public void testWebSocket() {

        connect(9090);


        server.setWebSocketMessageConsumer(webSocketMessage -> {

            puts(webSocketMessage.address(), webSocketMessage.body());
            if (webSocketMessage.getMessage().equals("What do you want on your cheeseburger?")) {
                webSocketMessage.getSender().sendText("Bacon");
                requestReceived.set(true);

            } else {
                puts("Websocket message", webSocketMessage.getMessage());
            }
        });


        run();


        final WebSocket webSocket = client.createWebSocket("/services/cheeseburger");

        webSocket.setTextMessageConsumer(message -> {
            if (message.equals("Bacon")) {
                responseReceived.set(true);
            }
        });


        webSocket.setOpenConsumer(aVoid -> webSocket.sendText("What do you want on your cheeseburger?"));

        webSocket.open();


        client.flush();



        validate();
        stop();

    }


    @Test
    public void testNewOpenWaitWebSocket() {

        connect(9090);


        server.setWebSocketMessageConsumer(webSocketMessage -> {

            puts(webSocketMessage.address(), webSocketMessage.body());
            if (webSocketMessage.getMessage().equals("What do you want on your cheeseburger?")) {
                webSocketMessage.getSender().sendText("Bacon");
                requestReceived.set(true);

            } else {
                puts("Websocket message", webSocketMessage.getMessage());
            }
        });


        run();


        final WebSocket webSocket = client.createWebSocket("/services/cheeseburger");

        webSocket.setTextMessageConsumer(message -> {
            if (message.equals("Bacon")) {
                responseReceived.set(true);
            }
        });

        webSocket.openAndWait();

        webSocket.sendText("What do you want on your cheeseburger?");


        client.flush();



        validate();
        stop();

    }

    @Test
    public void testNewOpenWaitWebSocketNewServerStuff() {

        connect(9090);


        server.setWebSocketOnOpenConsumer(webSocket -> webSocket.setTextMessageConsumer(message -> {
            if (message.equals("What do you want on your cheeseburger?")) {
                webSocket.sendText("Bacon");
                requestReceived.set(true);
            } else {
                puts("Websocket message", message);
            }
        }));

        run();


        final WebSocket webSocket = client.createWebSocket("/services/cheeseburger");

        webSocket.setTextMessageConsumer(message -> {
            if (message.equals("Bacon")) {
                responseReceived.set(true);
            }
        });

        webSocket.openAndWait();

        webSocket.sendText("What do you want on your cheeseburger?");


        client.flush();



        validate();
        stop();

    }

    @Test
    public void testHttpServerClient() throws Exception {


        connect(9191);


        server.setHttpRequestConsumer(request -> {
            requestReceived.set(true);
            puts("SERVER", request.getUri(), request.getBody());
            request.getReceiver().response(200, "application/json", "\"ok\"");
        });

        run();

        requestBuilder.setRemoteAddress("localhost").setMethod("GET").setUri("/client/foo");

        requestBuilder.setTextReceiver((code, mimeType, body) -> {
            responseReceived.set(true);

            puts("CLIENT", code, mimeType, body);

        });

        client.sendHttpRequest(requestBuilder.build());
        client.flush();

        validate();
        stop();
    }


    public void run() {

        server.start();
        client.start();
        Sys.sleep(500);
    }


    private void stop() {


        client.stop();
        server.stop();

        Sys.sleep(500);
    }

    public void validate() {


        super.waitForTrigger(10, o -> requestReceived.get() && responseReceived.get());

        if (!requestReceived.get()) {
            die("Request not received");
        }


        if (!responseReceived.get()) {
            die("Response not received");
        }

    }
}
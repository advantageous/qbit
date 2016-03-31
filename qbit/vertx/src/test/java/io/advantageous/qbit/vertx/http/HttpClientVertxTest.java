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
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;
import static junit.framework.Assert.assertEquals;


public class HttpClientVertxTest extends TimedTesting {

    AtomicReference<String> messageBody;

    AtomicInteger responseCode;
    AtomicBoolean requestReceived;
    AtomicBoolean responseReceived;
    HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
    HttpClient client;
    HttpServer server;

    int port = 9099;

    public void connect() {

        port = PortUtils.findOpenPortStartAt(port);

        server = new HttpServerBuilder().setPort(port).build();
        client = new HttpClientBuilder().setPoolSize(1).setPort(port).build();

        requestReceived = new AtomicBoolean();
        responseReceived = new AtomicBoolean();
        messageBody = new AtomicReference<>();
        responseCode = new AtomicInteger();
        port++;

    }

    @Test
    public void testWebSocket() {

        connect();


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


        webSocket.setOpenConsumer(
                aVoid -> {

                    webSocket.sendText("What do you want on your cheeseburger?");
                }
        );

        webSocket.open();


        client.flush();


        validate();
        stop();

    }


    @Test
    public void testNewOpenWaitWebSocket() {

        connect();


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
    public void testFormSend() throws Exception {


        connect();

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mockservice/ping")
                .addParam("foo", "bar")
                .setFormPostAndCreateFormBody()
                .setTextReceiver((code, mimeType, body) -> {


                    responseCode.set(code);
                    responseReceived.set(true);


                })
                .build();


        server.setHttpRequestConsumer(serverRequest -> {
            requestReceived.set(true);
            puts("SERVER", serverRequest.getUri(), serverRequest.getBodyAsString());

            messageBody.set(serverRequest.formParams().get("foo"));
            serverRequest.getReceiver().response(200, "application/json", "\"ok\"");

        });

        run();

        client.sendHttpRequest(request);

        client.flush();

        waitForTrigger(20, o -> this.responseReceived.get());


        puts("RESPONSE", responseCode, messageBody, responseReceived);

        assertEquals("bar", messageBody.get());

        assertEquals(200, responseCode.get());

        stop();
    }


    @Test
    public void testFormSendTooLargeBody() throws Exception {


        connect();


        server = new HttpServerBuilder().setPort(port - 1)
                .addRequestBodyContinuePredicate(httpRequest -> {
                    if (httpRequest.getContentLength() > 0) {
                        httpRequest.getReceiver().response(500, "applicaiton/json", "\"too big\"");
                        return false;
                    }
                    return true;
                })
                .build();

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mockservice/ping")
                .addParam("foo", "bar")
                .setFormPostAndCreateFormBody()
                .setTextReceiver((code, mimeType, body) -> {


                    responseCode.set(code);
                    responseReceived.set(true);
                    messageBody.set(body);


                })
                .build();


        server.setHttpRequestConsumer(serverRequest -> {
            requestReceived.set(true);
            serverRequest.getReceiver().response(200, "application/json", "\"ok\"");
        });

        run();

        client.sendHttpRequest(request);

        client.flush();

        waitForTrigger(20, o -> this.responseReceived.get());


        puts("RESPONSE", responseCode, messageBody, responseReceived);


        assertEquals(500, responseCode.get());

        assertEquals("\"too big\"", messageBody.get());

        stop();
    }

    @Test
    public void testFormSendUseBody() throws Exception {


        connect();

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mockservice/ping")
                .addParam("foo", "bar")
                .setFormPostAndCreateFormBody()
                .setTextReceiver((code, mimeType, body) -> {


                    responseCode.set(code);
                    responseReceived.set(true);


                })
                .build();


        server.setHttpRequestConsumer(serverRequest -> {
            requestReceived.set(true);
            messageBody.set(serverRequest.getBodyAsString() + " " + serverRequest.getFormParams().get("foo"));
            serverRequest.getReceiver().response(200, "application/json", "\"ok\"");

        });

        run();

        client.sendHttpRequest(request);

        client.flush();

        waitForTrigger(20, o -> this.responseReceived.get());


        puts("RESPONSE", responseCode, messageBody, responseReceived);

        assertEquals("foo=bar bar", messageBody.get());

        assertEquals(200, responseCode.get());

        stop();
    }

    @Test
    public void testNewOpenWaitWebSocketNewServerStuff() {

        connect();


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


        connect();


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

        server.startServerAndWait();

        client.startClient();
    }


    private void stop() {


        client.stop();
        server.stop();

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
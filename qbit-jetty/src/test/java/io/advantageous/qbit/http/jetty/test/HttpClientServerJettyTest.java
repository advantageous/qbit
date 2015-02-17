/*******************************************************************************
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
 *  ________ __________.______________
 *  \_____  \\______   \   \__    ___/
 *   /  / \  \|    |  _/   | |    |  ______
 *  /   \_/.  \    |   \   | |    | /_____/
 *  \_____\ \_/______  /___| |____|
 *         \__>      \/
 *  ___________.__                  ____.                        _____  .__                                             .__
 *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
 *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
 *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
 *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
 *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
 *  .____    ._____.
 *  |    |   |__\_ |__
 *  |    |   |  || __ \
 *  |    |___|  || \_\ \
 *  |_______ \__||___  /
 *          \/       \/
 *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
 *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
 *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
 *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
 *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
 *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
 *  __________           __  .__              __      __      ___.
 *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
 *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
 *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
 *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
 *          \/     \/             \/     \/         \/       \/    \/
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
 *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 ******************************************************************************/

package io.advantageous.qbit.http.jetty.test;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.http.server.websocket.WebSocketMessageBuilder;
import io.advantageous.qbit.http.websocket.WebSocket;
import org.boon.core.Sys;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * @author rhightower on 2/15/15.
 */
public class HttpClientServerJettyTest {
    volatile boolean requestReceived;
    volatile boolean responseReceived;
    HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
    WebSocketMessageBuilder webSocketMessageBuilder = new WebSocketMessageBuilder();
    HttpClient client;
    HttpServer server;

    public void connect(int port) {

        client = new HttpClientBuilder().setPort(port).build();

        server = new HttpServerBuilder().setPort(port).build();

        requestReceived = false;
        responseReceived = false;

    }


    @Test
    public void testWebSocket() {

        connect(9090);


        server.setWebSocketMessageConsumer(webSocketMessage -> {

            puts(webSocketMessage.address(), webSocketMessage.body());
            if (webSocketMessage.getMessage().equals("What do you want on your cheeseburger?")) {
                webSocketMessage.getSender().sendText("Bacon");
                requestReceived = true;

            } else {
                puts("Websocket message", webSocketMessage.getMessage());
            }
        });


        run();


        final WebSocket webSocket = client.createWebSocket("/services/cheeseburger");

        webSocket.setTextMessageConsumer(message -> {
            if (message.equals("Bacon")) {
                responseReceived = true;
            }
        });


        webSocket.setOpenConsumer(aVoid -> webSocket.sendText("What do you want on your cheeseburger?"));

        webSocket.open();


        client.flush();

        Sys.sleep(1000);


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
                requestReceived = true;

            } else {
                puts("Websocket message", webSocketMessage.getMessage());
            }
        });


        run();


        final WebSocket webSocket = client.createWebSocket("/services/cheeseburger");

        webSocket.setTextMessageConsumer(message -> {
            if (message.equals("Bacon")) {
                responseReceived = true;
            }
        });

        webSocket.openAndWait();

        webSocket.sendText("What do you want on your cheeseburger?");


        client.flush();

        Sys.sleep(1000);


        validate();
        stop();

    }

    @Test
    public void testNewOpenWaitWebSocketNewServerStuff() {

        connect(9090);


        server.setWebSocketOnOpenConsumer(webSocket -> webSocket.setTextMessageConsumer(message -> {
            if (message.equals("What do you want on your cheeseburger?")) {
                webSocket.sendText("Bacon");
                requestReceived = true;
            } else {
                puts("Websocket message", message);
            }
        }));

        run();


        final WebSocket webSocket = client.createWebSocket("/services/cheeseburger");

        webSocket.setTextMessageConsumer(message -> {
            if (message.equals("Bacon")) {
                responseReceived = true;
            }
        });

        webSocket.openAndWait();

        webSocket.sendText("What do you want on your cheeseburger?");


        client.flush();

        Sys.sleep(1000);


        validate();
        stop();

    }

    @Test
    public void testHttpServerClient() throws Exception {


        connect(9191);


        server.setHttpRequestConsumer(request -> {
            requestReceived = true;
            puts("SERVER", request.getUri(), request.getBody());
            request.getReceiver().response(200, "application/json", "\"ok\"");
        });

        run();

        requestBuilder.setRemoteAddress("localhost").setMethod("GET").setUri("/client/foo");

        requestBuilder.setTextReceiver((code, mimeType, body) -> {
            responseReceived = true;

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

        Sys.sleep(500);


        if (!requestReceived) {
            die("Request not received");
        }


        if (!responseReceived) {
            die("Response not received");
        }

    }
}
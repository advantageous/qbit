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

package io.advantageous.qbit.http.server.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.concurrent.ExecutorContext;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.advantageous.qbit.concurrent.ScheduledExecutorBuilder.scheduledExecutorBuilder;
import static io.advantageous.qbit.http.server.websocket.WebSocketMessageBuilder.webSocketMessageBuilder;
import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/12/15.
 */
public class SimpleHttpServer implements HttpServer {
    private final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final QBitSystemManager systemManager;
    private final int flushInterval;
    private Consumer<WebSocketMessage> webSocketMessageConsumer = webSocketMessage -> {
    };
    private Consumer<WebSocketMessage> webSocketCloseMessageConsumer = webSocketMessage -> {
    };
    private Consumer<HttpRequest> httpRequestConsumer = request -> {
    };
    private Consumer<Void> requestIdleConsumer = aVoid -> {
    };
    private Consumer<WebSocket> webSocketConsumer = webSocket -> {

        defaultWebSocketHandler(webSocket);

    };
    private Consumer<Void> webSocketIdleConsumer = aVoid -> {
    };
    private Predicate<HttpRequest> shouldContinueHttpRequest = request -> true;
    private ExecutorContext executorContext;

    public SimpleHttpServer(QBitSystemManager systemManager, int flushInterval) {
        this.systemManager = systemManager;
        this.flushInterval = flushInterval;
    }

    public SimpleHttpServer() {
        this.systemManager = null;
        flushInterval = 50;
    }

    /**
     * Main entry point.
     *
     * @param request request to handle
     */
    public void handleRequest(final HttpRequest request) {
        if (debug) {
            puts("HttpServer::handleRequest", request);
            logger.debug("HttpServer::handleRequest" + request);
        }
        if (shouldContinueHttpRequest.test(request)) {
            httpRequestConsumer.accept(request);
        }
    }


    public void handleWebSocketMessage(final WebSocketMessage webSocketMessage) {
        webSocketMessageConsumer.accept(webSocketMessage);
    }


    public void handleWebSocketClosedMessage(WebSocketMessage webSocketMessage) {
        webSocketCloseMessageConsumer.accept(webSocketMessage);
    }

    @Override
    public void setShouldContinueHttpRequest(Predicate<HttpRequest> predicate) {
        this.shouldContinueHttpRequest = predicate;
    }

    @Override
    public void setWebSocketMessageConsumer(final Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.webSocketMessageConsumer = webSocketMessageConsumer;
    }

    @Override
    public void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketCloseMessageConsumer) {
        this.webSocketCloseMessageConsumer = webSocketCloseMessageConsumer;
    }

    @Override
    public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;
    }

    @Override
    public void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer) {
        this.requestIdleConsumer = idleConsumer;
    }

    @Override
    public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {
        this.webSocketIdleConsumer = idleConsumer;
    }


    @Override
    public void start() {

        if (debug) {
            puts("HttpServer Started");
            logger.debug("HttpServer Started");
        }

        startPeriodicFlush();
    }

    private void startPeriodicFlush() {
        if (executorContext != null) {
            throw new IllegalStateException("Can't call start twice");
        }

        executorContext = scheduledExecutorBuilder()
                .setThreadName("HttpServer")
                .setInitialDelay(flushInterval)
                .setPeriod(flushInterval)
                .setDescription("HttpServer Periodic Flush")
                .setRunnable(() -> {
                    requestIdleConsumer.accept(null);
                    webSocketIdleConsumer.accept(null);
                })
                .build();

        executorContext.start();
    }

    @Override
    public void stop() {


        if (systemManager != null) systemManager.serviceShutDown();

        if (debug) {
            puts("HttpServer Stopped");
            logger.debug("HttpServer Stopped");
        }

        if (executorContext != null) {
            executorContext.stop();
        }
    }

    public void handleWebSocketQueueIdle() {
        webSocketIdleConsumer.accept(null);
    }


    public void handleRequestQueueIdle() {
        requestIdleConsumer.accept(null);
    }

    public void handleOpenWebSocket(final WebSocket webSocket) {
        this.webSocketConsumer.accept(webSocket);
    }


    private void defaultWebSocketHandler(final WebSocket webSocket) {


        webSocket.setTextMessageConsumer(webSocketMessageIn -> {

            final WebSocketMessage webSocketMessage = webSocketMessageBuilder()
                    .setMessage(webSocketMessageIn)
                    .setUri(webSocket.uri())
                    .setRemoteAddress(webSocket.remoteAddress())
                    .setTimestamp(Timer.timer().now()).setSender(
                            message -> {
                                if (webSocket.isOpen()) {
                                    webSocket.sendText(message);
                                }

                            }).build();
            handleWebSocketMessage(webSocketMessage);

        });


        webSocket.setBinaryMessageConsumer(webSocketMessageIn -> {

            final WebSocketMessage webSocketMessage = webSocketMessageBuilder()
                    .setMessage(webSocketMessageIn)
                    .setUri(webSocket.uri())
                    .setRemoteAddress(webSocket.remoteAddress())
                    .setTimestamp(Timer.timer().now()).setSender(
                            new WebSocketSender() {
                                @Override
                                public void sendText(String message) {
                                    webSocket.sendBinary(message.getBytes(StandardCharsets.UTF_8));
                                }

                                @Override
                                public void sendBytes(byte[] message) {
                                    webSocket.sendBinary(message);
                                }
                            }

                    ).build();
            handleWebSocketMessage(webSocketMessage);

        });


        webSocket.setCloseConsumer(new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {

                long time = Timer.timer().now();

                final WebSocketMessage webSocketMessage = webSocketMessageBuilder()

                        .setUri(webSocket.uri())
                        .setRemoteAddress(webSocket.remoteAddress())
                        .setTimestamp(time).build();


                handleWebSocketClosedMessage(webSocketMessage);

            }
        });


        webSocket.setErrorConsumer(new Consumer<Exception>() {
            @Override
            public void accept(Exception e) {
                logger.error("Error with WebSocket handling", e);
            }
        });

    }


    @Override
    public void setWebSocketOnOpenConsumer(Consumer<WebSocket> onOpenConsumer) {
        this.webSocketConsumer = onOpenConsumer;
    }
}

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

package io.advantageous.qbit.http.server.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.concurrent.ExecutorContext;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.advantageous.qbit.concurrent.ScheduledExecutorBuilder.scheduledExecutorBuilder;
import static io.advantageous.qbit.http.server.websocket.WebSocketMessageBuilder.webSocketMessageBuilder;

/**
 * Captures all of the logic of an HTTP server that QBit implements, but does not
 * handle the actual wire transfer.
 *
 * @author rhightower on 2/12/15.
 */
public class SimpleHttpServer implements HttpServer {
    private final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final QBitSystemManager systemManager;
    private final int flushInterval;
    private final ServiceDiscovery serviceDiscovery;
    private final HealthServiceAsync healthServiceAsync;
    private final String name;
    private final int port;

    private Consumer<WebSocketMessage> webSocketMessageConsumer = webSocketMessage -> {
    };
    private Consumer<WebSocketMessage> webSocketCloseMessageConsumer = webSocketMessage -> {
    };
    private Consumer<HttpRequest> httpRequestConsumer = request -> {
    };
    private Consumer<Void> requestIdleConsumer = aVoid -> {
    };
    private Consumer<WebSocket> webSocketConsumer = this::defaultWebSocketHandler;
    private Consumer<Void> webSocketIdleConsumer = aVoid -> {
    };
    private Predicate<HttpRequest> shouldContinueHttpRequest = request -> true;

    private ExecutorContext executorContext;
    private Predicate<WebSocket> shouldContinueWebSocket = webSocket -> true;


    public SimpleHttpServer(
            final String endpointName,
            final QBitSystemManager systemManager,
                            final int flushInterval,
                            final int port,
                            final ServiceDiscovery serviceDiscovery,
                            final HealthServiceAsync healthServiceAsync) {


        this.name = endpointName == null ? "HTTP_SERVER_" + port : endpointName;
        this.port = port;
        this.systemManager = systemManager;
        this.flushInterval = flushInterval;
        this.serviceDiscovery = serviceDiscovery;
        this.healthServiceAsync = healthServiceAsync;
    }


    public SimpleHttpServer() {

        this.port = 8080;
        this.name = "HTTP_SERVER";
        this.systemManager = null;
        this.flushInterval = 1;
        this.serviceDiscovery = null;
        this.healthServiceAsync = null;
    }

    /**
     * Main entry point.
     *
     * @param request request to handle
     */
    public void handleRequest(final HttpRequest request) {
        if (debug) {
            System.out.println("HttpServer::handleRequest " + request);
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


    public void setShouldContinueWebSocket(Predicate<WebSocket> predicate) {
        this.shouldContinueWebSocket = predicate;
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
            logger.debug("HttpServer Started");
        }

        startPeriodicFlush();

        if (serviceDiscovery!=null) {
            serviceDiscovery.registerWithTTL(name, port, 60_000);
        }
    }

    private void startPeriodicFlush() {
        if (executorContext != null) {
            throw new IllegalStateException("Can't call startClient twice");
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

        if (serviceDiscovery!=null) {
            handleCheckIn();
        }
        requestIdleConsumer.accept(null);
    }

    final AtomicLong lastCheckIn = new AtomicLong(Timer.clockTime());

    final AtomicBoolean ok = new AtomicBoolean(true);

    private void handleCheckIn() {

        if (healthServiceAsync == null) {
            if (Timer.clockTime() - lastCheckIn.get() > 30_000) {
                lastCheckIn.set(Timer.clockTime());
                serviceDiscovery.checkInOk("HTTP_SERVER");

            }

        } else {

            if (Timer.clockTime() - lastCheckIn.get() > 10_000) {
                lastCheckIn.set(Timer.clockTime());

                healthServiceAsync.ok(ok::set);
                ServiceProxyUtils.flushServiceProxy(healthServiceAsync);

                if (ok.get()) {
                    serviceDiscovery.checkInOk(name);
                } else {
                    serviceDiscovery.checkIn(name, HealthStatus.FAIL);
                }


                ServiceProxyUtils.flushServiceProxy(serviceDiscovery);

            }
        }
    }

    public void handleOpenWebSocket(final WebSocket webSocket) {
        if (this.shouldContinueWebSocket.test(webSocket)) {
            this.webSocketConsumer.accept(webSocket);
        }
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


        webSocket.setCloseConsumer(aVoid -> {

            long time = Timer.timer().now();

            final WebSocketMessage webSocketMessage = webSocketMessageBuilder()

                    .setUri(webSocket.uri())
                    .setRemoteAddress(webSocket.remoteAddress())
                    .setTimestamp(time).build();


            handleWebSocketClosedMessage(webSocketMessage);

        });


        webSocket.setErrorConsumer(e -> logger.error("Error with WebSocket handling", e));

    }


    @Override
    public void setWebSocketOnOpenConsumer(Consumer<WebSocket> onOpenConsumer) {
        this.webSocketConsumer = onOpenConsumer;
    }
}

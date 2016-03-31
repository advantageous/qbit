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
import io.advantageous.qbit.concurrent.ExecutorContext;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.request.impl.HttpResponseCreatorDefault;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.RequestContinuePredicate;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
    final AtomicLong lastCheckIn = new AtomicLong(Timer.clockTime());
    final AtomicBoolean ok = new AtomicBoolean(true);
    private final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final QBitSystemManager systemManager;
    private final int flushInterval;
    private final ServiceDiscovery serviceDiscovery;
    private final HealthServiceAsync healthServiceAsync;
    private final String name;
    private final int port;
    private final long checkInEveryMiliDuration;
    private final CopyOnWriteArrayList<HttpResponseDecorator> decorators;
    private final HttpResponseCreator httpResponseCreator;
    private final EndpointDefinition endpointDefinition;
    protected Runnable onStart;
    protected Consumer<Throwable> errorHandler;
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
    private Predicate<HttpRequest> shouldContinueReadingRequestBody = request -> true;
    private ExecutorContext executorContext;
    private Predicate<WebSocket> shouldContinueWebSocket = webSocket -> true;

    public SimpleHttpServer(
            final String endpointName,
            final QBitSystemManager systemManager,
            final int flushInterval,
            final int port,
            final ServiceDiscovery serviceDiscovery,
            final HealthServiceAsync healthServiceAsync,
            final int serviceDiscoveryTtl,
            final TimeUnit serviceDiscoveryTtlTimeUnit,
            final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
            final HttpResponseCreator httpResponseCreator,
            final RequestContinuePredicate requestBodyContinuePredicate) {
        this.decorators = decorators;
        this.httpResponseCreator = httpResponseCreator;

        this.shouldContinueReadingRequestBody = requestBodyContinuePredicate;

        this.name = endpointName == null ? "HTTP_SERVER_" + port : endpointName;
        this.port = port;
        this.systemManager = systemManager;
        this.flushInterval = flushInterval;
        this.serviceDiscovery = serviceDiscovery;
        this.healthServiceAsync = healthServiceAsync;
        this.endpointDefinition = createEndpointDefinition(serviceDiscoveryTtl,
                serviceDiscoveryTtlTimeUnit);

        this.checkInEveryMiliDuration =
                serviceDiscoveryTtlTimeUnit.toMillis(serviceDiscoveryTtl) / 3;
    }

    public SimpleHttpServer() {

        this.port = 8080;
        this.name = "HTTP_SERVER";
        this.systemManager = null;
        this.flushInterval = 1;
        this.serviceDiscovery = null;
        this.healthServiceAsync = null;
        this.endpointDefinition = null;
        this.checkInEveryMiliDuration = 100_000;
        this.decorators = new CopyOnWriteArrayList<>();
        this.httpResponseCreator = new HttpResponseCreatorDefault();
    }

    public Runnable getOnStart() {
        if (onStart == null) {
            onStart = () -> {
            };
        }
        return onStart;
    }

    @Override
    public void setOnStart(final Runnable runnable) {
        this.onStart = runnable;
    }

    public Consumer<Throwable> getErrorHandler() {
        if (errorHandler == null) {
            errorHandler = Throwable::printStackTrace;
        }
        return errorHandler;
    }

    EndpointDefinition createEndpointDefinition(int serviceDiscoveryTtl, TimeUnit serviceDiscoveryTtlTimeUnit) {
        EndpointDefinition endpointDefinition;
        if (serviceDiscovery != null) {
            endpointDefinition = serviceDiscovery.registerWithTTL(name, port,
                    (int) serviceDiscoveryTtlTimeUnit.toSeconds(serviceDiscoveryTtl));
            serviceDiscovery.checkInOk(endpointDefinition.getId());
        } else {
            endpointDefinition = null;
        }
        return endpointDefinition;
    }

    @Override
    public void setOnError(final Consumer<Throwable> exceptionConsumer) {
        this.errorHandler = exceptionConsumer;
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
                    handleRequestQueueIdle();
                    handleWebSocketQueueIdle();
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

        if (serviceDiscovery != null) {
            handleCheckIn();
        }
        requestIdleConsumer.accept(null);
    }

    public void handleCheckIn() {

        if (healthServiceAsync == null) {
            if (Timer.clockTime() - lastCheckIn.get() > checkInEveryMiliDuration) {
                lastCheckIn.set(Timer.clockTime());
                serviceDiscovery.checkInOk(endpointDefinition.getId());
            }
        } else {

            if (Timer.clockTime() - lastCheckIn.get() > checkInEveryMiliDuration) {
                lastCheckIn.set(Timer.clockTime());

                healthServiceAsync.ok(ok::set);
                ServiceProxyUtils.flushServiceProxy(healthServiceAsync);

                if (ok.get()) {
                    serviceDiscovery.checkInOk(endpointDefinition.getId());
                } else {
                    serviceDiscovery.checkIn(endpointDefinition.getId(), HealthStatus.FAIL);
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

    public CopyOnWriteArrayList<HttpResponseDecorator> getDecorators() {
        return decorators;
    }


    public HttpResponseCreator getHttpResponseCreator() {
        return httpResponseCreator;
    }


    public Predicate<HttpRequest> getShouldContinueReadingRequestBody() {
        return shouldContinueReadingRequestBody;
    }

    public SimpleHttpServer setShouldContinueReadingRequestBody(Predicate<HttpRequest> shouldContinueReadingRequestBody) {
        this.shouldContinueReadingRequestBody = shouldContinueReadingRequestBody;
        return this;
    }
}

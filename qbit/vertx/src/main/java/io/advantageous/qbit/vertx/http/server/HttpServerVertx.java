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

package io.advantageous.qbit.vertx.http.server;


import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.HttpContentTypes;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.RequestContinuePredicate;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.util.Timer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.JksOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 */
public class HttpServerVertx implements HttpServer {

    private final Logger logger = LoggerFactory.getLogger(HttpServerVertx.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final QBitSystemManager systemManager;
    private final SimpleHttpServer simpleHttpServer;
    private final int port;
    private final String host;
    private final Vertx vertx;
    private final HttpServerOptions options;
    private final VertxServerUtils vertxUtils = new VertxServerUtils();
    private final boolean startedVertx;
    private io.vertx.core.http.HttpServer httpServer;

    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    private Object context = Sys.contextToHold();


    /**
     * For Metrics.
     */
    private volatile int exceptionCount;
    /**
     * For Metrics.
     */
    private volatile int closeCount;


    public HttpServerVertx(final boolean startedVertx,
                           final Vertx vertx,
                           final String endpointName,
                           final HttpServerOptions options,
                           final QBitSystemManager systemManager,
                           final ServiceDiscovery serviceDiscovery,
                           final HealthServiceAsync healthServiceAsync,
                           final int serviceDiscoveryTtl,
                           final TimeUnit serviceDiscoveryTtlTimeUnit,
                           final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                           final HttpResponseCreator httpResponseCreator,
                           final RequestContinuePredicate requestBodyContinuePredicate) {

        this.startedVertx = startedVertx;

        this.simpleHttpServer = new SimpleHttpServer(endpointName, systemManager,
                options.getFlushInterval(), options.getPort(), serviceDiscovery,
                healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit,
                decorators, httpResponseCreator, requestBodyContinuePredicate);
        this.vertx = vertx;
        this.systemManager = systemManager;
        this.port = options.getPort();
        this.host = options.getHost();
        this.options = BeanUtils.copy(options);
        this.setWebSocketIdleConsume(aVoid -> {
        });
        this.setHttpRequestsIdleConsumer(aVoid -> {
        });
    }


    @Override
    public void setShouldContinueHttpRequest(final Predicate<HttpRequest> shouldContinueHttpRequest) {
        this.simpleHttpServer.setShouldContinueHttpRequest(shouldContinueHttpRequest);
    }

    @Override
    public void setWebSocketMessageConsumer(final Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.simpleHttpServer.setWebSocketMessageConsumer(webSocketMessageConsumer);
    }

    @Override
    public void setWebSocketCloseConsumer(final Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.simpleHttpServer.setWebSocketCloseConsumer(webSocketMessageConsumer);
    }

    @Override
    public void setHttpRequestConsumer(final Consumer<HttpRequest> httpRequestConsumer) {
        this.simpleHttpServer.setHttpRequestConsumer(httpRequestConsumer);
    }

    @Override
    public void setHttpRequestsIdleConsumer(final Consumer<Void> idleRequestConsumer) {
        this.simpleHttpServer.setHttpRequestsIdleConsumer(
                aVoid -> {
                    idleRequestConsumer.accept(null);
                    vertxUtils.setTime(Timer.timer().now());
                }
        );
    }


    @Override
    public void setWebSocketIdleConsume(final Consumer<Void> idleWebSocketConsumer) {
        this.simpleHttpServer.setWebSocketIdleConsume(
                aVoid -> {
                    idleWebSocketConsumer.accept(null);
                    vertxUtils.setTime(Timer.timer().now());
                }
        );
    }


    @Override
    public void start() {
        startWithNotify(null);
    }

    @Override
    public void startWithNotify(final Runnable runnable) {

        simpleHttpServer.start();

        if (debug) {
            vertx.setPeriodic(10_000, event -> logger.info("Exception Count {} Close Count {}", exceptionCount, closeCount));
        }


        final io.vertx.core.http.HttpServerOptions vertxOptions = new io.vertx.core.http.HttpServerOptions();


        vertxOptions.setTcpNoDelay(options.isTcpNoDelay());
        vertxOptions.setSoLinger(options.getSoLinger());
        vertxOptions.setUsePooledBuffers(options.isUsePooledBuffers());
        vertxOptions.setReuseAddress(options.isReuseAddress());
        vertxOptions.setAcceptBacklog(options.getAcceptBackLog());
        vertxOptions.setTcpKeepAlive(options.isKeepAlive());
        vertxOptions.setCompressionSupported(options.isCompressionSupport());
        vertxOptions.setMaxWebsocketFrameSize(options.getMaxWebSocketFrameSize());
        vertxOptions.setSsl(options.isSsl());


        final JksOptions jksOptions = new JksOptions();
        jksOptions.setPath(options.getTrustStorePath());
        jksOptions.setPassword(options.getTrustStorePassword());

        vertxOptions.setTrustStoreOptions(jksOptions);
        httpServer = vertx.createHttpServer(vertxOptions);
        httpServer.websocketHandler(this::handleWebSocketMessage);
        httpServer.requestHandler(this::handleHttpRequest);

        if (Str.isEmpty(host)) {
            httpServer.listen(port, event -> {
                if (event.failed()) {
                    logger.error("HTTP SERVER unable to start on port " + port + " default host ");
                    simpleHttpServer.getErrorHandler().accept(event.cause());
                } else {

                    if (runnable != null) {
                        runnable.run();
                    }
                    logger.info("HTTP SERVER started on port " + port + " default host ");
                    simpleHttpServer.getOnStart().run();
                }
            });
        } else {
            httpServer.listen(port, host, event -> {
                if (event.failed()) {
                    logger.error("HTTP SERVER UNABLE to START on port " + port + " host " + host);
                    simpleHttpServer.getErrorHandler().accept(event.cause());
                } else {

                    if (runnable != null) runnable.run();
                    logger.info("HTTP SERVER started on port " + port + " host " + host);
                    simpleHttpServer.getOnStart().run();
                }
            });
        }

    }

    @Override
    public HttpServer startServerAndWait() {

        final Runnable onStart = simpleHttpServer.getOnStart();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean started = new AtomicBoolean();

        final Runnable ourOnStart = new Runnable() {
            @Override
            public void run() {

                started.set(true);
                latch.countDown();
                onStart.run();
            }
        };

        simpleHttpServer.setOnStart(ourOnStart);
        this.start();

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
        } finally {
            simpleHttpServer.setOnStart(onStart);
        }

        if (!started.get()) {
            throw new IllegalStateException("Unable to start server");
        }
        return this;
    }

    @Override
    public void stop() {
        simpleHttpServer.stop();
        try {
            if (httpServer != null) {

                httpServer.close();
            }

            if (startedVertx && vertx != null) {
                vertx.close();
            }
        } catch (Exception ex) {

            logger.info("HTTP SERVER unable to close " + port + " host " + host);
        }
        if (systemManager != null) systemManager.serviceShutDown();

    }


    /**
     * Handle a vertx request by converting it into a QBit request.
     *
     * @param request request
     */
    private void handleHttpRequest(final HttpServerRequest request) {


        if (debug) {
            setupMetrics(request);
            logger.debug("HttpServerVertx::handleHttpRequest::{}:{}", request.method(), request.uri());
        }

        switch (request.method().toString()) {

            case "PUT":
            case "POST":
            case "OPTIONS":
            case "TRACE":
            case "DELETE":
            case "CONNECT":
                handleRequestWithBody(request);
                break;

            case "HEAD":
            case "GET":
                handleRequestWithNoBody(request);
                break;

            default:
                throw new IllegalStateException("method not supported yet " + request.method());

        }

    }

    private void handleRequestWithNoBody(HttpServerRequest request) {
        final HttpRequest getRequest;
        getRequest = vertxUtils.createRequest(request, null, new HashMap<>(),
                simpleHttpServer.getDecorators(), simpleHttpServer.getHttpResponseCreator());
        simpleHttpServer.handleRequest(getRequest);
    }

    private void handleRequestWithBody(HttpServerRequest request) {
        final String contentType = request.headers().get("Content-Type");

        if (HttpContentTypes.isFormContentType(contentType)) {
            request.setExpectMultipart(true);
        }

        final Buffer[] bufferHolder = new Buffer[1];
        final HttpRequest bodyHttpRequest = vertxUtils.createRequest(request, () -> bufferHolder[0], new HashMap<>(),
                simpleHttpServer.getDecorators(), simpleHttpServer.getHttpResponseCreator());
        if (simpleHttpServer.getShouldContinueReadingRequestBody().test(bodyHttpRequest)) {
            request.bodyHandler((buffer) -> {
                bufferHolder[0] = buffer;
                simpleHttpServer.handleRequest(bodyHttpRequest);
            });
        } else {
            logger.info("Request body rejected {} {}", request.method(), request.absoluteURI());
        }
    }


    private void setupMetrics(final HttpServerRequest request) {

        request.exceptionHandler(event -> {

            if (debug) {
                exceptionCount++;
            }

            logger.info("EXCEPTION", event);

        });

        request.endHandler(event -> {


            if (debug) {
                closeCount++;
            }


            logger.info("REQUEST OVER");
        });
    }

    private void handleWebSocketMessage(final ServerWebSocket webSocket) {
        simpleHttpServer.handleOpenWebSocket(vertxUtils.createWebSocket(webSocket));
    }


    @Override
    public void setWebSocketOnOpenConsumer(Consumer<WebSocket> onOpenConsumer) {
        this.simpleHttpServer.setWebSocketOnOpenConsumer(onOpenConsumer);
    }


}

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

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import org.boon.Str;
import org.boon.core.reflection.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.advantageous.qbit.queue.QueueBuilder.queueBuilder;

/**
 */
public class HttpServerVertxWithQueues implements HttpServer {

    private final Logger logger = LoggerFactory.getLogger(HttpServerVertx.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final int maxRequestBatches;
    private final QBitSystemManager systemManager;
    private final SimpleHttpServer simpleHttpServer = new SimpleHttpServer(null, -1);
    private final int port;
    private final String host;
    private final int requestBatchSize;// = 50;
    private final int pollTime;// = 5;
    private final Vertx vertx;
    private final boolean manageQueues;
    private final int flushInterval;// = 100;
    private final QueueBuilder responseQueueBuilder;
    private final QueueBuilder requestQueueBuilder;
    private final QueueBuilder webSocketMessageQueueBuilder;
    volatile int exceptionCount;
    volatile int closeCount;
    volatile long id;
    private org.vertx.java.core.http.HttpServer httpServer;
    private Queue<HttpRequest> requests;
    private SendQueue<HttpRequest> httpRequestSendQueue;
    private Queue<HttpResponseInternal> responses;
    private SendQueue<HttpResponseInternal> httpResponsesSendQueue;
    private SendQueue<WebSocketMessage> webSocketMessageIncommingSendQueue;
    private ReentrantLock requestLock;
    private ReentrantLock responseLock;
    private ReentrantLock webSocketSendLock;
    private Queue<WebSocketMessage> webSocketMessageInQueue;


    public HttpServerVertxWithQueues(final Vertx vertx,
                                     final HttpServerOptions options,
                                     final QueueBuilder requestQueueBuilder,
                                     final QueueBuilder responseQueueBuilder,
                                     final QueueBuilder webSocketMessageQueueBuilder,
                                     final QBitSystemManager systemManager) {

        this.vertx = vertx;
        this.maxRequestBatches = options.getMaxRequestBatches();
        this.systemManager = systemManager;
        this.port = options.getPort();
        this.host = options.getHost();
        this.requestBatchSize = options.getRequestBatchSize();
        this.pollTime = options.getPollTime();
        this.flushInterval = options.getFlushInterval();

        if (requestQueueBuilder != null || webSocketMessageQueueBuilder != null || options.isManageQueues()) {
            this.manageQueues = true;

        } else {
            this.manageQueues = false;
        }


        if (manageQueues) {

            if (requestQueueBuilder != null) {
                this.requestQueueBuilder = BeanUtils.copy(requestQueueBuilder);

            } else {
                this.requestQueueBuilder = queueBuilder()
                        .setName("HttpServerRequests").setPollWait(pollTime).setSize(maxRequestBatches)
                        .setBatchSize(requestBatchSize);

            }
            if (responseQueueBuilder != null) {
                this.responseQueueBuilder = BeanUtils.copy(requestQueueBuilder);

            } else {
                this.responseQueueBuilder = queueBuilder()
                        .setName("HttpServerResponses").setPollWait(pollTime).setSize(maxRequestBatches)
                        .setBatchSize(requestBatchSize);

            }

            if (webSocketMessageQueueBuilder != null) {
                this.webSocketMessageQueueBuilder = BeanUtils.copy(webSocketMessageQueueBuilder);
            } else {
                this.webSocketMessageQueueBuilder = queueBuilder()
                        .setName("WebSocket").setPollWait(pollTime).setSize(maxRequestBatches)
                        .setBatchSize(requestBatchSize);
            }

        } else {
            this.requestQueueBuilder = null;
            this.responseQueueBuilder = null;
            this.webSocketMessageQueueBuilder = null;
        }
    }

    public HttpServerVertxWithQueues(HttpServerOptions options, QueueBuilder requestQueueBuilder,
                                     final QueueBuilder responseQueueBuilder,
                                     QueueBuilder webSocketMessageQueueBuilder, QBitSystemManager systemManager) {
        this(VertxFactory.newVertx(), options, requestQueueBuilder, responseQueueBuilder,
                webSocketMessageQueueBuilder, systemManager);
    }

    private static Buffer createBuffer(Object body) {
        Buffer buffer = null;

        if (body instanceof byte[]) {

            byte[] bBody = ((byte[]) body);
            buffer = new Buffer(bBody);

        } else if (body instanceof String) {

            String sBody = ((String) body);
            buffer = new Buffer(sBody, "UTF-8");
        }
        return buffer;
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
    public void setHttpRequestsIdleConsumer(Consumer<Void> idleRequestConsumer) {
        this.simpleHttpServer.setHttpRequestsIdleConsumer(idleRequestConsumer);
    }

    @Override
    public void setWebSocketIdleConsume(Consumer<Void> idleWebSocketConsumer) {
        this.simpleHttpServer.setWebSocketIdleConsume(idleWebSocketConsumer);

    }

    @Override
    public void start() {

        manageQueues();

        if (debug) {
            vertx.setPeriodic(10_000, new Handler<Long>() {
                @Override
                public void handle(Long event) {

                    logger.info("Exceptions", exceptionCount, "Close Count", closeCount);
                }
            });
        }
        httpServer = vertx.createHttpServer();

        if (manageQueues) {
            vertx.setPeriodic(flushInterval, aLong -> {


                try {
                    requestLock.lock();
                    try {
                        httpRequestSendQueue.flushSends();


                    } finally {
                        requestLock.unlock();
                    }


                    responseLock.lock();
                    try {
                        httpResponsesSendQueue.flushSends();
                    } finally {
                        responseLock.unlock();
                    }

                    webSocketSendLock.lock();

                    try {
                        webSocketMessageIncommingSendQueue.flushSends();
                    } finally {
                        webSocketSendLock.unlock();
                    }
                } catch (Exception ex) {
                    logger.error("Unable to flush", ex);
                }


            });
        }


        httpServer.setTCPNoDelay(true);//TODO this needs to be in builder
        httpServer.setSoLinger(0); //TODO this needs to be in builder
        httpServer.setUsePooledBuffers(true); //TODO this needs to be in builder
        httpServer.setReuseAddress(true); //TODO this needs to be in builder
        httpServer.setAcceptBacklog(1_000_000); //TODO this needs to be in builder
        httpServer.setTCPKeepAlive(true); //TODO this needs to be in builder
        httpServer.setCompressionSupported(false);//TODO this needs to be in builder
        httpServer.setMaxWebSocketFrameSize(100_000_000);


        httpServer.websocketHandler(this::handleWebSocketMessage);

        httpServer.requestHandler(this::handleHttpRequest);


        if (Str.isEmpty(host)) {
            httpServer.listen(port);
        } else {
            httpServer.listen(port, host);
        }


        logger.info("HTTP SERVER started on port " + port + " host " + host);


    }

    private void manageQueues() {

        if (manageQueues) {

            responseLock = new ReentrantLock();
            requestLock = new ReentrantLock();
            webSocketSendLock = new ReentrantLock();

            requests = requestQueueBuilder.setName("HTTP Requests").build();
            httpRequestSendQueue = requests.sendQueue();
            responses = responseQueueBuilder.setName("HTTP Responses").build();
            httpResponsesSendQueue = responses.sendQueue();
            webSocketMessageInQueue = webSocketMessageQueueBuilder.setName("WebSocketIn").build();
            webSocketMessageIncommingSendQueue = webSocketMessageInQueue.sendQueue();


            webSocketMessageInQueue.startListener(new ReceiveQueueListener<WebSocketMessage>() {
                @Override
                public void receive(WebSocketMessage webSocketMessage) {
                    simpleHttpServer.handleWebSocketMessage(webSocketMessage);
                }

                @Override
                public void idle() {
                    simpleHttpServer.handleWebSocketQueueIdle();

                }
            });


            responses.startListener(new ReceiveQueueListener<HttpResponseInternal>() {
                @Override
                public void receive(final HttpResponseInternal response) {
                    response.send();
                }
            });

            requests.startListener(new ReceiveQueueListener<HttpRequest>() {
                @Override
                public void receive(final HttpRequest request) {
                    simpleHttpServer.handleRequest(request);
                }

                @Override
                public void idle() {
                    simpleHttpServer.handleRequestQueueIdle();
                }
            });

        }

    }

    @Override
    public void stop() {

        try {
            if (httpServer != null) {

                httpServer.close();
            }
        } catch (Exception ex) {

            logger.info("HTTP SERVER unable to close " + port + " host " + host);
        }

        manageQueuesStop();

        if (systemManager != null) systemManager.serviceShutDown();

    }

    private void manageQueuesStop() {

        try {
            if (requests != null) {
                requests.stop();
            }

            if (responses != null) {
                responses.stop();
            }

            if (webSocketMessageInQueue != null) {
                webSocketMessageInQueue.stop();
            }

        } catch (Exception ex) {

            logger.info("Unable to shutdown queues");
        }
    }

    private void handleHttpRequest(final HttpServerRequest request) {

        request.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {

                if (debug) {
                    exceptionCount++;
                }

                logger.info("EXCEPTION", event);

            }
        });

        request.bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer event) {

                //puts("BODY PARAM", request.params().size());
            }
        });

        request.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer event) {

                //puts("DATA PARAM", request.params().size());
            }
        });

//        puts("PATH", request.path());
//
//        puts("PATH ABS URI", request.absoluteURI());


        request.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {


                if (debug) {
                    closeCount++;
                }


                logger.info("REQUEST OVER");
            }
        });


        if (debug) logger.debug("HttpServerVertx::handleHttpRequest::{}:{}", request.method(), request.uri());

        switch (request.method()) {

            case "PUT":
            case "POST":

                request.bodyHandler((Buffer buffer) -> {
                    final HttpRequest postRequest = createRequest(request, buffer);

                    if (manageQueues) {

                        sendRequestOnQueue(postRequest);

                    } else {

                        simpleHttpServer.handleRequest(postRequest);

                    }

                });
                break;


            case "HEAD":
            case "OPTIONS":
            case "DELETE":
            case "GET":
                final HttpRequest getRequest;
                getRequest = createRequest(request, null);

                if (manageQueues) {
                    sendRequestOnQueue(getRequest);

                } else {
                    simpleHttpServer.handleRequest(getRequest);
                }

                break;

            default:
                throw new IllegalStateException("method not supported yet " + request.method());

        }

    }

    private void sendRequestOnQueue(HttpRequest request) {

        requestLock.lock();
        try {
            httpRequestSendQueue.send(request);
        } finally {
            requestLock.unlock();
        }
    }

    private void sendWebSocketOnQueue(WebSocketMessage message) {

        webSocketSendLock.lock();
        try {
            webSocketMessageIncommingSendQueue.send(message);
        } finally {
            webSocketSendLock.unlock();
        }
    }

    private void handleWebSocketMessage(final ServerWebSocket webSocket) {


        webSocket.dataHandler((Buffer buffer) -> {
                    WebSocketMessage webSocketMessage =
                            createWebSocketMessage(webSocket, buffer);


                    if (debug) logger.debug("HttpServerVertx::handleWebSocketMessage::%s", webSocketMessage);

                    if (manageQueues) {
                        sendWebSocketOnQueue(webSocketMessage);
                    } else {

                        simpleHttpServer.handleWebSocketMessage(webSocketMessage);
                    }
                }
        );


        webSocket.closeHandler(event -> {
            WebSocketMessage webSocketMessage =
                    createWebSocketMessage(webSocket, null);
            simpleHttpServer.handleWebSocketClosedMessage(webSocketMessage);

        });


    }

    private WebSocketMessage createWebSocketMessage(final ServerWebSocket serverWebSocket, final Buffer buffer) {


        return createWebSocketMessage(serverWebSocket.uri(), serverWebSocket.remoteAddress().toString(),

                new WebSocketSender() {
                    @Override
                    public void sendText(String message) {
                        serverWebSocket.writeTextFrame(message);
                    }

                    @Override
                    public void sendBytes(byte[] message) {
                        serverWebSocket.writeBinaryFrame(new Buffer(message));

                    }
                }, buffer != null ? buffer.toString("UTF-8") : "");
    }

    private WebSocketMessage createWebSocketMessage(final String address, final String returnAddress, final WebSocketSender webSocketSender, final String message) {


        return new WebSocketMessage(-1L, -1L, address, message, returnAddress, webSocketSender);
    }

    private HttpRequest createRequest(final HttpServerRequest request, final Buffer buffer) {

        //puts(request.params().size(), request.absoluteURI(), request.params().get("key"), request.params().get("value"));

        final MultiMap<String, String> params = request.params().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.params());
        final MultiMap<String, String> headers = request.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.headers());
        final byte[] body = buffer == null ? "".getBytes(StandardCharsets.UTF_8) : buffer.getBytes();

        final String contentType = request.headers().get("Content-Type");

        return new HttpRequest(id++, request.path(), request.method(), params, headers, body,
                request.remoteAddress().toString(),
                contentType, createResponse(request.response()), Timer.timer().now());
    }

    private HttpResponseReceiver createResponse(final HttpServerResponse response) {
        return (code, mimeType, body) -> {

            if (manageQueues) {

                HttpResponseInternal httpResponseInternal = new HttpResponseInternal(response, code, mimeType, body);


                responseLock.lock();
                try {

                    httpResponsesSendQueue.send(httpResponseInternal);
                } finally {
                    responseLock.unlock();
                }

            } else {

                response.setStatusCode(code).putHeader("Content-Type", mimeType);
                //response.setStatusCode(code).putHeader("Keep-Alive", "timeout=600");
                Buffer buffer = createBuffer(body);
                response.end(buffer);
            }

        };
    }

    private static class HttpResponseInternal {
        final HttpServerResponse response;
        final int code;
        final String mimeType;
        final Object body;

        private HttpResponseInternal(HttpServerResponse response, int code, String mimeType, Object body) {
            this.response = response;
            this.code = code;
            this.mimeType = mimeType;
            this.body = body;
        }

        public void send() {
            response.setStatusCode(code).putHeader("Content-Type", mimeType);
            Buffer buffer = createBuffer(body);
            response.putHeader("Content-Length", Integer.toString(buffer.length()));
            //response.putHeader("Keep-Alive", "timeout=30");
            response.end(buffer);
        }

    }


}

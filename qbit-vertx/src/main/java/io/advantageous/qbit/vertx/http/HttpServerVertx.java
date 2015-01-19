package io.advantageous.qbit.vertx.http;


import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpResponse;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.queue.impl.BasicQueue;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import org.boon.Str;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 */
public class HttpServerVertx implements HttpServer {

    private final Logger logger = LoggerFactory.getLogger(HttpServerVertx.class);

    private final boolean debug = logger.isDebugEnabled();

    /**
     * I am leaving these protected and non-final so subclasses can use injection frameworks for them.
     */
    protected int port;
    protected String host;


    protected  int requestBatchSize = 50;
    protected  int pollTime = 5;

    protected Vertx vertx;

    protected boolean manageQueues = true;
    protected  int flushInterval = 100;


    private org.vertx.java.core.http.HttpServer httpServer;



    public HttpServerVertx(final int port, final String host, boolean manageQueues,
                           final int pollTime,
                           final int requestBatchSize,
                           final int flushInterval) {

        this.port = port;
        this.host = host;
        this.vertx = VertxFactory.newVertx();
        this.manageQueues = manageQueues;
        this.pollTime = pollTime;
        this.requestBatchSize = requestBatchSize;
        this.flushInterval = flushInterval;


    }

    private Consumer<WebSocketMessage> webSocketMessageConsumer = websocketMessage -> logger.debug("HttpServerVertx::DEFAULT WEB_SOCKET HANDLER CALLED WHICH IS ODD");
    private Consumer<HttpRequest> httpRequestConsumer = request -> logger.debug("HttpServerVertx::DEFAULT HTTP HANDLER CALLED WHICH IS ODD");


    private Queue<HttpRequest> requests;
    private SendQueue<HttpRequest> httpRequestSendQueue;
    private Queue<HttpResponseInternal> responses;
    private SendQueue<HttpResponseInternal> httpResponsesSendQueue;
    private SendQueue<WebSocketMessage> webSocketMessageIncommingSendQueue;
    private ReentrantLock requestLock;
    private ReentrantLock responseLock;

    private ReentrantLock webSocketSendLock;


    private Queue<WebSocketMessage> webSocketMessageInQueue;


    @Override
    public void setWebSocketMessageConsumer(final Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.webSocketMessageConsumer = webSocketMessageConsumer;
    }

    @Override
    public void setHttpRequestConsumer(final Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;
    }

    @Override
    public void start() {

        manageQueues();

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


        httpServer.setTCPKeepAlive(true);
        httpServer.setTCPNoDelay(true);
        httpServer.setSoLinger(0);
        httpServer.setUsePooledBuffers(true);
        httpServer.setReuseAddress(true);
        httpServer.setAcceptBacklog(1_000_000);
        httpServer.setTCPKeepAlive(true);
        httpServer.setCompressionSupported(true);
        httpServer.setSoLinger(1000);


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

            requests = new QueueBuilder().setName("HttpServerRequests").setPollWait(pollTime).setBatchSize(requestBatchSize).build();

            httpRequestSendQueue = requests.sendQueue();
            responses = new QueueBuilder().setName("HttpServerRequests").setPollWait(pollTime).setBatchSize(requestBatchSize).build();


            httpResponsesSendQueue = responses.sendQueue();
            webSocketMessageInQueue =

            new QueueBuilder().setName("WebSocketMessagesIn " + host + " " + port).setPollWait(pollTime).setBatchSize(requestBatchSize)
                    .setLinkTransferQueue().setCheckEvery(10).setTryTransfer(true).build();


            webSocketMessageIncommingSendQueue = webSocketMessageInQueue.sendQueue();



            webSocketMessageInQueue.startListener(new ReceiveQueueListener<WebSocketMessage>() {
                @Override
                public void receive(WebSocketMessage webSocketMessage) {

                    webSocketMessageConsumer.accept(webSocketMessage);


                }

                @Override
                public void empty() {

                }

                @Override
                public void limit() {

                }

                @Override
                public void shutdown() {

                }

                @Override
                public void idle() {

                }
            });



            responses.startListener(new ReceiveQueueListener<HttpResponseInternal>() {
                @Override
                public void receive(final HttpResponseInternal response) {
                    response.send();
                }

                @Override
                public void empty() {

                }

                @Override
                public void limit() {

                }

                @Override
                public void shutdown() {

                }

                @Override
                public void idle() {

                }
            });

            requests.startListener(new ReceiveQueueListener<HttpRequest>() {
                @Override
                public void receive(HttpRequest request) {

                    httpRequestConsumer.accept(request);
                }

                @Override
                public void empty() {

                }

                @Override
                public void limit() {

                }

                @Override
                public void shutdown() {

                }

                @Override
                public void idle() {

                }
            });

        }

    }

    @Override
    public void stop() {

        try {
            if (httpServer!=null) {

                httpServer.close();
            }
        } catch (Exception ex) {

            logger.info("HTTP SERVER unable to close " + port + " host " + host);
        }

        manageQueuesStop();


    }

    private void manageQueuesStop() {

        try {
            if (requests != null) {
                requests.stop();
            }

            if (responses != null) {
                responses.stop();
            }
        } catch (Exception ex) {

            logger.info("Unable to shutdown queues");
        }
    }

    private void handleHttpRequest(final HttpServerRequest request) {

        request.exceptionHandler( new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {

                logger.info("EXCEPTION", event);

            }
        });

        request.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {

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
                        this.httpRequestConsumer.accept(postRequest);
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
                    this.httpRequestConsumer.accept(getRequest);
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

                        this.webSocketMessageConsumer.accept(webSocketMessage);
                    }
                }
        );
    }

    private WebSocketMessage createWebSocketMessage(final ServerWebSocket webSocket, final Buffer buffer) {
        return new WebSocketMessage(webSocket.uri(), buffer.toString("UTF-8"), webSocket.remoteAddress().toString(),
                webSocket::writeTextFrame);
    }

    volatile long id;
    private HttpRequest createRequest(final HttpServerRequest request, final Buffer buffer) {

        final MultiMap<String, String> params = request.params().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.params());
        final MultiMap<String, String> headers = request.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.headers());
        final byte[] body = buffer == null ? "".getBytes(StandardCharsets.UTF_8) : buffer.getBytes();

        final String contentType = request.headers().get("Content-Type");

        return new HttpRequest(id++, request.uri(), request.method(), params, headers, body,
                request.remoteAddress().toString(),
                contentType, createResponse(request.response()), Timer.timer().now());
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

            response.putHeader("Content-Size", Integer.toString(buffer.length()));
            response.end(buffer);
        }

    }

    private HttpResponse createResponse(final HttpServerResponse response) {
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

                Buffer buffer = createBuffer(body);

                response.putHeader("Content-Size", Integer.toString(buffer.length()));

                response.end(buffer);
            }

        };
    }


}

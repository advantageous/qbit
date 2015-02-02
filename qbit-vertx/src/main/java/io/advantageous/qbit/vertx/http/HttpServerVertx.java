package io.advantageous.qbit.vertx.http;


import io.advantageous.qbit.http.*;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import io.advantageous.qbit.vertx.http.verticle.HttpServerWorkerVerticle;
import org.boon.Lists;
import org.boon.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
 */
public class HttpServerVertx implements HttpServer {

    private final Logger logger = LoggerFactory.getLogger(HttpServerVertx.class);

    private final boolean debug = logger.isDebugEnabled();
    private final int maxRequestBatches;
    private final int httpWorkers;
    private final Class handler;

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

    private Consumer<Void> idleRequestConsumer = new Consumer<Void>() {
        @Override
        public void accept(Void aVoid) {

        }
    };
    private Consumer<Void> idleWebSocketConsumer = new Consumer<Void>() {
        @Override
        public void accept(Void aVoid) {

        }
    };




    public HttpServerVertx(final int port, final String host, boolean manageQueues,
                           final int pollTime,
                           final int requestBatchSize,
                           final int flushInterval,
                           final int maxRequests,
                           final Vertx vertx) {

        this.port = port;
        this.host = host;
        this.vertx = vertx;
        this.manageQueues = manageQueues;
        this.pollTime = pollTime;
        this.requestBatchSize = requestBatchSize;
        this.flushInterval = flushInterval;
        this.maxRequestBatches = maxRequests;
        httpWorkers = -1;
        handler = null;



    }


    public HttpServerVertx(final int port, final String host, boolean manageQueues,
                           final int pollTime,
                           final int requestBatchSize,
                           final int flushInterval,
                           final int maxRequests) {


        this(port, host, manageQueues, pollTime, requestBatchSize, flushInterval, maxRequests, -1, null);


    }

    public HttpServerVertx(int port, String host, boolean manageQueues, int pollTime, int requestBatchSize, int flushInterval, int maxRequests, int httpWorkers, Class handler) {


        this.port = port;
        this.host = host;
        this.vertx = VertxFactory.newVertx();
        this.manageQueues = manageQueues;
        this.pollTime = pollTime;
        this.requestBatchSize = requestBatchSize;
        this.flushInterval = flushInterval;
        this.maxRequestBatches = maxRequests;
        this.httpWorkers = httpWorkers;
        this.handler = handler;
    }


    private Consumer<WebSocketMessage> webSocketMessageConsumer = websocketMessage -> logger.debug("HttpServerVertx::DEFAULT WEB_SOCKET HANDLER CALLED WHICH IS ODD");

    private Consumer<WebSocketMessage> webSocketCloseConsumer = webSocketMessage -> {};

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
    public void setWebSocketCloseConsumer(final Consumer<WebSocketMessage> webSocketMessageConsumer) {

        this.webSocketCloseConsumer = webSocketMessageConsumer;
    }

    @Override
    public void setHttpRequestConsumer(final Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;
    }


    @Override
    public void setHttpRequestsIdleConsumer(Consumer<Void> idleRequestConsumer) {
        this.idleRequestConsumer = idleRequestConsumer;

    }


    @Override
    public void setWebSocketIdleConsume(Consumer<Void> idleWebSocketConsumer) {
        this.idleWebSocketConsumer = idleWebSocketConsumer;

    }

    @Override
    public void start() {

        if (httpWorkers > 0 && handler!=null) {


            PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();



            JsonObject jsonObject = new JsonObject();
            jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_PORT, this.port);
            jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_FLUSH_INTERVAL, this.flushInterval);
            jsonObject.putBoolean(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_MANAGE_QUEUES, this.manageQueues);
            jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES, this.maxRequestBatches);
            jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_POLL_TIME, this.pollTime);
            jsonObject.putString(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_HOST, this.host);
            jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE, this.requestBatchSize);
            jsonObject.putString(HttpServerWorkerVerticle.HTTP_SERVER_HANDLER, handler.getName());

            URL[] urls = getClasspathUrls();



            platformManager.deployVerticle(HttpServerWorkerVerticle.class.getName(), jsonObject, urls, httpWorkers, null,
                    new Handler<AsyncResult<String>>() {
                        @Override
                        public void handle(AsyncResult<String> stringAsyncResult) {
                            if (stringAsyncResult.succeeded()) {
                                puts("Launched verticle");
                            }
                        }
                    }
            );
        } else {

            manageQueues();

            if (debug) {
                vertx.setPeriodic(10_000, new Handler<Long>() {
                    @Override
                    public void handle(Long event) {

                        puts ("Exceptions", exceptionCount, "Close Count", closeCount);
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



            httpServer.setTCPKeepAlive(true);
            httpServer.setTCPNoDelay(true);
            httpServer.setSoLinger(0);
            httpServer.setUsePooledBuffers(true);
            httpServer.setReuseAddress(true);
            httpServer.setAcceptBacklog(1_000_000);
            httpServer.setTCPKeepAlive(true);
            httpServer.setCompressionSupported(true);
            httpServer.setSoLinger(1000);
            httpServer.setMaxWebSocketFrameSize(100_000_000);



            httpServer.websocketHandler(this::handleWebSocketMessage);

            httpServer.requestHandler(this::handleHttpRequest);




            if (Str.isEmpty(host)) {
                httpServer.listen(port);
            } else {
                httpServer.listen(port, host);
            }

        }

        logger.info("HTTP SERVER started on port " + port + " host " + host);




    }

    private URL[] getClasspathUrls() {


        final String classpathString = System.getProperty("java.class.path");

        final List<String> classpathStrings = Lists.list(Str.split(classpathString, ':'));

        final List<URL> urlList = new ArrayList<>(classpathStrings.size());

        for (String path : classpathStrings) {
            File file = new File(path);
            try {
                urlList.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return urlList.toArray(new URL[urlList.size()]);
    }

    private void manageQueues() {

        if (manageQueues) {

            responseLock = new ReentrantLock();
            requestLock = new ReentrantLock();
            webSocketSendLock = new ReentrantLock();

            requests = new QueueBuilder().setName("HttpServerRequests").setPollWait(pollTime).setSize(maxRequestBatches).setBatchSize(requestBatchSize).build();

            httpRequestSendQueue = requests.sendQueue();
            responses = new QueueBuilder().setName("HTTP Responses").setPollWait(pollTime).setBatchSize(requestBatchSize).build();


            httpResponsesSendQueue = responses.sendQueue();
            webSocketMessageInQueue =

            new QueueBuilder().setName("WebSocketMessagesIn " + host + " " + port)
                    .setPollWait(pollTime).setBatchSize(requestBatchSize).setSize(maxRequestBatches)
                    .build();


            webSocketMessageIncommingSendQueue = webSocketMessageInQueue.sendQueue();



            webSocketMessageInQueue.startListener(new ReceiveQueueListener<WebSocketMessage>() {
                @Override
                public void receive(WebSocketMessage webSocketMessage) {

                    webSocketMessageConsumer.accept(webSocketMessage);


                }

                @Override
                public void idle() {

                    idleWebSocketConsumer.accept(null);
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
                public void receive(HttpRequest request) {

                    httpRequestConsumer.accept(request);
                }

                @Override
                public void idle() {

                    idleRequestConsumer.accept(null);
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

    volatile int exceptionCount;
    volatile int closeCount;

    private void handleHttpRequest(final HttpServerRequest request) {

        request.exceptionHandler( new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {

                if (debug) {
                    exceptionCount++;
                }

                logger.info("EXCEPTION", event);

            }
        });

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



        webSocket.closeHandler(event -> {


            WebSocketMessage webSocketMessage =
                    createWebSocketMessage(webSocket, null);

            webSocketCloseConsumer.accept(webSocketMessage);

        });


    }

    private WebSocketMessage createWebSocketMessage(final ServerWebSocket serverWebSocket, final Buffer buffer) {


        return createWebSocketMessage(serverWebSocket.uri(), serverWebSocket.remoteAddress().toString(), serverWebSocket::writeTextFrame, buffer != null ? buffer.toString("UTF-8"): "");
    }


    private WebSocketMessage createWebSocketMessage(final String address, final String returnAddress, final WebSocketSender webSocketSender, final String message) {


        return new WebSocketMessage(-1L, -1L, address, message, returnAddress, webSocketSender);
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
            response.putHeader("Content-Length", Integer.toString(buffer.length()));
            //response.putHeader("Keep-Alive", "timeout=30");
            response.end(buffer);
        }

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


}

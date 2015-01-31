package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import org.boon.core.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.WebSocket;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/30/15.
 */
public class HttpVertxClient implements HttpClient {


    private final Logger logger = LoggerFactory.getLogger(HttpVertxClient.class);
    private final boolean debug = logger.isDebugEnabled();


    protected ScheduledExecutorService scheduledExecutorService;

    /**
     * I am leaving these protected and non-final so subclasses can use injection frameworks for them.
     */
    protected  int port;
    protected int requestBatchSize=50;
    protected  String host;
    protected  int timeOutInMilliseconds;
    protected  int poolSize;
    protected org.vertx.java.core.http.HttpClient httpClient;
    protected Vertx vertx;
    protected final boolean keepAlive;
    protected final boolean pipeline;
    protected  final int flushInterval = 20000;


    private final Map<String, WebSocket> webSocketMap = new ConcurrentHashMap<>();



    /**
     * Are we closed.
     */
    private final AtomicBoolean closed = new AtomicBoolean();
    private Consumer<Void> periodicFlushCallback = aVoid -> {

    };
    private boolean autoFlush;


    public HttpVertxClient(String host, int port, int pollTime, int requestBatchSize, int timeOutInMilliseconds, int poolSize,
                                     boolean autoFlush, boolean keepAlive, boolean pipeline) {

        this.port = port;
        this.host = host;
        this.timeOutInMilliseconds = timeOutInMilliseconds;
        this.poolSize = poolSize;
        this.vertx = VertxFactory.newVertx();
        this.requestBatchSize = requestBatchSize;
        this.poolSize = poolSize;
        this.keepAlive=keepAlive;
        this.pipeline=pipeline;
        this.autoFlush = autoFlush;

    }

    @Override
    public void sendHttpRequest(final HttpRequest request) {
        if(debug) logger.debug("HTTP CLIENT: sendHttpRequest:: \n{}\n", request);


        final HttpClientRequest httpClientRequest = httpClient.request(
                request.getMethod(), request.getUri(),
                httpClientResponse -> handleResponse(request, httpClientResponse));

        final MultiMap<String, String> headers = request.getHeaders();

        if (headers!=null) {

            for (String key : headers.keySet()) {
                httpClientRequest.putHeader(key, headers.getAll(key));
            }
        }

        final byte[] body = request.getBody();

        if (keepAlive) {
            httpClientRequest.putHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE);
        }


        if (body != null && body.length > 0) {


            httpClientRequest.putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(body.length));
            if (request.getContentType()!=null) {


                httpClientRequest.putHeader("Content-Type", request.getContentType());
            }
            httpClientRequest.end(new Buffer(request.getBody()));

        } else {
            httpClientRequest.end();
        }

        if (debug) logger.debug("HttpClientVertx::SENT \n{}", request);

    }

    @Override
    public void sendWebSocketMessage(final WebSocketMessage webSocketMessage) {
        final String uri = webSocketMessage.getUri();

        WebSocket webSocket = webSocketMap.get(uri);

        if (webSocket!=null) {
            try {
                webSocket.writeTextFrame(webSocketMessage.getMessage());
            } catch (Exception ex) {
                connectWebSocketAndSend(webSocketMessage);
            }
        } else {
            connectWebSocketAndSend(webSocketMessage);
        }

    }

    @Override
    public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {
        this.periodicFlushCallback = periodicFlushCallback;
    }



    @Override
    public void stop() {

        try {
            if (this.scheduledExecutorService!=null)
                this.scheduledExecutorService.shutdown();
        } catch (Exception ex) {
            logger.warn("problem shutting down executor client for Http Client", ex);
        }


        try {
            if (httpClient != null) {
                httpClient.close();
            }
        }catch (Exception ex) {

            logger.warn("problem shutting down vertx httpClient for QBIT Http Client", ex);
        }

    }

    private void autoFlush() {
        periodicFlushCallback.accept(null);
    }
    @Override
    public HttpClient start() {
        connect();

        scheduledExecutorService = Executors.newScheduledThreadPool(2);

        if (autoFlush) {
            this.scheduledExecutorService.scheduleAtFixedRate(this::autoFlush, 0, flushInterval, TimeUnit.MILLISECONDS);
        }
        return this;

    }




    private void connectWebSocketAndSend(final WebSocketMessage webSocketMessage) {


        final String uri = webSocketMessage.getUri();

        WebSocket webSocket = webSocketMap.get(uri);

        if (webSocket == null) {

            final BlockingQueue<WebSocket> connectQueue = new ArrayBlockingQueue<WebSocket>(1);

            httpClient.connectWebsocket(uri, new Handler<WebSocket>(){
                @Override
                public void handle(final WebSocket webSocket) {

                    webSocketMap.put(uri, webSocket);

                    connectQueue.offer(webSocket);


                    webSocket.dataHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {

                            webSocketMessage.getSender().send(buffer.toString());
                        }
                    });

                    webSocket.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {
                            logger.debug("Closed WebSocket " + uri);

                            webSocketMap.remove(uri);
                        }
                    });

                    webSocket.exceptionHandler(new Handler<Throwable>() {
                        @Override
                        public void handle(Throwable event) {
                            logger.warn("Problem with WebSocket connection " + uri, event);
                        }
                    });

                    webSocket.endHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {

                            logger.debug("End WebSocket Message" + uri);
                        }
                    });

                }
            });


            try {
                webSocket = connectQueue.poll(timeOutInMilliseconds, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Unable to get connection interrupted thread", e);

            }

        }

        if (webSocket!=null) {
            webSocket.writeTextFrame(webSocketMessage.getMessage());

        }

    }

    @Override
    public void flush() {
    }


    volatile long responseCount=0;

    private void handleResponse(final HttpRequest request, final HttpClientResponse httpClientResponse) {
        final int statusCode = httpClientResponse.statusCode();
        final MultiMap<String, String> headers = httpClientResponse.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(httpClientResponse.headers());



        if (debug) {
            responseCount++;
            puts("status code", httpClientResponse.statusCode(), responseCount);
        }

        httpClientResponse.bodyHandler(buffer -> {
            final String body = buffer.toString("UTF-8");


            if (debug) {
                puts("got body", "BODY");
            }

            handleResponseFromServer(request, statusCode, headers, body);
        });
    }

    private void handleResponseFromServer(HttpRequest request, int responseStatusCode, MultiMap<String, String> responseHeaders, String body) {
        if(debug) {
            logger.debug("HttpClientVertx::handleResponseFromServer:: request = {}, response status code = {}, \n" +
                    "response headers = {}, body = {}", request, responseStatusCode, responseHeaders, body);
        }
        request.getResponse().response(responseStatusCode, responseHeaders.get("Content-Type"), body);
    }

    private void connect() {
        httpClient = vertx.createHttpClient().setHost(host).setPort(port)
                .setConnectTimeout(timeOutInMilliseconds).setMaxPoolSize(poolSize)
                .setKeepAlive(keepAlive).setPipelining(pipeline)
                .setSoLinger(100)
                .setTCPNoDelay(false)
                .setMaxWebSocketFrameSize(100_000_000)
                .setConnectTimeout(this.timeOutInMilliseconds);


        httpClient.setUsePooledBuffers(true);

        if(debug) logger.debug("HTTP CLIENT: connect:: \nhost {} \nport {}\n", host, port);

        httpClient.exceptionHandler(throwable -> {

            if (throwable instanceof ConnectException) {
                closed.set(true);
            } else {
                logger.error("Unable to connect to " + host + " port " + port, throwable);
            }
        });

        Sys.sleep(100);

    }
}

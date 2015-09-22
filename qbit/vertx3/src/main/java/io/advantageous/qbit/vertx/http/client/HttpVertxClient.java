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

package io.advantageous.qbit.vertx.http.client;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.primitive.CharBuf;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.concurrent.ExecutorContext;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.network.NetSocket;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import io.advantageous.qbit.vertx.http.util.VertxCreate;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.net.JksOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.qbit.concurrent.ScheduledExecutorBuilder.scheduledExecutorBuilder;
import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;

/**
 * @author rhightower on 1/30/15.
 */
public class HttpVertxClient implements HttpClient {

    protected final boolean keepAlive;
    protected final boolean pipeline;
    protected final int flushInterval;
    private final Logger logger = LoggerFactory.getLogger(HttpVertxClient.class);
    private final boolean debug = logger.isDebugEnabled() || GlobalConstants.DEBUG;
    private final boolean trace = logger.isTraceEnabled();


    /**
     * Are we closed.
     */
    private final AtomicBoolean closed = new AtomicBoolean();
    /**
     * I am leaving these protected and non-final so subclasses can use injection frameworks for them.
     */
    protected final int port;
    protected final String host;
    protected final int timeOutInMilliseconds;
    private final boolean ssl;
    private final String trustStorePath;
    private final String trustStorePassword;
    private final boolean trustAll;
    private final boolean verifyHost;
    private final int maxWebSocketFrameSize;
    private final boolean tryUseCompression;
    private final boolean tcpNoDelay;
    private final int soLinger;
    protected int poolSize;
    protected io.vertx.core.http.HttpClient httpClient;
    protected final Vertx vertx;
    volatile long responseCount = 0;
    private ExecutorContext executorContext;
    private final boolean autoFlush;
    private Consumer<Void> periodicFlushCallback = aVoid -> {
    };

    public HttpVertxClient(final String host,
                           final int port,
                           final int timeOutInMilliseconds,
                           final int poolSize,
                           final boolean autoFlush,
                           final int flushInterval,
                           final boolean keepAlive,
                           final boolean pipeline,
                           final boolean ssl,
                           final boolean verifyHost,
                           final boolean trustAll,
                           final int maxWebSocketFrameSize,
                           final boolean tryUseCompression,
                           final String trustStorePath,
                           final String trustStorePassword,
                           final boolean tcpNoDelay,
                           final int soLinger) {

        this.flushInterval = flushInterval;
        this.port = port;
        this.host = host;
        this.timeOutInMilliseconds = timeOutInMilliseconds;
        this.poolSize = poolSize;
        this.vertx = VertxCreate.newVertx();
        this.poolSize = poolSize;
        this.keepAlive = keepAlive;
        this.pipeline = pipeline;
        this.autoFlush = autoFlush;
        this.ssl = ssl;
        this.verifyHost = verifyHost;
        this.trustAll = trustAll;
        this.maxWebSocketFrameSize = maxWebSocketFrameSize;
        this.tryUseCompression = tryUseCompression;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.tcpNoDelay = tcpNoDelay;
        this.soLinger = soLinger;

    }



    @Override
    public void sendHttpRequest(final HttpRequest request) {

        checkClosed();

        if (trace) {
            logger.debug(sputs("HTTP CLIENT: sendHttpRequest:: \n{}\n", request, "\nparams\n", request.params()));
        }

        String uri = getURICreateParamsIfNeeded(request);

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

        final HttpClientRequest httpClientRequest = httpClient.request(
                httpMethod, uri,
                httpClientResponse -> handleResponse(request, httpClientResponse));

        final MultiMap<String, String> headers = request.getHeaders();

        if (headers != null) {

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
            if (request.getContentType() != null) {


                httpClientRequest.putHeader("Content-Type", request.getContentType());
            }
            httpClientRequest.end( Buffer.buffer(request.getBody()));

        } else {
            httpClientRequest.end();
        }

        if (trace) logger.trace("HttpClientVertx::SENT \n{}", request);

    }

    private String getURICreateParamsIfNeeded(HttpRequest request) {

        String uri = request.getUri();

        final MultiMap<String, String> params = request.params();

        if (params != null && params.size() > 0) {
            CharBuf charBuf = CharBuf.create(request.getUri().length() + params.size() * 10);

            charBuf.add(request.getUri()).add("?");

            for (Map.Entry<String, Collection<String>> entry : params) {
                try {
                    String key = URLEncoder.encode(entry.getKey(), "UTF-8");

                    final Collection<String> values = entry.getValue();

                    for (String val : values) {
                        val = URLEncoder.encode(val, "UTF-8");

                        charBuf.addString(key).add('=').addString(val).add('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
            }

            charBuf.removeLastChar();
            uri = charBuf.toString();
        }
        return uri;
    }

    @Override
    public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {
        this.periodicFlushCallback = periodicFlushCallback;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void stop() {

        this.closed.set(true);

        if (executorContext != null) {
            executorContext.stop();
            executorContext = null;
        }

        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception ex) {

            logger.debug("problem shutting down vertx httpClient for QBIT Http Client", ex);
        }

        if (vertx != null) {
            try {
                vertx.close();

            } catch (Exception ex) {
                logger.debug("problem shutting down vertx for QBIT Http Client", ex);

            }
        }

    }

    private void autoFlush() {
        periodicFlushCallback.accept(null);
    }

    @Override
    public HttpClient startClient() {
        connect();
        if (autoFlush) {

            if (executorContext != null) {
                throw new IllegalStateException(sputs("Unable to startClient up Vertx client, it is already started"));
            }

            this.executorContext = scheduledExecutorBuilder()
                    .setThreadName("HttpClient")
                    .setInitialDelay(50)
                    .setPeriod(this.flushInterval).setRunnable(this::autoFlush)
                    .build();

            executorContext.start();
        }
        return this;
    }

    @Override
    public WebSocket createWebSocket(final String uri) {

        final String remoteAddress = Str.add("ws://", host, ":", Integer.toString(port), uri);
        return webSocketBuilder().setUri(uri).setWebSocketSender(createWebSocketSender(uri))
                .setRemoteAddress(remoteAddress).build();

    }

    private WebSocketSender createWebSocketSender(String uri) {
        return new WebSocketSender() {
            volatile io.vertx.core.http.WebSocket vertxWebSocket;

            @Override
            public void sendText(String message) {
                vertxWebSocket.writeFinalTextFrame(message);
            }

            @Override
            public void openWebSocket(WebSocket webSocket) {

                httpClient.websocket(uri, vertxWebSocket -> {
                    this.vertxWebSocket = vertxWebSocket;

                    /* Handle on Message. */
                    vertxWebSocket.handler(
                            buffer -> webSocket.onTextMessage(buffer.toString("UTF-8"))
                    );

                    /* Handle onClose */
                    vertxWebSocket.closeHandler(event -> webSocket.onClose());

                    /* Handle on Exception. */
                    vertxWebSocket.exceptionHandler(event -> {
                        if (event instanceof Exception) {
                            webSocket.onError((Exception) event);
                        } else {
                            webSocket.onError(new Exception(event));
                        }
                    });

                    /* Handle onOpen. */
                    webSocket.onOpen();

                });
            }

            @Override
            public void open(NetSocket netSocket) {
                openWebSocket((WebSocket) netSocket);
            }

            @Override
            public void sendBytes(byte[] message) {
                vertxWebSocket.writeFinalBinaryFrame(Buffer.buffer(message));
            }
        };
    }

    @Override
    public void flush() {
    }

    private void handleResponse(final HttpRequest request, final HttpClientResponse httpClientResponse) {
        final int statusCode = httpClientResponse.statusCode();
        final MultiMap<String, String> headers = httpClientResponse.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(httpClientResponse.headers());

        if (debug) {
            responseCount++;
            puts("status code", httpClientResponse.statusCode(), responseCount);
        }

        httpClientResponse.bodyHandler(buffer -> {

            if (request.getReceiver().isText()) {
                final String body = buffer.toString("UTF-8");

                if (debug) {
                    puts("got body", "BODY");
                }

                handleResponseFromServer(request, statusCode, headers, body);
            } else {
                final byte[] body = buffer.getBytes();
                handleResponseFromServerBytes(request, statusCode, headers, body);

            }
        });
    }

    private void handleResponseFromServer(
            final HttpRequest request,
            final int responseStatusCode,
            final MultiMap<String, String> responseHeaders,
            final String body) {
        if (debug) {
            logger.debug("HttpClientVertx::handleResponseFromServer:: request = {}, response status code = {}, \n" +
                    "response headers = {}, body = {}", request, responseStatusCode, responseHeaders, body);
        }
        final HttpResponseReceiver<Object> receiver = request.getReceiver();
        final String contentType = responseHeaders.get("Content-Type");
        receiver.response(responseStatusCode, contentType, body, responseHeaders);
    }


    private void handleResponseFromServerBytes(
            final HttpRequest request,
            final int responseStatusCode,
            final MultiMap<String, String> responseHeaders,
            final byte[] body) {
        if (debug) {
            logger.debug("HttpClientVertx::handleResponseFromServerBytes:: request = {}, response status code = {}, \n" +
                    "response headers = {}, body = {}", request, responseStatusCode, responseHeaders, body);
        }
        request.getReceiver().response(responseStatusCode,
                responseHeaders.get("Content-Type"), body, responseHeaders);
    }


    private void connect() {

        /*
            private final boolean ssl;
    private final boolean verifyHost;
    private final boolean trustAll;
    private final int maxWebSocketFrameSize;
    private final boolean tryUseCompression;
    private final String trustStorePath;
    private final boolean tcpNoDelay;
    private final int soLinger;

         */
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        final JksOptions jksOptions = new JksOptions();
        jksOptions.setPath(trustStorePath).setPassword(trustStorePassword);

        httpClientOptions.setDefaultHost(host).setDefaultPort(port)
                .setConnectTimeout(timeOutInMilliseconds)
                .setMaxPoolSize(poolSize)
                .setKeepAlive(keepAlive)
                .setPipelining(pipeline)
                .setSoLinger(soLinger)
                .setTcpNoDelay(tcpNoDelay)
                .setTryUseCompression(tryUseCompression)
                .setSsl(ssl)
                .setTrustAll(trustAll)
                .setVerifyHost(verifyHost)
                .setMaxWebsocketFrameSize(maxWebSocketFrameSize)
                .setUsePooledBuffers(true);



        httpClient = vertx.createHttpClient(httpClientOptions);



        if (debug) logger.debug("HTTP CLIENT: connect:: \nhost {} \nport {}\n", host, port);
//
//        httpClient.exceptionHandler(throwable -> {
//
//            if (throwable instanceof ConnectException) {
//                closed.set(true);
//                try {
//                    stop();
//                } catch (Exception ex) {
//                    logger.warn("Unable to stop client " +
//                            "after failed connection", ex);
//                }
//            } else {
//                logger.error("Unable to connect to " + host + " port " + port, throwable);
//            }
//        });

//        Sys.sleep(100);

        closed.set(false);

    }


    public boolean isClosed() {
        return closed.get();
    }


    /* NOTE: There is a better way to do this.
    * Going to use Phantom references which are a
    * better way to keep track of which objects are being freed.
    **/
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!this.closed.get()) {
            logger.warn("we detected a connection that " +
                    "was not closed host " + host + " port " + port);
            try {
                stop();
            } catch (Exception ex) {
                logger.warn("Problem closing client in finalize", ex);
            }
        }
    }

    @Override
    public void start() {
        startClient();
    }
}

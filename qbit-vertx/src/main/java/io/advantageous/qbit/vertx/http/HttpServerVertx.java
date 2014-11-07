package io.advantageous.qbit.vertx.http;


import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpResponse;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import org.boon.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;

import java.util.function.Consumer;

import static org.boon.Boon.puts;

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

    /*
    how often do we want a call on the HTTP request thread so we can perform cleanup or flush in milis.
     */
    protected long timeCallbackDuration = 50;
    protected Vertx vertx;

    private org.vertx.java.core.http.HttpServer httpServer;


    /**
     * Constructor
     *
     * @param port port
     * @param host host
     * @param vertx vertx
     */
    public HttpServerVertx(final int port, final String host, final Vertx vertx) {
        this.port = port;
        this.host = host;
        this.vertx = vertx;
    }

    public HttpServerVertx(final int port) {
        this(port, "localhost", VertxFactory.newVertx());
    }


    public HttpServerVertx(final int port, final String host) {
        this(port, host, VertxFactory.newVertx());
    }

    private Consumer<WebSocketMessage> webSocketMessageConsumer = websocketMessage -> logger.debug("HttpServerVertx::DEFAULT WEBSOCKET HANDLER CALLED WHICH IS ODD");


    private Consumer<HttpRequest> httpRequestConsumer = request -> logger.debug("HttpServerVertx::DEFAULT HTTP HANDLER CALLED WHICH IS ODD");



    @Override
    public void setWebSocketMessageConsumer(final Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.webSocketMessageConsumer = webSocketMessageConsumer;
    }

    @Override
    public void setHttpRequestConsumer(final Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;
    }

    @Override
    public void run() {

        httpServer = vertx.createHttpServer();

        httpServer.setTCPKeepAlive(true);
        httpServer.setTCPNoDelay(true);
        httpServer.setSoLinger(0);
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

    @Override
    public void stop() {

        try {
            if (httpServer!=null) {

                httpServer.close();
            }
        } catch (Exception ex) {

            logger.info("HTTP SERVER unable to close " + port + " host " + host);
        }


    }

    private void handleHttpRequest(final HttpServerRequest request) {


        if (debug) logger.debug("HttpServerVertx::handleHttpRequest::{}:{}", request.method(), request.uri());

        switch (request.method()) {

            case "PUT":
            case "POST":

                request.dataHandler((Buffer buffer) -> {
                    final HttpRequest postRequest;
                    postRequest = createRequest(request, buffer);
                    this.httpRequestConsumer.accept(postRequest);
                });
                break;


            case "HEAD":
            case "OPTIONS":
            case "DELETE":
            case "GET":
                final HttpRequest getRequest;
                getRequest = createRequest(request, null);
                this.httpRequestConsumer.accept(getRequest);
                break;

            default:
                throw new IllegalStateException("method not supported yet " + request.method());

        }

    }

    private void handleWebSocketMessage(final ServerWebSocket webSocket) {


        webSocket.dataHandler((Buffer buffer) -> {
                    WebSocketMessage webSocketMessage =
                            createWebSocketMessage(webSocket, buffer);


                    if (debug) logger.debug("HttpServerVertx::handleWebSocketMessage::%s", webSocketMessage);

                    this.webSocketMessageConsumer.accept(webSocketMessage);
                }
        );
    }

    private WebSocketMessage createWebSocketMessage(final ServerWebSocket webSocket, final Buffer buffer) {
        return new WebSocketMessage(webSocket.uri(), buffer.toString("UTF-8"), webSocket.remoteAddress().toString(),
                webSocket::writeTextFrame);
    }

    private HttpRequest createRequest(final HttpServerRequest request, final Buffer buffer) {

        final MultiMap<String, String> params = request.params().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.params());
        final MultiMap<String, String> headers = request.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.headers());
        final String body = buffer == null ? "" : buffer.toString("UTF-8");

        return new HttpRequest(request.uri(), request.method(), params, headers, body,
                request.remoteAddress().toString(),
                createResponse(request.response()));
    }


    private HttpResponse createResponse(final HttpServerResponse response) {
        return (code, mimeType, body) -> {
            response.setStatusCode(code).putHeader("Content-Type", mimeType);
            response.end(body, "UTF-8");
        };
    }


}

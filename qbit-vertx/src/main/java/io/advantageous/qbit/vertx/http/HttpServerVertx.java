package io.advantageous.qbit.vertx.http;



import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpResponse;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.vertx.example.vertx.MultiMapWrapper;
import org.boon.Str;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;

import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
 */
public class HttpServerVertx implements HttpServer {


    /**
     * I am leaving these protected and non-final so subclasses can use injection frameworks for them.
     */
    protected int port;
    protected String host;
    protected Vertx vertx;

    private org.vertx.java.core.http.HttpServer httpServer;



    /**
     * Constructor
     * @param port port
     */
    public HttpServerVertx( int port, String host, Vertx vertx ) {
        this.port = port;
        this.host = host;
        this.vertx = vertx;
    }


    public HttpServerVertx( int port ) {
        this(port, null, VertxFactory.newVertx());
    }

    private Consumer<WebSocketMessage> webSocketMessageConsumer = websocketMessage -> {

    };


    private Consumer<HttpRequest> httpRequestConsumer = request -> {

    };


    @Override
    public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.webSocketMessageConsumer = webSocketMessageConsumer;
    }

    @Override
    public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
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

        puts("HTTP SERVER started on port", port, " host ", host);

    }

    private void handleHttpRequest(HttpServerRequest request) {
        request.dataHandler((Buffer buffer) -> {

            HttpRequest httpRequest = createRequest(request, buffer);

            this.httpRequestConsumer.accept(httpRequest);

        });

    }

    private void handleWebSocketMessage(ServerWebSocket webSocket) {


        webSocket.dataHandler((Buffer buffer) -> {
                    WebSocketMessage webSocketMessage =
                            createWebSocketMessage(webSocket, buffer);
                    this.webSocketMessageConsumer.accept(webSocketMessage);
                }
        );
    }

    private WebSocketMessage createWebSocketMessage(ServerWebSocket webSocket, Buffer buffer) {
        return new WebSocketMessage(webSocket.uri(), buffer.toString("UTF-8"), webSocket.remoteAddress().toString(),
                webSocket::writeTextFrame);
    }

    private HttpRequest createRequest(HttpServerRequest request, Buffer buffer) {
        return new HttpRequest(request.uri(), request.method(),
                new MultiMapWrapper(request.params()), buffer.toString("UTF-8"),
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

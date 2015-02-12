package io.advantageous.qbit.servlet;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by rhightower on 2/12/15.
 */
public class ServletHttpServer implements HttpServer {

    private Consumer<WebSocketMessage> webSocketMessageConsumer = webSocketMessage -> {

    };
    private Consumer<WebSocketMessage> webSocketCloseMessageConsumer = webSocketMessage -> {

    };
    private Consumer<HttpRequest> httpRequestConsumer = request -> {

    };
    private Consumer<Void> requestIdleConsumer = aVoid -> {};
    private Consumer<Void> webSocketIdleConsumer = aVoid -> {};
    private Predicate<HttpRequest> shouldContinueHttpRequest = request -> true;

    @Override
    public void setShouldContinueHttpRequest(Predicate<HttpRequest> predicate) {
        this.shouldContinueHttpRequest = predicate;
    }

    public void handleRequest(final HttpRequest request) {
        if (shouldContinueHttpRequest.test(request)) {
            httpRequestConsumer.accept(request);
        }
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

    }

    @Override
    public void stop() {

    }
}

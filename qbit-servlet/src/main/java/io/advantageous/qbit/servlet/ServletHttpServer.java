package io.advantageous.qbit.servlet;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;

import java.util.concurrent.*;
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
    private ScheduledFuture<?> future;

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

    private ScheduledExecutorService monitor;

    @Override
    public void start() {


        monitor = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("ServletHttpServer Flush Thread" );
                    return thread;
                }
        );

        /** This wants to be configurable. */
        future = monitor.scheduleAtFixedRate(() -> {
            try {
                requestIdleConsumer.accept(null);
                webSocketIdleConsumer.accept(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                //logger.error("blah blah Manager::Problem running queue manager", ex); //TODO log this
            }
        }, 50, 50, TimeUnit.MILLISECONDS);


    }

    @Override
    public void stop() {

        if (future!=null) {
            future.cancel(true);
        }

        if (monitor!=null) {
            monitor.shutdown();
        }
    }
}

package io.advantageous.qbit.vertx.http.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.HttpContentTypes;
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
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class SimpleVertxHttpServerWrapper implements HttpServer {

    private final Logger logger = LoggerFactory.getLogger(SimpleVertxHttpServerWrapper.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final QBitSystemManager systemManager;
    private final SimpleHttpServer simpleHttpServer;
    private final VertxServerUtils vertxUtils = new VertxServerUtils();
    private final Vertx vertx;
    private final io.vertx.core.http.HttpServer httpServer;
    private final Router router;
    private final Route route;

    /**
     * For Metrics.
     */
    private volatile int exceptionCount;
    /**
     * For Metrics.
     */
    private volatile int closeCount;

    public SimpleVertxHttpServerWrapper(
            final io.vertx.core.http.HttpServer httpServer,
            final Router router,
            final Route route,
            final int flushInterval,
            final String endpointName,
            final Vertx vertx,
            final QBitSystemManager systemManager,
            final ServiceDiscovery serviceDiscovery,
            final HealthServiceAsync healthServiceAsync,
            final int serviceDiscoveryTtl,
            final TimeUnit serviceDiscoveryTtlTimeUnit,
            final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
            final HttpResponseCreator httpResponseCreator,
            final RequestContinuePredicate requestBodyContinuePredicate) {

        this.router = router;
        this.route = route;
        this.vertx = vertx;
        this.simpleHttpServer = new SimpleHttpServer(endpointName, systemManager,
                flushInterval, "localhost", 0, serviceDiscovery,
                healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit,
                decorators, httpResponseCreator, requestBodyContinuePredicate);
        this.systemManager = systemManager;
        this.setWebSocketIdleConsume(aVoid -> {
        });
        this.setHttpRequestsIdleConsumer(aVoid -> {
        });

        this.httpServer = httpServer;
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

        simpleHttpServer.start();

        if (debug) {
            vertx.setPeriodic(10_000, event -> logger.info("Exception Count {} Close Count {}", exceptionCount, closeCount));
        }

        httpServer.websocketHandler(this::handleWebSocketMessage);

        if (route != null) {
            route.handler(event -> handleHttpRequest(event.request(), event.data()));
        } else if (router != null) {
            router.route().handler(event -> handleHttpRequest(event.request(), event.data()));
        } else {
            httpServer.requestHandler(this::handleHttpRequest);
        }

    }

    @Override
    public void stop() {
        simpleHttpServer.stop();
        if (systemManager != null) systemManager.serviceShutDown();
    }

    private void handleHttpRequest(final HttpServerRequest request) {
        handleHttpRequest(request, new HashMap<>());
    }

    private void handleHttpRequest(final HttpServerRequest request, final Map<String, Object> data) {

        if (debug) {
            setupMetrics(request);
            logger.debug("HttpServerVertx::handleHttpRequest::{}:{}", request.method(), request.uri());
        }

        switch (request.method().toString()) {

            case "PUT":
            case "POST":

                final String contentType = request.headers().get("Content-Type");
                if (HttpContentTypes.isFormContentType(contentType)) {
                    request.setExpectMultipart(true);
                }

                final Buffer[] bufferHolder = new Buffer[1];
                final HttpRequest bodyHttpRequest = vertxUtils.createRequest(request, () -> bufferHolder[0], data,
                        simpleHttpServer.getDecorators(), simpleHttpServer.getHttpResponseCreator());
                if (simpleHttpServer.getShouldContinueReadingRequestBody().test(bodyHttpRequest)) {
                    request.bodyHandler((buffer) -> {
                        bufferHolder[0] = buffer;
                        simpleHttpServer.handleRequest(bodyHttpRequest);
                    });
                } else {
                    logger.info("Request body rejected {} {}", request.method(), request.absoluteURI());
                }

                break;

            case "HEAD":
            case "OPTIONS":
            case "DELETE":
            case "GET":
                final HttpRequest getRequest;
                getRequest = vertxUtils.createRequest(request, null, data,
                        simpleHttpServer.getDecorators(), simpleHttpServer.getHttpResponseCreator());
                simpleHttpServer.handleRequest(getRequest);
                break;

            default:
                throw new IllegalStateException("method not supported yet " + request.method());

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

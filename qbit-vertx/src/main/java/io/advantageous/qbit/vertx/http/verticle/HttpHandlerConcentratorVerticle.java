package io.advantageous.qbit.vertx.http.verticle;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServerHttpHandler;
import io.advantageous.qbit.http.WebSocketMessage;
import org.boon.core.reflection.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rhightower on 1/27/15.
 */
public class HttpHandlerConcentratorVerticle extends BaseHttpRelay {

    private final Logger logger = LoggerFactory.getLogger(HttpHandlerConcentratorVerticle.class);
    private final boolean debug = logger.isDebugEnabled();


    public static final String HTTP_HANDLER_VERTICLE_HANDLER = "HTTP_HANDLER_VERTICLE_HANDLER";
    private String handlerClassName;
    private HttpServerHttpHandler httpServerHandler;


    @Override
    protected void extractConfig() {
        super.extractConfig();
        if (container.config().containsField(HTTP_HANDLER_VERTICLE_HANDLER)) {
            handlerClassName = container.config().getString(HTTP_HANDLER_VERTICLE_HANDLER);
        }
    }

    @Override
    protected void idleWebSocket() {
        httpServerHandler.webSocketQueueIdle().accept(null);
    }

    @Override
    protected void idleRequests() {

        httpServerHandler.requestQueueIdle().accept(null);
    }

    @Override
    protected void handleWebSocketClosed(WebSocketMessage webSocketMessage) {


        if (debug) logger.debug("HTTP HANDLER VERTICLE GOT CLOSED WEB_SOCKET " + webSocketMessage);
        httpServerHandler.webSocketClosed().accept(webSocketMessage);
    }

    @Override
    protected void handleWebSocketMessage(WebSocketMessage message) {


        if (debug) logger.debug("HTTP HANDLER VERTICLE GOT WEB_SOCKET " + message);
        httpServerHandler.webSocketConsumer().accept(message);

    }

    @Override
    protected void handleHttpRequest(HttpRequest httpRequest) {

        if (debug) logger.debug("HTTP HANDLER VERTICLE GOT REQUEST " + httpRequest);
        httpServerHandler.httpRequestConsumer().accept(httpRequest);
    }

    @Override
    protected void afterStart() {

        final Object o = Reflection.newInstance(handlerClassName);
        if (o instanceof HttpServerHttpHandler) {
            this.httpServerHandler = (HttpServerHttpHandler) o;
        }

    }
}

package io.advantageous.qbit.vertx.http.verticle;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServerHttpHandler;
import io.advantageous.qbit.http.WebSocketMessage;
import org.boon.core.reflection.Reflection;

/**
 * Created by rhightower on 1/27/15.
 */
public class HttpHandlerVerticle extends BaseHttpRelay {


    private static final String HTTP_HANDLER_VERTICLE_HANDLER = "HTTP_HANDLER_VERTICLE_HANDLER";
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
    protected void handleWebSocketMessage(WebSocketMessage message) {
        httpServerHandler.webSocketConsumer().accept(message);

    }

    @Override
    protected void handleHttpRequest(HttpRequest httpRequest) {
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

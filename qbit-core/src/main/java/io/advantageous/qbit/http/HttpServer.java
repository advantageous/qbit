package io.advantageous.qbit.http;

import io.advantageous.qbit.server.Server;

import java.util.function.Consumer;

/**
 * Represents an HTTP server.
 * Has the ability to register callbacks.
 *
 * Created by rhightower on 10/22/14.
 * @author rhightower
 */
public interface HttpServer extends Server {

    void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer);

    void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer);

    void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer);

    void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer);

    void setWebSocketIdleConsume(Consumer<Void> idleConsumer);

}

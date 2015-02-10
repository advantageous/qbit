package io.advantageous.qbit.http;

import io.advantageous.qbit.server.Server;

import java.util.function.Consumer;
import java.util.function.Predicate;

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

    default void setShouldContinueHttpRequest(Predicate<HttpRequest> predicate) {
        throw new RuntimeException("Not supported");
    }

    void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer);

    void setWebSocketIdleConsume(Consumer<Void> idleConsumer);


}

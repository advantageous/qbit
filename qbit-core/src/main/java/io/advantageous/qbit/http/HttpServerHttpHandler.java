package io.advantageous.qbit.http;

import io.advantageous.qbit.service.Callback;

/**
 * Created by rhightower on 1/27/15.
 */
public interface HttpServerHttpHandler {

    Callback<HttpRequest> httpRequestConsumer();
    Callback<WebSocketMessage> webSocketConsumer();
    Callback<WebSocketMessage> webSocketClosed();
    Callback<Void> webSocketQueueIdle();
    Callback<Void> requestQueueIdle();

}

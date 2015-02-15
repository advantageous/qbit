package io.advantageous.qbit.http.websocket;

import io.advantageous.qbit.network.NetworkSender;

/**
 * Created by rhightower on 10/22/14.
 * @author rhightower
 */
public interface WebSocketSender extends NetworkSender {


    default void openWebSocket(WebSocket webSocket) {
        open(webSocket);
    }
}

package io.advantageous.qbit.http;

import io.advantageous.qbit.network.NetSocket;
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

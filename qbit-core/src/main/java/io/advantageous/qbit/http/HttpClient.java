package io.advantageous.qbit.http;

import java.util.function.Consumer;

/**
 * This is an interface that allows users to send HTTP requests to a server.
 *
 * Created by rhightower on 10/28/14.
 *
 * @author rhightower
 */
public interface HttpClient {
    void sendHttpRequest(HttpRequest request);


    void sendWebSocketMessage(WebSocketMessage webSocketMessage);

    void periodicFlushCallback(Consumer<Void> periodicFlushCallback);


    void start();

    void flush();

    void stop();

}

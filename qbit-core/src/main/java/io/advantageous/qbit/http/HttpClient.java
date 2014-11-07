package io.advantageous.qbit.http;

/**
 * Created by rhightower on 10/28/14.
 * @author rhightower
 */
public interface HttpClient {
    void sendHttpRequest(HttpRequest request);


    void sendWebSocketMessage(WebSocketMessage webSocketMessage);

    void run();

    void flush();

    void stop();

}

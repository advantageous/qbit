package io.advantageous.qbit.http;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by rhightower on 10/22/14.
 * @author rhightower
 */
public interface HttpServer {

    void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer);

    void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer);


    void setTimeCallback(Consumer<Long> time);

    void run();


    void stop();
}

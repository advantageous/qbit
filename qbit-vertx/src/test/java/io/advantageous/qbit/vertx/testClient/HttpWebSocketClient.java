package io.advantageous.qbit.vertx.testClient;

import io.advantageous.qbit.http.client.HttpClient;
import org.boon.core.Sys;

import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.websocket.WebSocketMessageBuilder.webSocketMessageBuilder;
import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/13/15.
 */
public class HttpWebSocketClient {

    public static void main(String... args) {
        final HttpClient httpClient = httpClientBuilder().setAutoFlush(true).setPort(9999).build();

        httpClient.start();

        httpClient.sendWebSocketMessage(
                webSocketMessageBuilder().setMessage("Hello").setUri("/hello")
                        .setSender(message -> puts(message))
                        .build()
        );

        Sys.sleep(100000);
    }
}

package io.advantageous.qbit.http.jetty;


import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.websocket.WebSocket;
import org.boon.core.Sys;

import java.util.function.Consumer;

import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.server.websocket.WebSocketMessageBuilder.webSocketMessageBuilder;
import static org.boon.Boon.puts;


/**
 * Created by rhightower on 2/13/15.
 */
public class HttpWebSocketClient {

    public static void main(String... args) {

        final HttpClient httpClient = httpClientBuilder()
                .setPort(9999).build().start();


        final WebSocket webSocket = httpClient.createWebSocket("/hello");

        webSocket.setTextMessageConsumer(message ->
            puts("\n\n\n", message, "\n\n")
        );

        webSocket.openAndWait();

        webSocket.sendText("Hello");

        Sys.sleep(100000);
    }
}

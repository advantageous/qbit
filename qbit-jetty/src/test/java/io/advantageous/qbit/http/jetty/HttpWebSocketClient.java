package io.advantageous.qbit.http.jetty;


import io.advantageous.qbit.http.HttpClient;
import org.boon.core.Sys;

import static io.advantageous.qbit.http.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.WebSocketMessageBuilder.webSocketMessageBuilder;
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
                        .setSender(message -> puts("\n\n\n", message, "\n\n"))
                        .build()
        );

        Sys.sleep(100000);
    }
}

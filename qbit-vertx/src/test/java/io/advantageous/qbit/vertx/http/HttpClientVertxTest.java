package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.*;
import org.boon.core.Sys;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class HttpClientVertxTest {

    volatile boolean requestReceived;
    volatile boolean responseReceived;
    HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
    WebSocketMessageBuilder webSocketMessageBuilder = new WebSocketMessageBuilder();
    HttpClient client;
    HttpServer server;

    public void connect(int port) {

        client = new HttpClientBuilder().setPort(port).build();
        client.start();

        server = new HttpServerBuilder().setPort(port).build();

        requestReceived = false;
        responseReceived = false;

    }

    @Test
    public void testWebSocket() {

        connect(9090);


        server.setWebSocketMessageConsumer(webSocketMessage -> {
            if (webSocketMessage.getMessage().equals("What do you want on your cheeseburger?")) {
                webSocketMessage.getSender().send("Bacon");
                requestReceived = true;

            } else {
                puts("Websocket message", webSocketMessage.getMessage());
            }
        });


        final WebSocketMessage webSocketMessage = webSocketMessageBuilder.setUri("/services/cheeseburger")
                .setMessage("What do you want on your cheeseburger?").setSender(
                message -> {
                    if (message.equals("Bacon")) {
                        responseReceived = true;
                    }
                }
        ).build();

        run();

        client.sendWebSocketMessage(webSocketMessage);
        client.flush();

        Sys.sleep(100);



        validate();
        stop();

    }


    @Test
    public void testHttpServerClient() throws Exception {


        connect(9191);


        server.setHttpRequestConsumer(request -> {
            requestReceived = true;
            puts("SERVER", request.getUri(), request.getBody());
            request.getResponse().response(200, "application/json", "\"ok\"");
        });

        run();

        requestBuilder.setRemoteAddress("localhost").setMethod("GET").setUri("/client/foo");

        requestBuilder.setTextResponse((code, mimeType, body) -> {
            responseReceived = true;

            puts("CLIENT", code, mimeType, body);

        });

        client.sendHttpRequest(requestBuilder.build());
        client.flush();

        validate();
        stop();
    }


    public void run() {

        server.start();
        client.start();
        Sys.sleep(500);
    }



    private void stop() {


        client.stop();
        server.stop();

        Sys.sleep(500);
    }

    public void validate() {

        Sys.sleep(500);



        if (!requestReceived) {
            die("Request not received");
        }


        if (!responseReceived) {
            die("Response not received");
        }

    }
}
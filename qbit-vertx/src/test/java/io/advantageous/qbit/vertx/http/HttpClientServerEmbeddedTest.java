package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.*;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.function.Consumer;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 1/26/15.
 */
public class HttpClientServerEmbeddedTest {

    static volatile boolean requestReceived;
    volatile boolean responseReceived;
    HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
    WebSocketMessageBuilder webSocketMessageBuilder = new WebSocketMessageBuilder();
    HttpClient client;
    HttpServer server;

    public static class HanderClass implements Consumer<HttpServer> {



        @Override
        public void accept(HttpServer server) {

            server.setHttpRequestConsumer(request -> {
                requestReceived = true;
                puts("SERVER", request.getUri(), request.getBody());
                request.getResponse().response(200, "application/json", "\"ok\"");
            });

        }
    }

    public void connect(int port) {

        client = new HttpClientBuilder().setPort(port).build();
        client.start();

        server = new HttpServerBuilder().setPort(port).setHandlerClass(HanderClass.class).setWorkers(20).build();

        requestReceived = false;
        responseReceived = false;

    }


    @Test
    public void test() {}

    @Test
    public void testHttpServerClient() throws Exception {


        connect(10100);



        run();

        requestBuilder.setRemoteAddress("localhost").setMethod("GET").setUri("/client/foo");

        requestBuilder.setTextResponse((code, mimeType, body) -> {
            responseReceived = true;

            puts("CLIENT", code, mimeType, body);

        });

        client.sendHttpRequest(requestBuilder.build());
        client.flush();

        Sys.sleep(1000);


        validate();
        stop();
    }


    public void run() {

        server.start();
        client.start();
        Sys.sleep(5000);
    }



    private void stop() {


        client.stop();
        server.stop();

        Sys.sleep(500);
    }

    public void validate() {

        Sys.sleep(1000);




        if (!responseReceived) {
            die("Response not received");
        }

    }
}
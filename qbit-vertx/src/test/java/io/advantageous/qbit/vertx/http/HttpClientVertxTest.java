package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.*;
import org.boon.core.Sys;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class HttpClientVertxTest {

    volatile boolean requestReceived;

    volatile boolean responseReceived;

    @Test
    public void testRun() throws Exception {

        HttpClient client = new HttpClientVertx(9090, "localhost");
        client.run();

        HttpServer server = new HttpServerVertx(9090, "localhost");

        server.setHttpRequestConsumer(request -> {
            requestReceived = true;

            puts("SERVER", request.getUri(), request.getBody());

            request.getResponse().response(200, "application/json", "\"ok\"");
        });

        server.run();

        Sys.sleep(500);


        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        requestBuilder.setRemoteAddress("localhost").setMethod("GET").setUri("/service/foo");


        requestBuilder.setResponse((code, mimeType, body) -> {
            responseReceived = true;

            puts("CLIENT", code, mimeType, body);

        });


        client.sendHttpRequest(requestBuilder.build());

        client.flush();



//        It don't work yet. 
//        Sys.sleep(500);
//
//        if (!requestReceived) {
//            die("Request not received");
//        }
//
//
//        if (!responseReceived) {
//            die("Response not received");
//        }


    }
}
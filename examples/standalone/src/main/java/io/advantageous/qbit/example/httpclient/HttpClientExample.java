package io.advantageous.qbit.example.httpclient;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServer;

import java.util.concurrent.TimeUnit;

import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.server.HttpServerBuilder.httpServerBuilder;

public class HttpClientExample {


    public static void main(final String... args) {


        final HttpServer httpServer = httpServerBuilder().build();

        httpServer.setHttpRequestConsumer(request -> {
            if (request.getParams().getFirst("myparam").equals("myvalue")) {
                request.getReceiver().respondOK("\"hello\"");
            }
        });

        httpServer.startServerAndWait(); //starts and wait until server is listening


        final HttpClient client = httpClientBuilder().buildAndStart();

        final HttpTextResponse with1ParamWithTimeout = client.getWith1ParamWithTimeout("/", "myparam", "myvalue", 1, TimeUnit.SECONDS);

        System.out.println(with1ParamWithTimeout.body());

        final HttpTextResponse with1ParamWithTimeout2 = client.getWith1ParamWithTimeout("/", "myparam", "sdf", 1, TimeUnit.SECONDS);


        System.out.println(with1ParamWithTimeout2.body());
    }

}

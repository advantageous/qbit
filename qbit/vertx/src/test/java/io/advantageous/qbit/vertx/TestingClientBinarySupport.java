package io.advantageous.qbit.vertx;

import io.advantageous.boon.core.IO;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;

import java.io.File;

import static io.advantageous.boon.core.IO.puts;

/**
 * created by rick on 6/3/15.
 */
public class TestingClientBinarySupport {

    public static void main(String... args) {

        HttpClient client = HttpClientBuilder.httpClientBuilder().setPort(80).setHost("farm4.staticflickr.com").build();

        client.start();


        final File thisDir = new File(".");
        final File thisImage = new File(thisDir, "foo.jpg");

        HttpTextResponse httpResponse = client.get("/3721/9207329484_ba28755ec4_o.jpg");
        puts(httpResponse.contentType());

        HttpRequest httpRequest = HttpRequestBuilder.httpRequestBuilder()
                .setUri("/3721/9207329484_ba28755ec4_o.jpg")
                .setBinaryReceiver((code, contentType, body) -> {

                    puts(body.length, code, contentType);


                    IO.write(thisImage.toPath(), body);
                })
                .build();

        client.sendHttpRequest(httpRequest);

        HttpServer server = HttpServerBuilder.httpServerBuilder().setPort(9999).build();

        server.setHttpRequestConsumer(serverRequest -> {

            serverRequest.getReceiver().response(200, "image/jpeg", IO.input(thisImage.getAbsolutePath()));
        });

        server.startServer();
    }
}

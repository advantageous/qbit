package io.advantageous.qbit.vertx;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.util.GzipUtils;

import java.nio.charset.StandardCharsets;

import static io.advantageous.boon.core.IO.puts;

/**
 * created by rick on 6/3/15.
 */
public class TestingClientGzipSupport {
    public static void main(String... args) throws Exception {


        System.out.println(GzipUtils.decode(GzipUtils.encode("\"HELLO\"")));


        HttpServer server = HttpServerBuilder.httpServerBuilder().setPort(9999).build();

        server.setHttpRequestConsumer(serverRequest -> {

            final String contentEncoding = serverRequest.getHeaders().getFirst("Content-Encoding");

            if (serverRequest.getContentType().startsWith("application/json")
                    && contentEncoding.equalsIgnoreCase("gzip")) {
                try {
                    puts("S BODY FROM GZIP", GzipUtils.decode(serverRequest.getBody()));
                    puts("S BODY FROM GZIP", new String(serverRequest.getBody(), StandardCharsets.UTF_8));

                    serverRequest.getReceiver().response(200, "application/json",
                            "true");

                } catch (Exception e) {
                    e.printStackTrace();
                    serverRequest.getReceiver().response(500, "application/json",
                            "false");
                }
            } else {
                puts("SERVER ", serverRequest.getBodyAsString());
                serverRequest.getReceiver().response(200, "application/json",
                        "1");
            }
        });

        server.startServerAndWait();

        HttpClient client = HttpClientBuilder.httpClientBuilder()
                .setPort(9999)
                .setHost("localhost").build();

        client.start();

        HttpRequest httpRequest = HttpRequestBuilder.httpRequestBuilder()
                .setBinaryReceiver((code, contentType, body) -> {


                    puts("CLIENT", body.length, code, contentType, new String(body, StandardCharsets.UTF_8));
                })
                .setMethodPost()
                .setJsonBodyForPostGzip("\"Hello\"")
                .build();

        puts("HELLO HERE", GzipUtils.decode(httpRequest.getBody()));
        client.sendHttpRequest(httpRequest);

    }
}

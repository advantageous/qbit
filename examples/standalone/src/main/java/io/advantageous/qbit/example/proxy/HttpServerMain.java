package io.advantageous.qbit.example.proxy;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;


public class HttpServerMain {

    public static void main(String... args) throws Exception {
        final HttpServerBuilder httpServerBuilder = HttpServerBuilder.httpServerBuilder().setPort(8080);

        final HttpServer httpServer = httpServerBuilder.build();

        httpServer.setHttpRequestConsumer(request -> {
            request.getReceiver().response(200, request.getContentType(), request.body());
        });

        httpServer.startServer();
    }
}

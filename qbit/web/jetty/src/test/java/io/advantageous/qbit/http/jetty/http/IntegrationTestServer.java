package io.advantageous.qbit.http.jetty.http;

import io.advantageous.boon.core.Maps;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;

import java.util.Map;

import static io.advantageous.boon.core.IO.puts;

public class IntegrationTestServer {

    public static void main(String... args) {
        HttpServer httpServer = HttpServerBuilder.httpServerBuilder().setPort(9999).build();
        httpServer.setHttpRequestConsumer(request -> {


            final Map<String, String> map = Maps.map("method", request.getMethod(),
                    "body", request.getBodyAsString(),
                    "uri", request.getUri(),
                    "returnAddress", request.getRemoteAddress()
            );

            for (String key : request.params().keySet()) {
                map.put("requestParam." + key, request.params().getFirst(key));
            }


            for (String key : request.headers().keySet()) {
                map.put("header." + key, request.headers().getFirst(key));
            }


            request.getReceiver().response(200, "application/json",
                    JsonFactory.toJson(map));
        });
        httpServer.startServer();

        puts("Started");
    }

}

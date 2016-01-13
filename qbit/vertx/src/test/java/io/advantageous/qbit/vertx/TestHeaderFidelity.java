package io.advantageous.qbit.vertx;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertTrue;

public class TestHeaderFidelity {

    @Test
    public void test() throws Exception {


        final int openPortStartAt = PortUtils.findOpenPortStartAt(7777);
        HttpServer server = HttpServerBuilder.httpServerBuilder().setPort(openPortStartAt).build();

        server.setHttpRequestConsumer(serverRequest -> {

            final MultiMap<String, String> headers = MultiMap.multiMap();

            headers.add("foo", "bar").add("foo", "baz");
            serverRequest.getReceiver().response(200, "application/json", "true", headers);
        });

        server.startServerAndWait();

        HttpClient client = HttpClientBuilder.httpClientBuilder()
                .setPort(openPortStartAt)
                .setHost("localhost").build();

        client.start();

        final HttpTextResponse httpResponse = client.get("/hi");

        puts(httpResponse.headers());


        boolean foundFoo = httpResponse.headers().keySet().contains("foo");
        assertTrue(foundFoo);
    }

}

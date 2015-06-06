package io.advantageous.qbit.vertx;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.util.MultiMap;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertTrue;

public class TestHeaderFidelity {

    @Test
    public void test() throws Exception {


        HttpServer server = HttpServerBuilder.httpServerBuilder().setPort(9999).build();

        server.setHttpRequestConsumer(serverRequest -> {

            final MultiMap<String, String> headers = MultiMap.multiMap();

            headers.add("foo", "bar").add("foo", "baz");
            serverRequest.getReceiver().response(200, "application/json", "true", headers);
        });

        server.startServer();

        HttpClient client = HttpClientBuilder.httpClientBuilder()
                .setPort(9999)
                .setHost("localhost").build();

        client.start();

        final HttpResponse httpResponse = client.get("/hi");

        Sys.sleep(1000);
        puts(httpResponse.headers());


        boolean foundFoo = httpResponse.headers().keySet().contains("foo");
        assertTrue(foundFoo);
    }

}

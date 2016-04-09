package io.advantageous.qbit.vertx.bugs;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Bug660 {

    @Test
    public void test() throws Exception {

        AtomicInteger codeRef = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();


        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder()
                .setHost("localhost")
                .setPort(9999).setErrorHandler((err) ->
                {
                    error.compareAndSet(null, err);
                })
                .buildAndStart();


        httpClient.sendHttpRequest(HttpRequestBuilder
                .httpRequestBuilder()
                .setJsonBodyForPost("\"hi mob\"")
                .setResponse((code, contentType, body) -> {
                    puts(code, contentType, body);
                    codeRef.set(code);
                })
                .build());

        for (int index = 0; index < 100; index++) {
            if (codeRef.get() != 0 && error.get() != null) {
                break;
            }
            Sys.sleep(1);
        }

        assertEquals(503, codeRef.get());


        assertNotNull(error.get());

    }

}

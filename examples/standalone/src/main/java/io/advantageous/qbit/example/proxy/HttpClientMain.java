package io.advantageous.qbit.example.proxy;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;

import static io.advantageous.boon.core.IO.puts;

public class HttpClientMain {


    public static void main(String... args) throws Exception{

        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.httpClientBuilder().setPort(9090);
        final HttpClient httpClient = httpClientBuilder.buildAndStart();

        for (int index =0; index < 100; index++) {
            final HttpTextResponse httpTextResponse = httpClient.postJson("/foo", "\"bar\"");
            puts(httpTextResponse);
        }
    }
}

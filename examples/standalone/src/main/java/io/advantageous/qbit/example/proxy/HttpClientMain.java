package io.advantageous.qbit.example.proxy;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequestBuilder;

import static io.advantageous.boon.core.IO.puts;

public class HttpClientMain {


    public static void main(String... args) throws Exception{

        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.httpClientBuilder().setPort(9090);
        final HttpClient httpClient = httpClientBuilder.setKeepAlive(false).setPipeline(false).setPoolSize(100)
                .buildAndStart();

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setJsonBodyForPost("\"FOO\"").setUri("/foo");
        httpRequestBuilder.setTextReceiver((code, contentType, body) -> puts(code, contentType, body));

        for (int index =0; index < 100; index++) {

            httpClient.sendHttpRequest(httpRequestBuilder.build());
        }
    }
}

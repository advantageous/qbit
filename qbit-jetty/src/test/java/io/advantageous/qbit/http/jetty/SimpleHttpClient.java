package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.http.HttpClient;

import static io.advantageous.qbit.http.HttpClientBuilder.httpClientBuilder;
import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/14/15.
 */
public class SimpleHttpClient {

    public static void main(String... args) throws Exception {

        final HttpClient httpClient = httpClientBuilder().setAutoFlush(true).setPort(9999).build();

        httpClient.start();

        final String body = httpClient.get("/hello").body();

        puts("\n\n\n\nBODY", body, "\n\n\n\nBODY");

    }
}

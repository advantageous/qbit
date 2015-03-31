package io.advantageous.qbit.http.jetty.service;

import io.advantageous.qbit.http.client.HttpClient;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;

/**
 * Example hitting a service written in QBit REST with the QBit Http Client
 * Created by rhightower on 3/17/15.
 */
public class ServiceServerHttpClient {

    public static void main(final String... args) throws Exception {
        HttpClient httpClient = httpClientBuilder().setPort(9998).build();
        httpClient.start();

        puts(httpClient.get("/services/ping/ping").body());


        puts(httpClient.get("/services/ping/oneway").body());


        puts(httpClient.post("/services/ping/onewaypost").body());


        puts(httpClient.postWith1Param("/services/ping/onewaypostarg1", "arg", " mom").body());
    }
}

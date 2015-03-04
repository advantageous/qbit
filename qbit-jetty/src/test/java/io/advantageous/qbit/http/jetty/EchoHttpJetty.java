/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.request.HttpTextReceiver;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.boon.core.Sys;

import java.util.HashMap;
import java.util.Map;

import static io.advantageous.boon.Boon.fromJson;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.toJson;
import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.request.HttpRequestBuilder.httpRequestBuilder;
import static io.advantageous.qbit.http.server.HttpServerBuilder.httpServerBuilder;



/**
 * Created by rhightower on 2/16/15.
 */
public class EchoHttpJetty {

    public static void main(String... args) {


        /* Create an HTTP server. */
        HttpServer httpServer = httpServerBuilder()
                .setPort(8080).build();

        /* Setting up a request Consumer with Java 8 Lambda expression. */
        httpServer.setHttpRequestConsumer(httpRequest -> {

            Map<String, Object> results = new HashMap<>();
            results.put("method", httpRequest.getMethod());
            results.put("uri", httpRequest.getUri());
            results.put("body", httpRequest.getBodyAsString());
            results.put("headers", httpRequest.getHeaders());
            results.put("params", httpRequest.getParams());
            httpRequest.getReceiver().response(200, "application/json", toJson(results));
        });

        /* Start the server. */
        httpServer.start();


        /* Setup an httpClient. */
        HttpClient httpClient = httpClientBuilder().setHost("localhost").setPort(8080).build();
        httpClient.start();

        //sendGets(httpClient);
        //sendPosts(httpClient);
        //sendPuts(httpClient);

        //POST JSON

        httpClient.sendJsonPost("/foo/json/", toJson(new Employee("Rick", "Smith")));


        httpClient.sendJsonPostAsync("/foo/json/", toJson(new Employee("Rick", "Smith")),
                (code, contentType, body) -> puts("ASYNC POST", code, contentType, fromJson(body)));


        httpClient.sendJsonPostAsync("/foo/json/", toJson(new Employee("Rick", "Smith")),
                new HttpTextReceiver() {
                    @Override
                    public void response(int code, String contentType, String body) {
                        puts(code, contentType, body);
                    }
                });

        HttpResponse httpResponse = httpClient.postJson("/foo/json/sync",
                toJson(new Employee("Rick", "Smith")));

        puts("POST JSON RESPONSE", httpResponse);

        //PUT JSON

        httpClient.sendJsonPut("/foo/json/", toJson(new Employee("Rick", "Smith")));


        httpClient.sendJsonPutAsync("/foo/json/", toJson(new Employee("Rick", "Smith")),
                (code, contentType, body) -> puts("ASYNC PUT", code, contentType, fromJson(body)));


        httpResponse = httpClient.putJson("/foo/json/sync", toJson(new Employee("Rick", "Smith")));

        puts("PUT JSON RESPONSE", httpResponse);


        Sys.sleep(1000);

        httpServer.stop();
        httpClient.stop();
    }

    private static void sendGets(HttpClient httpClient) {
        /* Send no param get. */
        HttpResponse httpResponse = httpClient.get("/hello/mom");
        puts(httpResponse);


        /* Send one param get. */
        httpResponse = httpClient.getWith1Param("/hello/singleParam", "hi", "mom");
        puts("single param", httpResponse);


        /* Send two param get. */
        httpResponse = httpClient.getWith2Params("/hello/twoParams",
                "hi", "mom", "hello", "dad");
        puts("two params", httpResponse);


        /* Send two param get. */
        httpResponse = httpClient.getWith3Params("/hello/3params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids");
        puts("three params", httpResponse);


        /* Send four param get. */
        httpResponse = httpClient.getWith4Params("/hello/4params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets");
        puts("4 params", httpResponse);

        /* Send five param get. */
        httpResponse = httpClient.getWith5Params("/hello/5params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets",
                "hola", "neighbors");
        puts("5 params", httpResponse);


        /* Send six params with get. */

        final HttpRequest httpRequest = httpRequestBuilder().addParam("hi", "mom")
                .addParam("hello", "dad")
                .addParam("greetings", "kids")
                .addParam("yo", "pets")
                .addParam("hola", "pets")
                .addParam("salutations", "all").build();

        httpResponse = httpClient.sendRequestAndWait(httpRequest);
        puts("6 params", httpResponse);


        /* Using Async support with lambda. */
        httpClient.getAsync("/hi/async", (code, contentType, body) -> {
            puts("Async text with lambda", body);
        });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.getAsyncWith1Param("/hi/async", "hi", "mom", (code, contentType, body) -> {
            puts("Async text with lambda 1 param\n", body);
        });

        Sys.sleep(100);



        /* Using Async support with lambda. */
        httpClient.getAsyncWith2Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                (code, contentType, body) -> {
                    puts("Async text with lambda 2 params\n", body);
                });

        Sys.sleep(100);




        /* Using Async support with lambda. */
        httpClient.getAsyncWith3Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                (code, contentType, body) -> {
                    puts("Async text with lambda 3 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.getAsyncWith4Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                (code, contentType, body) -> {
                    puts("Async text with lambda 4 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.getAsyncWith5Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                "p5", "v5",
                (code, contentType, body) -> {
                    puts("Async text with lambda 5 params\n", body);
                });

        Sys.sleep(100);
    }

    private static void sendPosts(HttpClient httpClient) {

        /* Send no param post. */
        HttpResponse httpResponse = httpClient.post("/hello/mom");
        puts(httpResponse);


        /* Send one param post. */
        httpResponse = httpClient.postWith1Param("/hello/singleParam", "hi", "mom");
        puts("single param", httpResponse);


        /* Send two param post. */
        httpResponse = httpClient.postWith2Params("/hello/twoParams",
                "hi", "mom", "hello", "dad");
        puts("two params", httpResponse);


        /* Send two param post. */
        httpResponse = httpClient.postWith3Params("/hello/3params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids");
        puts("three params", httpResponse);


        /* Send four param post. */
        httpResponse = httpClient.postWith4Params("/hello/4params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets");
        puts("4 params", httpResponse);

        /* Send five param post. */
        httpResponse = httpClient.postWith5Params("/hello/5params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets",
                "hola", "neighbors");
        puts("5 params", httpResponse);


        /* Send six params with post. */

        HttpRequest httpRequest = httpRequestBuilder()
                .setUri("/sixPost")
                .setMethod("POST")
                .addParam("hi", "mom")
                .addParam("hello", "dad")
                .addParam("greetings", "kids")
                .addParam("yo", "pets")
                .addParam("hola", "pets")
                .addParam("salutations", "all").build();


        httpResponse = httpClient.sendRequestAndWait(httpRequest);
        puts("6 params", httpResponse);


        //////////////////
        //////////////////


        /* Using Async support with lambda. */
        httpClient.postAsync("/hi/async", (code, contentType, body) -> {
            puts("Async text with lambda", body);
        });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.postFormAsyncWith1Param("/hi/async", "hi", "mom", (code, contentType, body) -> {
            puts("Async text with lambda 1 param\n", body);
        });

        Sys.sleep(100);



        /* Using Async support with lambda. */
        httpClient.postFormAsyncWith2Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                (code, contentType, body) -> {
                    puts("Async text with lambda 2 params\n", body);
                });

        Sys.sleep(100);




        /* Using Async support with lambda. */
        httpClient.postFormAsyncWith3Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                (code, contentType, body) -> {
                    puts("Async text with lambda 3 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.postFormAsyncWith4Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                (code, contentType, body) -> {
                    puts("Async text with lambda 4 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.postFormAsyncWith5Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                "p5", "v5",
                (code, contentType, body) -> {
                    puts("Async text with lambda 5 params\n", body);
                });

        Sys.sleep(100);


        httpRequest = httpRequestBuilder().addParam("hi", "mom")
                .addParam("hello", "dad")
                .addParam("greetings", "kids")
                .addParam("yo", "pets")
                .addParam("hola", "pets")
                .addParam("salutations", "all")
                .setTextReceiver((code, contentType, body) -> puts(code, contentType, body))
                .build();

        httpClient.sendHttpRequest(httpRequest);
        puts("6 async params", httpResponse);


    }


    private static void sendPuts(HttpClient httpClient) {



        /* Send no param post. */
        HttpResponse httpResponse = httpClient.put("/hello/mom");
        puts(httpResponse);


        /* Send one param post. */
        httpResponse = httpClient.putWith1Param("/hello/singleParam", "hi", "mom");
        puts("single param", httpResponse);


        /* Send two param post. */
        httpResponse = httpClient.putWith2Params("/hello/twoParams",
                "hi", "mom", "hello", "dad");
        puts("two params", httpResponse);


        /* Send two param post. */
        httpResponse = httpClient.putWith3Params("/hello/3params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids");
        puts("three params", httpResponse);


        /* Send four param post. */
        httpResponse = httpClient.putWith4Params("/hello/4params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets");
        puts("4 params", httpResponse);

        /* Send five param post. */
        httpResponse = httpClient.putWith5Params("/hello/5params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets",
                "hola", "neighbors");
        puts("5 params", httpResponse);


        /* Send six params with post. */

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri("/sixPost")
                .setMethod("PUT")
                .addParam("hi", "mom")
                .addParam("hello", "dad")
                .addParam("greetings", "kids")
                .addParam("yo", "pets")
                .addParam("hola", "pets")
                .addParam("salutations", "all").build();


        httpResponse = httpClient.sendRequestAndWait(httpRequest);
        puts("6 params", httpResponse);


        //////////////////
        //////////////////


        /* Using Async support with lambda. */
        httpClient.putAsync("/hi/async", (code, contentType, body) -> {
            puts("Async text with lambda", body);
        });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.putFormAsyncWith1Param("/hi/async", "hi", "mom", (code, contentType, body) -> {
            puts("Async text with lambda 1 param\n", body);
        });

        Sys.sleep(100);



        /* Using Async support with lambda. */
        httpClient.putFormAsyncWith2Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                (code, contentType, body) -> {
                    puts("Async text with lambda 2 params\n", body);
                });

        Sys.sleep(100);




        /* Using Async support with lambda. */
        httpClient.putFormAsyncWith3Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                (code, contentType, body) -> {
                    puts("Async text with lambda 3 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.putFormAsyncWith4Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                (code, contentType, body) -> {
                    puts("Async text with lambda 4 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.putFormAsyncWith5Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                "p5", "v5",
                (code, contentType, body) -> {
                    puts("Async text with lambda 5 params\n", body);
                });

        Sys.sleep(100);


    }

    static class Employee {
        final String firstName;
        final String lastName;

        Employee(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}

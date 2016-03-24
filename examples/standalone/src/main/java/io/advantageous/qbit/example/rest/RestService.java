package io.advantageous.qbit.example.rest;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.HeaderParam;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.NoCacheHeaders;
import io.advantageous.qbit.annotation.http.POST;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.*;
import io.advantageous.qbit.http.request.decorator.HttpBinaryResponseHolder;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.request.decorator.HttpTextResponseHolder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.util.MultiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.http.server.CorsSupportBuilder.corsSupportBuilder;


public class RestService {


    @RequestMapping("/")
    @NoCacheHeaders
    public static class EchoService {

        @POST("/echo")
        public void echo(final Callback<HttpTextResponse> callback, @HeaderParam("USER")
                final String user,
                         String sentBody) {
            final HttpResponseBuilder responseBuilder = HttpResponseBuilder.httpResponseBuilder()
                    .addHeader("USER", user).setJsonBodyCodeOk(Str.doubleQuote(sentBody));
            callback.returnThis(responseBuilder.buildTextResponse());
        }
    }

    public static void main(final String... args) throws Exception {
        final ManagedServiceBuilder managedServiceBuilder =
                ManagedServiceBuilder.managedServiceBuilder().setRootURI("/")
                        .addEndpointService(new EchoService());

        managedServiceBuilder.getHttpServerBuilder().addShouldContinueHttpRequestPredicate(request -> {
            request.params().add("baz", "boo");
            return true;
        });

        managedServiceBuilder.getHttpServerBuilder().addResponseDecorator(corsSupportBuilder().buildResponseDecorator());


        managedServiceBuilder.getHttpServerBuilder().addResponseDecorator(new HttpResponseDecorator() {
            @Override
            public boolean decorateTextResponse(HttpTextResponseHolder responseHolder, String requestPath, String method,
                                                int code, String contentType, String payload,
                                                MultiMap<String, String> responseHeaders,
                                                MultiMap<String, String> requestHeaders,
                                                MultiMap<String, String> requestParams) {

                final HttpResponseBuilder responseBuilder = HttpResponseBuilder.httpResponseBuilder()
                        .setCode(code).setContentType(contentType).setBody(payload).setHeaders(responseHeaders);

                responseBuilder.addHeader("foo", "bar");

                responseHolder.setHttpTextResponse(responseBuilder.buildTextResponse());
                return true;
            }

            @Override
            public boolean decorateBinaryResponse(HttpBinaryResponseHolder responseHolder, String requestPath, String method,
                                                  int code, String contentType, byte[] payload,
                                                  MultiMap<String, String> responseHeaders,
                                                  MultiMap<String, String> requestHeaders,
                                                  MultiMap<String, String> requestParams) {
                return false;
            }
        });

        managedServiceBuilder.startApplication();



        for (int r = 0; r < 10; r++) {

            List<Thread> threadList = new ArrayList< >();
            for (int t = 0; t < 10; t++) {

                threadList.add(new Thread(() -> {

                    final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort(8080)
                            .setPoolSize(10).buildAndStart();

                    final String root = "root" + System.currentTimeMillis() + "-" + UUID.randomUUID();


                    final int requestCountPerThread = 10_000;
                    final CountDownLatch latch = new CountDownLatch(requestCountPerThread);

                    for (int index = 0; index < requestCountPerThread; index++) {

                        final String message = "\"a" + index + "-" + root + "\"";


                        final int callId = index;

                        HttpRequest httpRequest = HttpRequestBuilder.httpRequestBuilder()
                                .addHeader("USER", message)
                                .addHeader("Origin", "http://somesite.com")
                                .setJsonBodyForPost(message).setUri("/echo").setTextReceiver(new HttpTextReceiver() {


                                    @Override
                                    public void response(int code, String contentType, String body, MultiMap<String, String> headers) {
                                        final String user = headers.getFirst("USER");
                                        response(code, contentType, body);
                                        if (!message.equals(user)) {
                                            puts("HEADER NOT EQUAL TO REQUEST ***********************************\n\t", message, "\n\t", user);
                                        }
                                    }

                                    @Override
                                    public void response(int code, String contentType, String body) {

                                        latch.countDown();
                                        if (!message.equals(body)) {
                                            puts("BODY NOT EQUAL TO REQUEST ***********************************", message, body);
                                            puts("BODY NOT EQUAL TO REQUEST ***********************************", message, body);
                                            puts("BODY NOT EQUAL TO REQUEST ***********************************", message, body);
                                            puts("BODY NOT EQUAL TO REQUEST ***********************************", message, body);
                                            puts("BODY NOT EQUAL TO REQUEST ***********************************", message, body);
                                            puts("BODY NOT EQUAL TO REQUEST ***********************************", message, body);

                                        }


                                        if (callId % 100 == 0) {
                                            puts(callId);

                                        }
                                    }
                                }).build();

                        httpClient.sendHttpRequest(httpRequest);


                    }
                    try {
                        latch.await(100, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Sys.sleep(100);
                    httpClient.stop();
                }));

            }

            for (Thread t : threadList) {
                t.start();

            }


            for (Thread t : threadList) {
                t.join();
            }


        }

        Sys.sleep(5_000);

        System.exit(0);


    }


}

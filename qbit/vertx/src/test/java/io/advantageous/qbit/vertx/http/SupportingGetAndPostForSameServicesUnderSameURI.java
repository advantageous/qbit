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

package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.*;
import io.advantageous.qbit.http.request.decorator.HttpBinaryResponseHolder;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.request.decorator.HttpTextResponseHolder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.PortUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class SupportingGetAndPostForSameServicesUnderSameURI extends TimedTesting {

    Client client;
    ServiceEndpointServer server;
    HttpClient httpClient;
    ClientServiceInterface clientProxy;
    volatile int callCount;
    AtomicReference<String> pongValue;

    AtomicReference<MultiMap<String, String>> headers;
    boolean ok;
    int port = 8888;

    @Test
    public void testRestCallSimplePOST() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/pinger/ping")
                .setJsonBodyForPost("\"hello\"")
                .setTextReceiver(new HttpTextReceiver() {
                    @Override
                    public void response(int code, String mimeType, String body) {
                        if (code == 200) {
                            pongValue.set(body);
                        } else {
                            pongValue.set("ERROR " + body);
                            throw new RuntimeException("ERROR " + code + " " + body);

                        }
                    }
                })
                .build();

        httpClient.sendHttpRequest(request);

        httpClient.flush();

        waitForTrigger(20, o -> this.pongValue.get() != null);


        final String pongValue = this.pongValue.get();
        ok = pongValue.equals("\"hello pong\"") || die(pongValue);

    }

    @Test
    public void testRestCallSimpleGET() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/pinger/ping")
                .setMethod("GET")
                .setTextReceiver(new HttpTextReceiver() {
                    @Override
                    public void response(int code, String mimeType, String body) {
                        if (code == 200) {
                            pongValue.set(body);
                        } else {
                            pongValue.set("ERROR " + body);
                            throw new RuntimeException("ERROR " + code + " " + body);

                        }
                    }

                    @Override
                    public void response(int code, String mimeType, String body, MultiMap<String, String> theHeaders) {
                        if (code == 200) {
                            pongValue.set(body);
                        } else {
                            pongValue.set("ERROR " + body);
                            throw new RuntimeException("ERROR " + code + " " + body);

                        }
                        headers.set(theHeaders);
                    }
                })
                .build();

        httpClient.sendHttpRequest(request);

        httpClient.flush();


        waitForTrigger(20, o -> this.pongValue.get() != null);


        final String pongValue = this.pongValue.get();
        ok = pongValue.equals("\"pong\"") || die(pongValue);

        assertNotNull(headers.get());

        assertEquals("TEST", headers.get().get("TEST_HEADER"));

    }

    @Before
    public void setup() throws Exception {
        super.setupLatch();

        port = PortUtils.findOpenPortStartAt(8888);
        pongValue = new AtomicReference<>();

        headers = new AtomicReference<>();

        httpClient = new HttpClientBuilder().setPort(port).build();

        client = new ClientBuilder().setPort(port).build();

        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();

        endpointServerBuilder.setPort(port).getHttpServerBuilder();
        endpointServerBuilder.getHttpServerBuilder().addResponseDecorator(new HttpResponseDecorator() {
            @Override
            public boolean decorateTextResponse(HttpTextResponseHolder responseHolder, String requestPath, String method, int code,
                                                String contentType, String payload, MultiMap<String, String> responseHeaders,
                                                MultiMap<String, String> requestHeaders, MultiMap<String, String> requestParams) {

                responseHolder.setHttpTextResponse((HttpTextResponse) HttpResponseBuilder.httpResponseBuilder().setCode(code).setContentType(contentType)
                        .addHeader("TEST_HEADER", "TEST").setBody(payload).build());
                return true;
            }

            @Override
            public boolean decorateBinaryResponse(HttpBinaryResponseHolder responseHolder, String requestPath, String method, int code, String contentType, byte[] payload, MultiMap<String, String> responseHeaders, MultiMap<String, String> requestHeaders, MultiMap<String, String> requestParams) {
                return false;
            }
        });


        server = endpointServerBuilder.build();

        server.initServices(new MockService());

        server.startServerAndWait();


        clientProxy = client.createProxy(ClientServiceInterface.class, "pinger");
        client.start();
        httpClient.startClient();

        callCount = 0;
        pongValue.set(null);

        puts("STARTED..........");


    }

    @After
    public void teardown() throws Exception {

        port++;

        if (!ok) {
            die("NOT OK");
        }

        server.stop();
        client.stop();
        httpClient.stop();
        server = null;
        client = null;
        System.gc();

    }

    interface ClientServiceInterface {
        String ping(Callback<String> callback, String ping);
    }

    @RequestMapping("/pinger")
    class MockService {

        @RequestMapping(method = RequestMethod.POST, value = "/ping")
        public String ping(String ping) {
            callCount++;
            return ping + " pong";
        }


        @RequestMapping(method = RequestMethod.GET, value = "/ping")
        public String get() {
            callCount++;
            return "pong";
        }
    }
}

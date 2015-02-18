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
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpTextReceiver;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.test.TimedTesting;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 1/19/15.
 */
public class SupportingGetAndPostForSameServicesUnderSameURI extends TimedTesting {

    Client client;
    ServiceServer server;
    HttpClient httpClient;
    ClientServiceInterface clientProxy;
    volatile int callCount;
    AtomicReference<String> pongValue;
    boolean ok;
    int port = 8888;

    @Test
    public void testWebSocket() throws Exception {

        clientProxy.ping(new Callback<String>() {
            @Override
            public void accept(String s) {
                puts(s);
                pongValue.set(s);
            }
        }, "hi");

        ServiceProxyUtils.flushServiceProxy(clientProxy);

        waitForTrigger(20, o -> this.pongValue.get() != null);


        final String pongValue = this.pongValue.get();
        ok = pongValue.equals("hi pong") || die();

    }

    @Test
    public void testRestCallSimple() throws Exception {

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
                })
                .build();

        httpClient.sendHttpRequest(request);

        httpClient.flush();


        waitForTrigger(20, o -> this.pongValue.get() != null);


        final String pongValue = this.pongValue.get();
        ok = pongValue.equals("\"pong\"") || die(pongValue);

    }

    @Before
    public void setup() throws Exception {
        super.setupLatch();

        pongValue = new AtomicReference<>();

        httpClient = new HttpClientBuilder().setPort(port).build();

        client = new ClientBuilder().setPort(port).build();
        server = new ServiceServerBuilder().setPort(port).build();

        server.initServices(new MockService());

        server.start();

        Sys.sleep(200);

        clientProxy = client.createProxy(ClientServiceInterface.class, "pinger");
        client.start();
        httpClient.start();

        callCount = 0;
        pongValue.set(null);

        Sys.sleep(200);

        puts("STARTED..........");


    }

    @After
    public void teardown() throws Exception {

        port++;

        if (!ok) {
            die("NOT OK");
        }

        Sys.sleep(200);
        server.stop();
        Sys.sleep(200);
        client.stop();
        httpClient.stop();
        Sys.sleep(200);
        server = null;
        client = null;
        System.gc();
        Sys.sleep(1000);

    }

    static interface ClientServiceInterface {
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

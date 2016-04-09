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

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.qbit.util.PortUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertNotNull;


public class FullIntegrationTest extends TimedTesting {

    Client client;
    ServiceEndpointServer server;
    HttpClient httpClient;
    ClientServiceInterface clientProxy;
    AtomicInteger callCount = new AtomicInteger();

    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    Object context = Sys.contextToHold();


    @Test
    public void testWebSocket() throws Exception {

        final AtomicReference<String> pongValue = new AtomicReference<>();

        clientProxy.ping(s -> {
            puts(s);
            pongValue.set(s);
        }, "hi");

        ServiceProxyUtils.flushServiceProxy(clientProxy);

        waitForTrigger(2, o -> pongValue.get() != null);


        final String value = pongValue.get();

        assertNotNull(value);

        ok = value.equals("hi pong") || die();

    }

    public void testWebSocketSend10() throws Exception {


        final AtomicInteger returnCount = new AtomicInteger();

        final Callback<String> callback = s -> {
            returnCount.incrementAndGet();

            puts("                     PONG");
        };

        for (int index = 0; index < 20; index++) {

            clientProxy.ping(callback, "hi");

        }

        Sys.sleep(100);
        ServiceProxyUtils.flushServiceProxy(clientProxy);


        waitForTrigger(20, o -> returnCount.get() == callCount.get());

        Sys.sleep(100);

        puts("HERE                        ", callCount, returnCount);

        ok = returnCount.get() >= callCount.get() - 1 || die(returnCount, callCount);


    }

    @Test
    public void testRestCallSimple() throws Exception {


        final AtomicReference<String> pongValue = new AtomicReference<>();

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mockservice/ping")
                .setJsonBodyForPost("\"hello\"")
                .setTextReceiver((code, mimeType, body) -> {

                    System.out.println(body);

                    if (code == 200) {
                        pongValue.set(body);
                    } else {
                        pongValue.set("ERROR " + body);
                        throw new RuntimeException("ERROR " + code + " " + body);

                    }
                })
                .build();

        httpClient.sendHttpRequest(request);

        httpClient.flush();

        waitForTrigger(20, o -> pongValue.get() != null);


        final String value = pongValue.get();
        ok = value.equals("\"hello pong\"") || die(pongValue);

    }


    @Before
    public synchronized void setup() throws Exception {

        super.setupLatch();

        int port = PortUtils.findOpenPortStartAt(7000);

        httpClient = new HttpClientBuilder().setPort(port).setPoolSize(1).build();

        puts("PORT", port);

        client = new ClientBuilder().setPort(port).build();
        server = new EndpointServerBuilder().setPort(port).setFlushInterval(10).build();

        server.initServices(new MockService());

        server.startServerAndWait();

        clientProxy = client.createProxy(ClientServiceInterface.class, "mockService");
        httpClient.startClient();
        client.start();

        callCount.set(0);


    }

    @After
    public void teardown() throws Exception {


        server.stop();
        client.stop();
        httpClient.stop();
        server = null;
        client = null;
        System.gc();

    }

    class MockService {

        @RequestMapping(method = RequestMethod.POST)
        public String ping(String ping) {
            callCount.incrementAndGet();
            return ping + " pong";
        }
    }
}

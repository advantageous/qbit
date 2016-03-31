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

package io.advantageous.qbit.boon.client;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketBuilder;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;
import static junit.framework.Assert.assertEquals;


public class BoonClientIntegrationTest extends TimedTesting {

    Client client;
    AtomicBoolean httpStopCalled = new AtomicBoolean();
    AtomicBoolean httpStartCalled = new AtomicBoolean();
    AtomicBoolean httpSendWebSocketCalled = new AtomicBoolean();
    AtomicBoolean httpFlushCalled = new AtomicBoolean();
    AtomicBoolean httpPeriodicFlushCallbackCalled = new AtomicBoolean();
    boolean ok;
    AtomicInteger sum;
    volatile Response<Object> response;
    ServiceBundle serviceBundle;

    @Before
    public void setUp() throws Exception {

        setupLatch();
        sum = new AtomicInteger();

        client = new BoonClientFactory().create("/services", new HttpClientMock(), 10, new BeforeMethodSent() {
        });

        client.start();
        serviceBundle = new ServiceBundleBuilder().setAddress("/services").buildAndStart();
        serviceBundle.addService(new ServiceMock());
        sum.set(0);
        serviceBundle.startReturnHandlerProcessor(item -> response = item);
        Sys.sleep(1000);
    }

    @After
    public void tearDown() throws Exception {
        client.flush();
        Sys.sleep(1000);
        client.stop();
    }

    @Test
    public void testCreateProxy() throws Exception {
        client.start();
        Sys.sleep(100);

        final ServiceMockClientInterface mockService = client.createProxy(ServiceMockClientInterface.class, "serviceMock");

        mockService.add(1, 2);

        serviceBundle.flush();

        ((ClientProxy) mockService).clientProxyFlush();
        Sys.sleep(100);
        serviceBundle.flush();
        Sys.sleep(100);

        waitForTrigger(20, o -> httpSendWebSocketCalled.get());

        ok = httpSendWebSocketCalled.get() || die("Send called", httpSendWebSocketCalled);


    }


    //@Test
    public void testCallBack() throws Exception {
        client.start();
        Sys.sleep(1000);

        sum.set(0);

        final ServiceMockClientInterface mockService =
                client.createProxy(ServiceMockClientInterface.class, "serviceMock");

        mockService.add(1, 2);
        mockService.sum(integer -> sum.set(integer));

        client.flush();
        serviceBundle.flushSends();
        serviceBundle.flush();

        Sys.sleep(1000);
        ((ClientProxy) mockService).clientProxyFlush();
        client.flush();
        serviceBundle.flushSends();
        serviceBundle.flush();


        Sys.sleep(1000);
        waitForTrigger(20, o -> httpSendWebSocketCalled.get());
        ok = httpSendWebSocketCalled.get() || die();

        waitForTrigger(20, o -> sum.get() == 3);
        Sys.sleep(1000);

        assertEquals(3, sum.get());
    }

    public interface ServiceMockClientInterface {
        void add(@SuppressWarnings("SameParameterValue") int a, @SuppressWarnings("SameParameterValue") int b);

        void sum(Callback<Integer> callback);
    }

    public static class ServiceMock {
        int sum;

        public void add(int a, int b) {
            sum = sum + a + b;
        }

        public int sum() {
            return sum;
        }
    }

    private class HttpClientMock implements HttpClient {

        Consumer<Void> periodicFlushCallback;


        @Override
        public void start() {

        }

        @Override
        public void sendHttpRequest(HttpRequest request) {

        }


        @Override
        public WebSocket createWebSocket(final String uri) {

            final WebSocketBuilder webSocketBuilder = WebSocketBuilder.webSocketBuilder().setRemoteAddress("test").setUri(uri).setBinary(false).setOpen(true);

            final WebSocket webSocket = webSocketBuilder.build();

            final WebSocketSender webSocketSender = new WebSocketSender() {
                @Override
                public void sendText(final String body) {


                    httpSendWebSocketCalled.set(true);
                    periodicFlushCallback.accept(null);


                    final List<MethodCall<Object>> methodCalls = QBit.factory().createProtocolParser().parseMethodCalls("", body);

                    serviceBundle.call(methodCalls);

                    serviceBundle.flush();

                    Sys.sleep(100);

                    if (response != null) {

                        if (response.wasErrors()) {
                            puts("FAILED RESPONSE", response);
                        } else {
                            String simulatedMessageFromServer = QBit.factory().createEncoder().encodeResponses("", Lists.list(response));
                            webSocket.onTextMessage(simulatedMessageFromServer);
                        }
                    } else {
                        puts(response);
                    }

                }
            };

            BeanUtils.idx(webSocket, "networkSender", webSocketSender);

            return webSocket;
        }

        @Override
        public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {
            httpPeriodicFlushCallbackCalled.set(true);
            this.periodicFlushCallback = periodicFlushCallback;

        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public String getHost() {
            return "mock";
        }

        @Override
        public HttpClient startClient() {
            httpStartCalled.set(true);
            return this;

        }

        @Override
        public void flush() {
            httpFlushCalled.set(true);

        }

        @Override
        public void stop() {

            httpStopCalled.set(true);
        }
    }
}
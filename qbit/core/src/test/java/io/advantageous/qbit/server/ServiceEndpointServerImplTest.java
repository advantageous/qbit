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

package io.advantageous.qbit.server;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpTextReceiver;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.server.websocket.WebSocketMessageBuilder;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;

@SuppressWarnings("ALL")
public class ServiceEndpointServerImplTest extends TimedTesting {

    static AtomicInteger timeOutCounter = new AtomicInteger();
    volatile int callMeCounter = 0;
    volatile int responseCounter = 0;
    volatile int failureCounter = 0;
    volatile String lastResponse = "";
    private ServiceEndpointServer objectUnderTest;
    private ServiceEndpointServerImpl serviceServerImpl;
    private HttpServerMock httpServer;
    private boolean ok = true;

    @Before
    public void setup() {
        super.setupLatch();
        final Factory factory = QBit.factory();
        final ProtocolParser protocolParser = factory.createProtocolParser();
        final ProtocolEncoder encoder = factory.createEncoder();


        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").build();
        final JsonMapper mapper = factory.createJsonMapper();


        httpServer = new HttpServerMock();
        serviceServerImpl = new ServiceEndpointServerImpl(httpServer, encoder, protocolParser, serviceBundle, mapper, 1, 100, 30, 10, null, "", null, 8080, 0, null, null, 50, 2, 2);


        callMeCounter = 0;
        responseCounter = 0;
        serviceServerImpl.initServices(new ServiceMockObject());
        serviceServerImpl.start();

        Sys.sleep(500);


    }

    @Test
    public void testWithServiceQueue() {

        final Factory factory = QBit.factory();
        final ProtocolParser protocolParser = factory.createProtocolParser();
        final ProtocolEncoder encoder = factory.createEncoder();


        final Queue<Response<Object>> responseQueue = QueueBuilder.queueBuilder().setName("RESPONSE QUEUE TEST").build();

        final ServiceBundle serviceBundle = new ServiceBundleBuilder()
                .setResponseQueue(responseQueue).setAddress("/services").build();
        final JsonMapper mapper = factory.createJsonMapper();


        httpServer = new HttpServerMock();
        serviceServerImpl = new ServiceEndpointServerImpl(httpServer, encoder, protocolParser, serviceBundle,
                mapper, 1, 100, 30, 10, null, null, null, 8080, 0, null, null, 50, 2, 2);


        callMeCounter = 0;
        responseCounter = 0;


        ServiceQueue serviceQueue = serviceBuilder()
                .setResponseQueue(responseQueue)
                .setServiceObject(new MyOtherService()).buildAndStart();


        serviceServerImpl.addServiceQueue("/services/other/serviceCall", serviceQueue);

        serviceServerImpl.start();


        final HttpRequest request = new HttpRequestBuilder().setUri("/services/other/servicecall")
                .setTextReceiver(new MockReceiver()).setBody("\"call\"").build();

        httpServer.sendRequest(request);


        Sys.sleep(100);

        waitForTrigger(20, o -> responseCounter == 1 && callMeCounter == 1);

        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();

    }

    @Test
    public void testWebSocketCallWithServiceQueue() throws Exception {

        final Factory factory = QBit.factory();
        final ProtocolParser protocolParser = factory.createProtocolParser();
        final ProtocolEncoder encoder = factory.createEncoder();


        final Queue<Response<Object>> responseQueue = QueueBuilder.queueBuilder().setName("RESPONSE QUEUE").build();

        final ServiceBundle serviceBundle = new ServiceBundleBuilder()
                .setResponseQueue(responseQueue).setAddress("/services").build();
        final JsonMapper mapper = factory.createJsonMapper();


        httpServer = new HttpServerMock();
        serviceServerImpl = new ServiceEndpointServerImpl(httpServer, encoder, protocolParser, serviceBundle,
                mapper, 1, 100, 30, 10, null, null, null, 8080, 0, null, null, 50, 2, 2);


        callMeCounter = 0;
        responseCounter = 0;


        ServiceQueue serviceQueue = serviceBuilder()
                .setResponseQueue(responseQueue)
                .setServiceObject(new MyOtherService()).buildAndStart();


        serviceServerImpl.addServiceQueue("other", serviceQueue);

        serviceServerImpl.start();


        final MethodCall<Object> methodCall = new MethodCallBuilder().setObjectName("other").setName("method").setBody(null).build();

        final String message = QBit.factory().createEncoder().encodeMethodCalls("", Lists.list(methodCall));

        httpServer.sendWebSocketServerMessage(new WebSocketMessageBuilder().setRemoteAddress("/foo")
                .setMessage(message).setSender(new MockWebSocketSender()).build());


        Sys.sleep(100);


        waitForTrigger(20, o -> responseCounter == 1);

        Sys.sleep(10);

        ok |= responseCounter == 1 || die();
        ok |= failureCounter == 0 || die();

    }

    @Test
    public void testSimpleHTTPRequest() throws Exception {

        final HttpRequest request = new HttpRequestBuilder().setUri("/services/mock/callme").setTextReceiver(new MockReceiver()).setBody("").build();

        httpServer.sendRequest(request);


        Sys.sleep(10);
        serviceServerImpl.flush();
        Sys.sleep(10);

        waitForTrigger(20, o -> responseCounter == 1 && callMeCounter == 1);

        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();


    }

    @Test
    public void testTimeOut() throws Exception {

        timeOutCounter.set(0);
        Sys.sleep(10);

        final HttpRequest request = new HttpRequestBuilder().setUri("/services/mock/timeOut").setTextReceiver(new MockReceiver()).setBody("").build();

        httpServer.sendRequest(request);


        waitForTrigger(20, o -> timeOutCounter.get() >= 1);


        ok |= responseCounter == 0 || die();
        ok |= callMeCounter == 0 || die();
        ok |= timeOutCounter.get() >= 1 || die(); //TODO fix


    }

    @Test
    public void testSimplePOST_HTTPRequest() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/callPost")
                .setTextReceiver(new MockReceiver()).setMethod("POST").setBody("[]").build();

        httpServer.sendRequest(request);

        Sys.sleep(10);

        serviceServerImpl.flush();

        Sys.sleep(10);


        waitForTrigger(20, o -> responseCounter == 1 && callMeCounter == 1);

        Sys.sleep(10);


        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();

        ok |= lastResponse.equals("\"baconPOST\"") || die();


    }

    @Test
    public void testSimplePOST_HTTPRequest_ErrorWrongHttpMethod() throws Exception {

        failureCounter = 0;
        responseCounter = 0;
        callMeCounter = 0;

        final HttpRequest request = new HttpRequestBuilder().setUri("/services/mock/callPost").setTextReceiver(new MockReceiver()).setBody("[]").build();

        httpServer.sendRequest(request);

        waitForTrigger(20, o -> failureCounter == 1);


        ok |= failureCounter == 1 || die();
        ok |= callMeCounter == 0 || die();

        ok |= responseCounter == 0 || die();
        puts(lastResponse);
    }

    @Test
    public void testAsyncCallHttp() throws Exception {

        final HttpRequest request = new HttpRequestBuilder().setUri("/services/mock/callWithReturn")
                .setTextReceiver(new MockReceiver()).setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(10);
        serviceServerImpl.flush();

        Sys.sleep(10);


        waitForTrigger(20, o -> responseCounter == 1 && callMeCounter == 1);


        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();
        ok |= lastResponse.equals("\"bacon\"") || die();


    }

    @Test
    public void testWeSocketCallThatIsCrap() throws Exception {


        httpServer.sendWebSocketServerMessage(new WebSocketMessageBuilder().setMessage("CRAP")

                .setRemoteAddress("/crap/at/crap").setSender(new MockWebSocketSender()).build());


        Sys.sleep(10);
        serviceServerImpl.flush();

        Sys.sleep(10);


        waitForTrigger(20, o -> responseCounter == 1 && failureCounter == 1);

        ok |= responseCounter == 1 || die();
        ok |= failureCounter == 1 || die();

    }

    @Test
    public void testWebSocketCall() throws Exception {

        final MethodCall<Object> methodCall = new MethodCallBuilder().setObjectName("serviceMockObject").setName("callWithReturn").setBody(null).build();

        final String message = QBit.factory().createEncoder().encodeMethodCalls("", Lists.list(methodCall));

        httpServer.sendWebSocketServerMessage(new WebSocketMessageBuilder().setRemoteAddress("/foo").setMessage(message).setSender(new MockWebSocketSender()).build());


        Sys.sleep(10);


        serviceServerImpl.flush();

        Sys.sleep(10);


        waitForTrigger(20, o -> responseCounter == 1);

        Sys.sleep(10);

        ok |= responseCounter == 1 || die();
        ok |= failureCounter == 0 || die();

    }

    @Test
    public void testExceptionCall() throws Exception {

        final HttpRequest request = new HttpRequestBuilder().setUri("/services/mock/exceptionCall").setTextReceiver(new MockReceiver()).setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(10);


        serviceServerImpl.flush();

        Sys.sleep(10);


        waitForTrigger(20, o -> callMeCounter == 1 && failureCounter == 1);

        ok |= failureCounter == 1 || die();
        ok |= callMeCounter == 1 || die();

        puts(lastResponse);


    }

    @Test
    public void testExceptionCallWebSocket() throws Exception {

        final MethodCall<Object> methodCall = new MethodCallBuilder()
                .setObjectName("serviceMockObject").setName("exceptionCall").setBody(null).build();

        final String message = QBit.factory().createEncoder().encodeMethodCalls("", Lists.list(methodCall));

        httpServer.sendWebSocketServerMessage(new WebSocketMessageBuilder().setRemoteAddress("/error").setMessage(message).setSender(new MockWebSocketSender()).build());


        Sys.sleep(5);


        serviceServerImpl.flush();

        Sys.sleep(5);


        waitForTrigger(20, o -> callMeCounter == 1 && failureCounter == 1);
        ok |= failureCounter == 1 || die();
        ok |= callMeCounter == 1 || die();

    }

    public interface MyOtherInterface {
        void method(Callback<String> callback, String arg);
    }

    @RequestMapping("/other")
    public class MyOtherService {

        @RequestMapping("/serviceCall")
        public void method(Callback<String> callback, String arg) {
            callMeCounter++;
            callback.accept(arg);
        }
    }

    @RequestMapping("/mock")
    public class ServiceMockObject {

        @RequestMapping("/callme")
        public void callMe() {
            callMeCounter++;
        }

        @RequestMapping("/timeOut")
        public String timeOut() {


            puts("TIMEOUT");
            Sys.sleep(30000);

            return "ok";
        }


        @RequestMapping("/callWithReturn")
        public String callWithReturn() {
            callMeCounter++;
            return "bacon";
        }

        @RequestMapping(value = "/callPost", method = RequestMethod.POST)
        public String callPOST() {
            callMeCounter++;
            return "baconPOST";
        }


        @RequestMapping("/exceptionCall")
        public String exceptionCall() {
            callMeCounter++;
            throw new RuntimeException("EXCEPTION_CALL");
        }
    }

    class MockReceiver implements HttpTextReceiver {

        @Override
        public void response(int code, String mimeType, String body) {

            puts("RESPONSE", code, mimeType, body);
            lastResponse = body;

            if (code == 200 || code == 202) {
                responseCounter++;
            } else if (code == 408) {
                timeOutCounter.incrementAndGet();
                puts("GOT TIME OUT 408", timeOutCounter);
            } else {
                failureCounter++;
                puts("FAILURE", code, mimeType, body);
            }

        }
    }

    class MockWebSocketSender implements WebSocketSender {
        @Override
        public void sendText(final String message) {

            puts("MESSAGE", message);

            final List<Message<Object>> messages = QBit.factory().createProtocolParser().parse("", message);

            if (messages.size() == 1) {


                responseCounter++;
                final Response<Object> response = (Response<Object>) messages.get(0);
                if (response.wasErrors()) {
                    failureCounter++;
                }
            }

        }

        @Override
        public void sendBytes(byte[] message) {
            //Binary not supported yet
        }


    }

    class HttpServerMock implements HttpServer {
        Consumer<WebSocketMessage> webSocketMessageConsumer;
        Consumer<HttpRequest> requestConsumer;
        private Consumer<Void> idleConsumerRequest = new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {

            }
        };
        private Consumer<Void> idleConsumerWebSocket = new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {

            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                //Thread.currentThread().setDaemon(true);

                while (true) {
                    Sys.sleep(10);


                    idleConsumerRequest.accept(null);
                    idleConsumerWebSocket.accept(null);
                }

            }
        });

        {
            thread.start();
        }

        public void sendWebSocketServerMessage(WebSocketMessage ws) {
            webSocketMessageConsumer.accept(ws);
            this.idleConsumerWebSocket.accept(null);

        }


        public void sendRequest(HttpRequest request) {

            requestConsumer.accept(request);
            idleConsumerRequest.accept(null);
        }


        @Override
        public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {
            this.webSocketMessageConsumer = webSocketMessageConsumer;
        }

        @Override
        public void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

        }

        @Override
        public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
            this.requestConsumer = httpRequestConsumer;
        }

        @Override
        public void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer) {

            this.idleConsumerRequest = idleConsumer;
        }

        @Override
        public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {

            this.idleConsumerWebSocket = idleConsumer;

        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }

}
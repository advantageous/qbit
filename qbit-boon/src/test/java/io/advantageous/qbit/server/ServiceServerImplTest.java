package io.advantageous.qbit.server;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.sender.Sender;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import org.boon.Lists;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;
import static org.junit.Assert.*;

public class ServiceServerImplTest {

    private ServiceServer objectUnderTest;
    private ServiceServerImpl serviceServerImpl;
    private HttpServerMock httpServer;
    private boolean ok = true;


    volatile int callMeCounter = 0;

    volatile int responseCounter = 0;


    volatile int failureCounter = 0;


    volatile int timeOutCounter = 0;

    volatile String lastResponse = "";

    @RequestMapping("/mock")
    public  class ServiceMockObject {

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

    @Before
    public void setup() {
        final Factory factory = QBit.factory();
        final ProtocolParser protocolParser = factory.createProtocolParser();
        final ProtocolEncoder encoder = factory.createEncoder();
        final ServiceBundle serviceBundle = factory.createServiceBundle("/services");
        final JsonMapper mapper = factory.createJsonMapper();



        httpServer = new HttpServerMock();
        serviceServerImpl = new ServiceServerImpl(httpServer,
                                encoder,
                                protocolParser,
                                serviceBundle,
                                mapper,
                                1, 100);


        callMeCounter = 0;
        responseCounter = 0;
        serviceServerImpl.initServices(new ServiceMockObject());
        serviceServerImpl.start();

        Sys.sleep(500);





    }

    @Test
    public void testSimpleHTTPRequest() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/callme")
                .setTextResponse(new MockResponse())
                .setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);


        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();



    }

    @Test
    public void testTimeOut() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/timeOut")
                .setTextResponse(new MockResponse())
                .setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(3000);


        ok |= responseCounter == 0 || die();
        ok |= callMeCounter == 0 || die();
        ok |= timeOutCounter == 1 || die();




    }

    @Test
    public void testSimplePOST_HTTPRequest() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/callPost")
                .setTextResponse(new MockResponse()).setMethod("POST")
                .setBody("[]").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);

        serviceServerImpl.flush();

        Sys.sleep(200);


        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();

        ok |= lastResponse.equals("\"baconPOST\"") || die();



    }


    @Test
    public void testSimplePOST_HTTPRequest_ErrorWrongHttpMethod() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/callPost")
                .setTextResponse(new MockResponse())
                .setBody("[]").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);


        ok |= responseCounter == 0 || die();

        ok |= failureCounter == 1 || die();
        ok |= callMeCounter == 1 || die();

        puts(lastResponse);


    }

    @Test
    public void testAsyncCallHttp() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/callWithReturn")
                .setTextResponse(new MockResponse())
                .setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);
        serviceServerImpl.flush();

        Sys.sleep(200);



        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();
        ok |= lastResponse.equals("\"bacon\"") || die();



    }

    @Test
    public void testWesocketCallThatIsCrap() throws Exception {


        httpServer.sendWebSocketMessage(new WebSocketMessageBuilder().setMessage("CRAP").setSender(new MockWebSocketSender()).build());


        Sys.sleep(200);
        serviceServerImpl.flush();

        Sys.sleep(200);


        ok |= responseCounter == 1 || die();
        ok |= failureCounter == 1 || die();

    }

    @Test
    public void testWebSocketCall() throws Exception {

        final MethodCall<Object> methodCall = new MethodCallBuilder().setObjectName("serviceMockObject").setName("callWithReturn").setBody(null).build();

        final String message = QBit.factory().createEncoder().encodeAsString(Lists.list(methodCall));

        httpServer.sendWebSocketMessage(new WebSocketMessageBuilder().setMessage(message).setSender(new MockWebSocketSender()).build());



        Sys.sleep(200);


        serviceServerImpl.flush();

        Sys.sleep(200);



        ok |= responseCounter == 1 || die();
        ok |= failureCounter == 0 || die();

    }



    @Test
    public void testExceptionCall() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/exceptionCall")
                .setTextResponse(new MockResponse())
                .setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);


        serviceServerImpl.flush();

        Sys.sleep(200);




        ok |= failureCounter == 1 || die();
        ok |= callMeCounter == 1 || die();
        ok |= lastResponse.equals("\"java.lang.RuntimeException: EXCEPTION_CALL\"") || die();



    }

    @Test
    public void testExceptionCallWebSocket() throws Exception {

        final MethodCall<Object> methodCall = new MethodCallBuilder().setObjectName("serviceMockObject").setName("exceptionCall").setBody(null).build();

        final String message = QBit.factory().createEncoder().encodeAsString(Lists.list(methodCall));

        httpServer.sendWebSocketMessage(new WebSocketMessageBuilder().setMessage(message).setSender(new MockWebSocketSender()).build());



        Sys.sleep(200);


        serviceServerImpl.flush();

        Sys.sleep(200);




        ok |= failureCounter == 1 || die();
        ok |= callMeCounter == 1 || die();

    }





    class MockResponse implements HttpTextResponse {

        @Override
        public void response(int code, String mimeType, String body) {

            puts("RESPONSE", code, mimeType, body);
            lastResponse = body;

            if (code==200) {
                responseCounter++;
            } if (code == 408) {
                timeOutCounter++;
            }
            else {
                failureCounter++;
                puts("FAILURE", code, mimeType, body);
            }

        }
    }

    class MockWebSocketSender implements WebsSocketSender {
        @Override
        public void send(String message) {

            Response<Object> response = QBit.factory().createProtocolParser().parseResponse(message);
            responseCounter++;
            if (response.wasErrors()) {
                failureCounter++;
            }

        }


    }

    class HttpServerMock implements HttpServer {
        Consumer<WebSocketMessage> webSocketMessageConsumer;
        Consumer<HttpRequest> requestConsumer;

        public void sendWebSocketMessage(WebSocketMessage ws) {
            webSocketMessageConsumer.accept(ws);
        }


        public void sendRequest(HttpRequest request) {
            requestConsumer.accept(request);
        }


        @Override
        public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {
            this.webSocketMessageConsumer = webSocketMessageConsumer;
        }

        @Override
        public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
            this.requestConsumer = httpRequestConsumer;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }

}
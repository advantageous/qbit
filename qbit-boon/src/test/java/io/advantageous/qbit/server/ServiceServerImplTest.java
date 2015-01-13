package io.advantageous.qbit.server;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
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


    transient int callMeCounter = 0;

    transient int responseCounter = 0;


    transient int failureCounter = 0;

    transient String lastResponse = "";

    @RequestMapping("/mock")
    public  class ServiceMockObject {

        @RequestMapping("/callme")
        public void callMe() {
           callMeCounter++;
        }



        @RequestMapping("/callWithReturn")
        public String callWithReturn() {
            callMeCounter++;
            return "bacon";
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
                                1);


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
                .setResponse(new MockResponse())
                .setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);


        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();



    }


    @Test
    public void testAsyncCallHttp() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/callWithReturn")
                .setResponse(new MockResponse())
                .setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);


        ok |= responseCounter == 1 || die();
        ok |= callMeCounter == 1 || die();
        ok |= lastResponse.equals("\"bacon\"") || die();



    }


    @Test
    public void testExceptionCall() throws Exception {

        final HttpRequest request = new HttpRequestBuilder()
                .setUri("/services/mock/exceptionCall")
                .setResponse(new MockResponse())
                .setBody("").build();

        httpServer.sendRequest(request);

        Sys.sleep(200);


        //left off here 1/13/2015
        ok |= callMeCounter == 1 || die();
        ok |= lastResponse.equals("\"java.lang.RuntimeException: EXCEPTION_CALL\"") || die();



    }



    class MockResponse implements HttpResponse {

        @Override
        public void response(int code, String mimeType, String body) {

            puts("RESPONSE", code, mimeType, body);
            lastResponse = body;

            if (code==200) {
                responseCounter++;
            } else {
                failureCounter++;
                puts("FAILURE", code, mimeType, body);
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
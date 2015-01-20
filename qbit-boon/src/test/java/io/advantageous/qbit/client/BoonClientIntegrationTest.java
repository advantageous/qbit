package io.advantageous.qbit.client;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceBundle;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class BoonClientIntegrationTest {

    Client client;
    boolean httpStopCalled;

    boolean httpStartCalled;


    boolean httpSendWebSocketCalled;

    boolean httpFlushCalled;

    boolean httpPeriodicFlushCallbackCalled;

    boolean ok;
    volatile int sum;

    volatile Response<Object> response;


    ServiceBundle serviceBundle;


    public static interface ServiceMockClientInterface {
        void add(int a, int b);

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
    @Before
    public void setUp() throws Exception {

        client = new BoonClientFactory().create("/services",
                new HttpClientMock() , 10);

        client.start();

        serviceBundle = QBit.factory().createServiceBundle("/services");
        serviceBundle.addService(new ServiceMock());
        sum = 0;

        serviceBundle.startReturnHandlerProcessor(new ReceiveQueueListener<Response<Object>>() {
            @Override
            public void receive(Response<Object> item) {
                response = item;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        client.flush();
        Sys.sleep(100);
        client.stop();

    }

    @Test
    public void testCreateProxy() throws Exception {
        client.start();
        Sys.sleep(100);

        final ServiceMockClientInterface mockService = client.createProxy(ServiceMockClientInterface.class, "serviceMock");

        mockService.add(1, 2);

        serviceBundle.flush();

        ((ClientProxy)mockService).clientProxyFlush();
        Sys.sleep(100);
        serviceBundle.flush();
        Sys.sleep(100);

        ok = httpSendWebSocketCalled || die();



    }


    @Test
    public void testCallBack() throws Exception {
        client.start();
        Sys.sleep(100);

        final ServiceMockClientInterface mockService = client.createProxy(ServiceMockClientInterface.class, "serviceMock");


        mockService.add(1, 2);
        mockService.sum(integer -> sum = integer);

        ((ClientProxy)mockService).clientProxyFlush();

        ok = httpSendWebSocketCalled || die();


        ok = sum == 3 || die(sum);

    }


    private class HttpClientMock implements HttpClient {

        Consumer<Void> periodicFlushCallback;


        @Override
        public void sendHttpRequest(HttpRequest request) {

        }

        @Override
        public void sendWebSocketMessage(WebSocketMessage webSocketMessage) {

            httpSendWebSocketCalled = true;
            puts(webSocketMessage);
            periodicFlushCallback.accept(null);

            final String body = webSocketMessage.body().toString();

            final List<MethodCall<Object>> methodCalls = QBit.factory().createProtocolParser().parseMethods(body);

            serviceBundle.call(methodCalls);

            serviceBundle.flush();

            Sys.sleep(100);

            if (response!=null) {

                if (response.wasErrors()) {
                    puts("FAILED RESPONSE",response);
                } else {
                    webSocketMessage.getSender().send(QBit.factory().createEncoder().encodeAsString(response));
                }
            } else {
                puts(response);
            }

        }

        @Override
        public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {
            httpPeriodicFlushCallbackCalled = true;
            this.periodicFlushCallback = periodicFlushCallback;

        }

        @Override
        public void start() {
            httpStartCalled = true;

        }

        @Override
        public void flush() {
            httpFlushCalled = true;

        }

        @Override
        public void stop() {

            httpStopCalled = true;
        }
    }
}
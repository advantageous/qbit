package io.advantageous.qbit.client;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;
import static org.junit.Assert.*;

public class BoonClientTest {

    Client client;
    boolean httpStopCalled;

    boolean httpStartCalled;


    boolean httpSendWebSocketCalled;

    boolean httpFlushCalled;

    boolean httpPeriodicFlushCallbackCalled;

    boolean ok;


    public static interface ServiceMock {
        void add(int a, int b);

        void sum(Callback<Integer> callback);
    }

    @Before
    public void setUp() throws Exception {

        client = new BoonClientFactory().create("/uri", new HttpClientMock() , 10);

    }

    @After
    public void tearDown() throws Exception {
        client.flush();
        client.stop();

    }

    @Test
    public void testStop() throws Exception {

        client.stop();
        ok = httpStopCalled || die();

    }

    @Test
    public void testFlush() throws Exception {

        client.flush();
    }

    @Test
    public void testCreateProxy() throws Exception {
        client.start();
        Sys.sleep(100);

        final ServiceMock mockService = client.createProxy(ServiceMock.class, "mockService");

        mockService.add(1, 2);

        ((ClientProxy)mockService).clientProxyFlush();

        Sys.sleep(100);

        ok = httpSendWebSocketCalled || die();



    }


    @Test
    public void testCallBack() throws Exception {
        client.start();
        Sys.sleep(100);

        final ServiceMock mockService = client.createProxy(ServiceMock.class, "mockService");

        mockService.sum(integer -> puts("SUM", integer));

        ((ClientProxy)mockService).clientProxyFlush();

        Sys.sleep(100);

        ok = httpSendWebSocketCalled || die();



    }



    @Test
    public void testStart() throws Exception {

        client.start();
        ok = httpPeriodicFlushCallbackCalled || die();
        ok = httpStartCalled || die();
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
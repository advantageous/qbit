/*******************************************************************************

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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.client;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketBuilder;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpClientFactory;
import org.boon.core.Sys;
import org.boon.core.reflection.BeanUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;
import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class BoonClientTest {

    Client client;
    boolean httpStopCalled;
    boolean httpStartCalled;
    boolean httpSendWebSocketCalled;
    boolean httpFlushCalled;
    boolean httpPeriodicFlushCallbackCalled;
    boolean ok;
    volatile int sum;

    @Before
    public void setUp() throws Exception {
        client = new BoonClientFactory().create("/uri", new HttpClientMock(), 10);
        FactorySPI.setHttpClientFactory(new HttpClientFactory() {

            @Override
            public HttpClient create(String host, int port, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush, int flushRate, boolean keepAlive, boolean pipeLine) {
                return new HttpClientMock();
            }
        });


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

        ( ( ClientProxy ) mockService ).clientProxyFlush();

        Sys.sleep(100);

        ok = httpSendWebSocketCalled || die();


    }

    @Test
    public void testWithBuilder() throws Exception {

        final ClientBuilder clientBuilder = new ClientBuilder();
        ok = clientBuilder.setFlushInterval(100).getFlushInterval() == 100 || die();
        ok = clientBuilder.setPollTime(51).getPollTime() == 51 || die();
        ok = clientBuilder.setRequestBatchSize(13).getRequestBatchSize() == 13 || die();
        ok = clientBuilder.setAutoFlush(true).isAutoFlush() == true || die();
        ok = clientBuilder.setAutoFlush(false).isAutoFlush() == false || die();
        ok = clientBuilder.setAutoFlush(true).isAutoFlush() == true || die();
        ok = clientBuilder.setUri("/book").getUri().equals("/book") || die();
        ok = clientBuilder.setUri("/services").getUri().equals("/services") || die();
        ok = clientBuilder.setHost("host").getHost().equals("host") || die();
        ok = clientBuilder.setHost("localhost").getHost().equals("localhost") || die();
        ok = clientBuilder.setPort(9090).getPort() == 9090 || die();
        ok = clientBuilder.setPort(8080).getPort() == 8080 || die();
        ok = clientBuilder.setTimeoutSeconds(67).getTimeoutSeconds() == 67 || die();
        ok = clientBuilder.setTimeoutSeconds(5).getTimeoutSeconds() == 5 || die();
        ok = clientBuilder.setProtocolBatchSize(5).getProtocolBatchSize() == 5 || die();
        ok = clientBuilder.setProtocolBatchSize(50).getProtocolBatchSize() == 50 || die();

        client = clientBuilder.build();

        ok = clientBuilder.setProtocolBatchSize(-1).getProtocolBatchSize() == -1 || die();

        client = clientBuilder.build();

        client.start();

        Sys.sleep(100);

        final ServiceMock mockService = client.createProxy(ServiceMock.class, "mockService");


        mockService.sum(integer -> puts("SUM", integer));

        ( ( ClientProxy ) mockService ).clientProxyFlush();


        Sys.sleep(100);

        ok = httpSendWebSocketCalled || die();


    }

    @Test
    public void testCallBack() throws Exception {
        client.start();
        Sys.sleep(100);

        final ServiceMock mockService = client.createProxy(ServiceMock.class, "mockService");

        mockService.sum(integer -> puts("SUM", integer));

        ( ( ClientProxy ) mockService ).clientProxyFlush();

        Sys.sleep(100);

        ok = httpSendWebSocketCalled || die();


    }

    @Test
    public void testStart() throws Exception {

        client.start();
        ok = httpPeriodicFlushCallbackCalled || die();
        ok = httpStartCalled || die();
    }


    public static interface ServiceMock {
        void add(int a, int b);

        void sum(Callback<Integer> callback);
    }

    private class HttpClientMock implements HttpClient {

        Consumer<Void> periodicFlushCallback;


        @Override
        public void sendHttpRequest(HttpRequest request) {

        }


        @Override
        public WebSocket createWebSocket(final String uri) {

            final WebSocketBuilder webSocketBuilder = webSocketBuilder().setRemoteAddress("test").setUri(uri).setBinary(false).setOpen(true);

            final WebSocket webSocket = webSocketBuilder.build();

            final WebSocketSender webSocketSender = new WebSocketSender() {
                @Override
                public void sendText(final String body) {


                    httpSendWebSocketCalled = true;
                    periodicFlushCallback.accept(null);
                    Sys.sleep(100);

                }
            };

            BeanUtils.idx(webSocket, "networkSender", webSocketSender);

            return webSocket;
        }

        @Override
        public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {
            httpPeriodicFlushCallbackCalled = true;
            this.periodicFlushCallback = periodicFlushCallback;

        }

        @Override
        public HttpClient start() {
            httpStartCalled = true;
            return this;

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
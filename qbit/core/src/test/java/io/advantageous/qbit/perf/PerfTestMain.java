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

package io.advantageous.qbit.perf;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpClientFactory;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static io.advantageous.boon.core.IO.puts;


/**
 * created by Richard on 12/7/14.
 */
public class PerfTestMain {


    static Queue<WebSocketMessage> messages = new QueueBuilder().setName("websocket sim").setPollWait(100).setLinkedBlockingQueue().setBatchSize(5).build();


    static Object context = Sys.contextToHold();

    public static void main(String... args) {

        FactorySPI.setHttpClientFactory(new HttpClientFactory() {


            @Override
            public HttpClient create(String host, int port, int timeOutInMilliseconds, int poolSize, boolean autoFlush, int flushRate, boolean keepAlive, boolean pipeLine, boolean ssl, boolean verifyHost, boolean trustAll, int maxWebSocketFrameSize, boolean tryUseCompression, String trustStorePath, String trustStorePassword, boolean tcpNoDelay, int soLinger) {
                return new MockHttpClient();
            }

        });


        FactorySPI.setHttpServerFactory((options, name, systemManager, serviceDiscovery, healthServiceAsync, a, b, c, d, z)
                -> new MockHttpServer());
        ServiceEndpointServer server = new EndpointServerBuilder().build();
        server.initServices(new AdderService());
        server.start();


        puts("Server started");


        Client client = new ClientBuilder().setAutoFlush(true).setFlushInterval(50).setProtocolBatchSize(100).build();

        AdderClientInterface adder = client.createProxy(AdderClientInterface.class, "adderservice");

        client.start();


        puts("Client started");

        final long startTime = System.currentTimeMillis();

        for (int index = 0; index < 80_000_000; index++) {
            adder.add("name", 1);


            if (index % 400_000 == 0) {
                adder.sum(integer -> {


                    final long endTime = System.currentTimeMillis();

                    puts("sum", integer, "time", endTime - startTime, "rate", (integer / (endTime - startTime) * 1000));
                });
            }
        }

        client.flush();

        adder.sum(new Callback<Integer>() {
            @Override
            public void accept(Integer integer) {


                final long endTime = System.currentTimeMillis();

                puts("sum", integer, "time", endTime - startTime, "rate", (integer / (endTime - startTime) * 1000));
            }
        });


        client.flush();

        adder.sum(new Callback<Integer>() {
            @Override
            public void accept(Integer integer) {


                final long endTime = System.currentTimeMillis();

                puts("sum", integer, "time", endTime - startTime, "rate", (integer / (endTime - startTime) * 1000));
            }
        });


        client.flush();

        adder.sum(new Callback<Integer>() {
            @Override
            public void accept(Integer integer) {


                final long endTime = System.currentTimeMillis();

                puts("sum", integer, "time", endTime - startTime, "rate", (integer / (endTime - startTime) * 1000));
            }
        });


        client.flush();


        adder.sum(new Callback<Integer>() {
            @Override
            public void accept(Integer integer) {


                final long endTime = System.currentTimeMillis();
                puts("FINAL 1 sum", integer, "time", endTime - startTime);

            }
        });


        client.flush();

        adder.sum(new Callback<Integer>() {
            @Override
            public void accept(Integer integer) {


                final long endTime = System.currentTimeMillis();
                puts("FINAL 2 sum", integer, "time", endTime - startTime);
            }
        });


        client.flush();


        Sys.sleep(200_000);


        client.stop();
    }

    static class MockHttpServer implements HttpServer {

        private Consumer<WebSocketMessage> webSocketMessageConsumer;
        private Consumer<HttpRequest> httpRequestConsumer;


        @Override
        public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

            this.webSocketMessageConsumer = webSocketMessageConsumer;
        }

        @Override
        public void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

        }

        @Override
        public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {

            this.httpRequestConsumer = httpRequestConsumer;
        }

        @Override
        public void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer) {

        }

        @Override
        public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {

        }

        @Override
        public void start() {

            messages.startListener(new ReceiveQueueListener<WebSocketMessage>() {
                @Override
                public void receive(WebSocketMessage item) {

                    webSocketMessageConsumer.accept(item);
                }

                @Override
                public void empty() {

                }

                @Override
                public void limit() {

                }

                @Override
                public void shutdown() {

                }

                @Override
                public void idle() {

                }
            });
        }

        @Override
        public void stop() {

        }
    }

    static class MockHttpClient implements HttpClient {

        Consumer<Void> periodicFlushCallback;
        SendQueue<WebSocketMessage> sendQueue;
        Thread thread;
        ReentrantLock lock = new ReentrantLock();

        public void start() {

        }

        @Override
        public void sendHttpRequest(HttpRequest request) {

        }

//        @Override
//        public void zendWebSocketMessage(WebSocketMessage webSocketMessage) {
//
//            try {
//                lock.lock();
//
//                sendQueue.forwardEvent(webSocketMessage);
//            } finally {
//                lock.unlock();
//            }
//        }

        @Override
        public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {

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
            sendQueue = messages.sendQueue();

            thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        Sys.sleep(50);

                        periodicFlushCallback.accept(null);
                        sendQueue.flushSends();

                        if (thread.isInterrupted()) {
                            break;
                        }
                    }
                }
            });
            thread.start();
            return this;

        }

        @Override
        public void flush() {

            periodicFlushCallback.accept(null);
        }

        @Override
        public void stop() {

            thread.interrupt();
        }
    }
}

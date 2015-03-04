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

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.boon.core.Sys;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by rhightower on 11/12/14.
 */
public class HttpPerfClientTest {


    private static final int REQUEST_COUNT = 20_000_000;
    private static final int CLIENT_COUNT = 10;
    private static volatile LongAdder errorCount = new LongAdder();
    private static volatile LongAdder receivedCount = new LongAdder();

    public static void main(String... args) throws InterruptedException {

        puts("Arguments", args);


        String argHost = "localhost";
        int argPort = 8080;
        int argBatchSize = 10;
        int argPoolSize = 100;
        int argPollTime = 10;


        if (args.length > 0) {

            argHost = args[0];
        }


        if (args.length > 1) {

            argPort = Integer.parseInt(args[1]);
        }


        if (args.length > 2) {

            argBatchSize = Integer.parseInt(args[2]);
        }

        if (args.length > 3) {

            argPoolSize = Integer.parseInt(args[3]);
        }

        if (args.length > 4) {

            argPollTime = Integer.parseInt(args[4]);
        }


        final String host = argHost;
        final int port = argPort;
        final int batchSize = argBatchSize;
        final int poolSize = argPoolSize;
        final int pollTime = argPollTime;


        puts("Params for client host", host, "port", port);

        puts("\nParams for client batchSize", batchSize, "poolSize", poolSize);

        puts("Params for client pollTime", pollTime);


        final long startTime;

        final HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder();

        final HttpRequest perfRequest = httpRequestBuilder
                .setContentType("application/json")
                .setMethod("GET").setUri("/perf/").addHeader("X-USER", "BOB")
                .setTextReceiver((code, mimeType, body) -> {
                    if (code != 200 || !body.equals("\"ok\"")) {
                        errorCount.increment();
                        return;
                    }

                    receivedCount.increment();


                })
                .build();


        final int countPerThread = REQUEST_COUNT / CLIENT_COUNT;

        final List<Thread> threads = new ArrayList<>(CLIENT_COUNT);


        for (int threadNum = 0; threadNum < CLIENT_COUNT; threadNum++) {


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    //final HttpClientVertx client = new HttpClientVertx(9090, "localhost", false);

                    HttpClient client = new HttpClientBuilder().setPort(port)
                            .setHost(host)
                            .setPoolSize(poolSize).setRequestBatchSize(batchSize).
                                    setPollTime(pollTime).setAutoFlush(true)
                            .build();
                    client.start();

                    Sys.sleep(5000);

                    for (int index = 0; index < countPerThread; index++) {
                        client.sendHttpRequest(perfRequest);

                        if (index % 30_000 == 0) {
                            Sys.sleep(3_000);
                        }

//                        if (index % 200_000 == 0) {
//                            client.stop();
//                            Sys.sleep(1_000);
//                            client = new HttpClientBuilder().setPort(port)
//                                    .setHost(host)
//                                    .setPoolSize(poolSize).setRequestBatchSize(batchSize).
//                                            setPollTime(pollTime).setAutoFlush(true)
//                                    .build().start();
//                            client.start();
//
//
//                        }

                    }


                    Sys.sleep(20_000);
                    client.stop();
                }
            });

            threads.add(thread);


        }


        for (Thread t : threads) {

            t.start();
        }

        Sys.sleep(5000);

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 100_000; i++) {
            if (receivedCount.sum() + errorCount.sum() >= REQUEST_COUNT - 5000) {
                long duration = System.currentTimeMillis() - startTime;
                puts("DURATION", duration / 1000, "Recieved Count", receivedCount);
                break;

            }

            if (i % 10 == 0) {
                long duration = System.currentTimeMillis() - startTime;
                puts("DURATION", duration / 1000, "count", receivedCount, "errors", errorCount);
            }
            Sys.sleep(100);

        }


        for (Thread t : threads) {
            t.join();
        }


        Sys.sleep(1000);
        puts("\n\nerror count ", errorCount, "\nreceived count", receivedCount);


        Sys.sleep(1000);
        puts("\n\nerror count ", errorCount, "\nreceived count", receivedCount);

        Sys.sleep(2000);
        puts("\n\nerror count ", errorCount, "\nreceived count", receivedCount);

        long duration = System.currentTimeMillis() - startTime;
        puts("DURATION", duration / 1000, "Recieved Count", receivedCount);

        System.exit(0);


    }
}

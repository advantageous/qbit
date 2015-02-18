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

package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;

import java.util.ArrayList;
import java.util.List;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/2/15.
 */
public class SimpleWebSocketClient {

    static volatile int count = 0;

    public static void main(String... args) {

        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6060;


        List<Thread> threads = new ArrayList<>();


        for (int numThreads = 0; numThreads < 2; numThreads++) {
            final Client client = new ClientBuilder().setPoolSize(1).setPort(port).setHost(host).setRequestBatchSize(10_000)
                    .build();

            client.start();


            Thread thread = new Thread(new Runnable() {


                final SimpleServiceProxy myService = client.createProxy(SimpleServiceProxy.class, "myService");

                @Override
                public void run() {

                    for (int index = 0; index < 11_000_000; index++) {
                        myService.ping(strings -> {
                            count++;
                        });

                        if (index % 15_000 == 0) {
                            Sys.sleep(10);
                        }
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }


        double start = System.currentTimeMillis();
        while (count < 10_000_000) {
            for (int index = 0; index < 10; index++) {
                Sys.sleep(100);

                if (count > 10_000_000) {
                    break;
                }
            }
            double now = System.currentTimeMillis();
            double c = count;
            puts(count, (c / (now - start) * 1000));
        }

        puts("Done");
    }

    interface SimpleServiceProxy {

        void ping(Callback<List<String>> callback);

    }
}

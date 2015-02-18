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

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpTextReceiver;
import org.boon.core.Sys;

import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.request.HttpRequestBuilder.httpRequestBuilder;
import static org.boon.Boon.puts;


/**
 * Created by rhightower on 1/29/15.
 */
public class SimpleRestClient {

    static volatile int count = 0;

    public static void main(String... args) throws Exception {

//       final long start = System.currentTimeMillis();
//
//        final HttpClient httpClient = httpClientBuilder().setPoolSize(20)
//                .setPort(6060).build().start().start();
//
//
//        for (int index = 0; index < 10; index++) {
//
//            final HttpResponse httpResponse =
//                    httpClient.get("/services/myservice/ping");
//
//            puts(httpResponse.body());
//
//        }
//
//        final long stop = System.currentTimeMillis();
//
//        puts(count, stop - start);

        final HttpClient httpClient = httpClientBuilder()
                .setPort(6060).setPoolSize(500).setRequestBatchSize(100).setPipeline(true).setKeepAlive(true)
                .build().start();

        Sys.sleep(1_000);


        final long start = System.currentTimeMillis();


        final HttpRequest httpRequest = httpRequestBuilder().setUri("/services/myservice/ping").setTextReceiver(new HttpTextReceiver() {
            @Override
            public void response(int code, String mimeType, String body) {
                count++;
            }
        }).build();

        for (int index = 0; index < 500_005; index++) {
            httpClient.sendHttpRequest(httpRequest);
        }


        while (count < 490_000) {
            Sys.sleep(100);
            if (count > 100) {
                puts(count);
            }
            if (count > 500) {
                puts(count);
            }

            if (count > 750) {
                puts(count);
            }

            if (count > 950) {
                puts(count);
            }
            if (count > 2000) {
                puts(count);
            }

            if (count > 10_000) {
                puts(count);
            }

            if (count > 15_000) {
                puts(count);
            }


            if (count > 100_000) {
                puts(count);
            }
        }


        final long stop = System.currentTimeMillis();

        puts(count, stop - start);
    }
}

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

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import org.boon.Boon;


/**
 * engine1:wrk rhightower$ ./wrk -c 500 -d 10s http://localhost:9090/1 -H "X_RDIO_USER_ID: RICK"  --timeout 100000s -t 8
 */
public class WebServerSimpleApplication {


    public static void main(final String... args) throws Exception {


        final HttpServer httpServer = new HttpServerBuilder().setPort(9090)
                .setFlushInterval(500)
                .setPollTime(20)
                .setRequestBatchSize(40)
                .setManageQueues(true)
                .build();

        httpServer.setHttpRequestConsumer(request -> {

            request.getReceiver().response(200, "application/json", "\"ok\"");
        });
        httpServer.start();

        Boon.gets();
    }

}


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

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.boon.core.Sys;

import static io.advantageous.boon.Boon.puts;


/**
 * Created by Richard on 11/12/14.
 */
public class HttpPerfServerTest {


    public static void main(String... args) {


        puts("SERVER Arguments", args);


        String argHost = "localhost";
        int argPort = 8080;
        int argBatchSize = 10;
        int argFlushRate = 100;
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

            argFlushRate = Integer.parseInt(args[3]);
        }

        if (args.length > 4) {

            argPollTime = Integer.parseInt(args[4]);
        }


        final String host = argHost;
        final int port = argPort;
        final int batchSize = argBatchSize;
        final int flushRate = argFlushRate;
        final int pollTime = argPollTime;


        puts("Params for SERVER: host", host, "port", port);

        puts("\nParams for client batchSize", batchSize, "flushRate", flushRate);

        puts("Params for client pollTime", pollTime);


        final HttpServer server = new HttpServerBuilder()
                .setPort(port)
                .setHost(host)
                .setPollTime(pollTime)
                .setManageQueues(true)
                .setRequestBatchSize(batchSize)
                .setFlushInterval(flushRate)
                .build();


        server.setHttpRequestConsumer(request -> {

            if (request.getUri().equals("/perf/")) {
                request.getReceiver().response(200, "application/json", "\"ok\"");
            }
        });
        server.start();


        Sys.sleep(1000);


        Sys.sleep(1_000 * 1_000);
    }
}

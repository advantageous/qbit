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

package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.boon.core.Sys;

import static io.advantageous.boon.Boon.puts;


/**
 * @author rhightower on 2/14/15.
 */
public class ServiceServerClient {

    public static void main(String... args) throws Exception {

        Client client = new ClientBuilder().setPort(9998).setPollTime(10)
                .setAutoFlush(true).setFlushInterval(50).setRequestBatchSize(50)
                .setProtocolBatchSize(50).build();

        PingService pingService = client.createProxy(PingService.class, "ping");

        client.start();

        pingService.ping(s -> puts("FROM SERVER", s));


        ServiceProxyUtils.flushServiceProxy(pingService);

        Sys.sleep(1000000);
    }

    public interface PingService {
        void ping(Callback<String> callback);
    }
}

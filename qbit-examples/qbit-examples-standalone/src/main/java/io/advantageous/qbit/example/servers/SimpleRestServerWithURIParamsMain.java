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

import io.advantageous.qbit.annotation.PathVariable;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.system.QBitSystemManager;
import org.boon.core.Sys;

import static io.advantageous.qbit.client.ClientBuilder.clientBuilder;
import static io.advantageous.qbit.http.client.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

/**
 * @author rhightower
 *         on 2/17/15.
 */
public class SimpleRestServerWithURIParamsMain {


    public static void main(String... args) throws Exception {

        QBitSystemManager systemManager = new QBitSystemManager();

       /* Start Service server. */
        final ServiceServer server = serviceServerBuilder()
                .setSystemManager(systemManager)
                .setPort(7000).build();

        server.initServices(new AdderService());
        server.start();

       /* Start QBit client for WebSocket calls. */
        final Client client = clientBuilder().setPort(7000).setRequestBatchSize(1).build();


       /* Create a proxy to the service. */
        final AdderServiceClientInterface adderService =
                client.createProxy(AdderServiceClientInterface.class, "adder-service");

        client.start();



       /* Call the service */
        adderService.add(System.out::println, 1, 2);



        HttpClient httpClient = httpClientBuilder()
                .setHost("localhost")
                .setPort(7000).build();

        httpClient.start();
        String results = httpClient
                .get("/services/adder-service/add/2/2")
                .body();
        System.out.println(results);


//        HttpResponse httpResponse = httpClient.get("/services/adder-service/foo/randomcrap/2");
//        //System.out.println(httpResponse);
//
//
//        httpResponse = httpClient.get("/services/adder-bs/foo/randomcrap/2");
//        //System.out.println(httpResponse);


        Sys.sleep(100);

        client.stop();
        httpClient.stop();
        systemManager.shutDown();


    }

    interface AdderServiceClientInterface {

        void add(Callback<Integer> callback, int a, int b);
    }

    @RequestMapping("/adder-service")
    public static class AdderService {


        @RequestMapping("/add/{0}/{1}")
        public int add(@PathVariable int a, @PathVariable int b) {

            return a + b;
        }
    }


}

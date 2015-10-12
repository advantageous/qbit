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

package io.advantageous.qbit.example.hello;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.system.QBitSystemManager;

import static io.advantageous.qbit.http.server.HttpServerBuilder.httpServerBuilder;
import static io.advantageous.qbit.server.EndpointServerBuilder.endpointServerBuilder;

/**
 * created by rhightower on 2/9/15.
 */
@SuppressWarnings("ALL")
public class HelloWorldRestServer {


    public static final String HTML_HELLO_PAGE = "/ui/helloWorld.html";


    public static void main(String... args) {

        /* Create the system manager to manage the shutdown. */
        final QBitSystemManager systemManager = new QBitSystemManager();

        final HttpServer httpServer = httpServerBuilder()
                .setPort(9999).build();

        /* Register the Predicate using a Java 8 lambda expression. */
        httpServer.setShouldContinueHttpRequest(httpRequest -> {
            /* If not the page uri we want to then just continue by returning true. */
            if (!httpRequest.getUri().equals(HTML_HELLO_PAGE)) {
                return true;
            }
            /* Send the HTML file out to the browser. */
            httpRequest.getReceiver().response(200, "text/html", "some content");
            return false;
        });


        final EndpointServerBuilder endpointServerBuilder = endpointServerBuilder();
        endpointServerBuilder.getRequestQueueBuilder();

        /* Start the service. */
        final ServiceEndpointServer serviceEndpointServer = endpointServerBuilder
                    .setSystemManager(systemManager)
                    .setHttpServer(httpServer)
                    .build().initServices(new HelloService())
                    .startServer();

        /* Wait for the service to shutdown. */
        systemManager.waitForShutdown();

    }


}

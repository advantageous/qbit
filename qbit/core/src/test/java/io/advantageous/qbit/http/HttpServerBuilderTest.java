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

package io.advantageous.qbit.http;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.http.server.RequestContinuePredicate;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.system.QBitSystemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.advantageous.boon.core.Exceptions.die;

public class HttpServerBuilderTest {


    HttpServerBuilder objectUnderTest;

    Consumer<WebSocketMessage> webSocketMessageConsumer;
    Consumer<HttpRequest> httpRequestConsumer;
    boolean ok;

    @Before
    public void setUp() throws Exception {

        objectUnderTest = new HttpServerBuilder();

        webSocketMessageConsumer = webSocketMessage -> {

        };

        httpRequestConsumer = request -> {

        };

        FactorySPI.setFactory(new Factory() {


            public HttpServer createHttpServer(HttpServerOptions options,
                                               String endpointName,
                                               QBitSystemManager systemManager,
                                               ServiceDiscovery serviceDiscovery,
                                               HealthServiceAsync healthServiceAsync,
                                               final int serviceDiscoveryTtl,
                                               final TimeUnit serviceDiscoveryTtlTimeUnit,
                                               final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                               final HttpResponseCreator httpResponseCreator, RequestContinuePredicate requestBodyContinuePredicate) {

                return FactorySPI.getHttpServerFactory().create(options, endpointName, systemManager, serviceDiscovery,
                        healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit, decorators, httpResponseCreator, requestBodyContinuePredicate);
            }
        });

        FactorySPI.setHttpServerFactory((options, name, systemManager, serviceDiscovery, healthServiceAsync, a, b, c, d, z)
                -> new SimpleHttpServer());
        Sys.sleep(100);

    }

    @After
    public void tearDown() {
        FactorySPI.setFactory(null);

    }

    @Test
    public void test() throws Exception {


        ok = objectUnderTest.setHost("host").getHost().equals("host") || die();
        ok = objectUnderTest.setHost("localhost").getHost().equals("localhost") || die();

        ok = objectUnderTest.setPipeline(true).isPipeline() || die();
        ok = !objectUnderTest.setPipeline(false).isPipeline() || die();

        ok = objectUnderTest.setPort(9090).getPort() == 9090 || die();
        ok = objectUnderTest.setPort(8080).getPort() == 8080 || die();

        ok = objectUnderTest.setFlushInterval(909).getFlushInterval() == 909 || die();
        ok = objectUnderTest.setFlushInterval(808).getFlushInterval() == 808 || die();


        objectUnderTest.build();

    }
}
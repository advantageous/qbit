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

package io.advantageous.qbit.spi;

import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.RequestContinuePredicate;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.system.QBitSystemManager;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Creates an HttpServer.
 */
public interface HttpServerFactory {

    HttpServer create(
            HttpServerOptions options,
            String endPointName,
            QBitSystemManager systemManager,
            ServiceDiscovery serviceDiscovery,
            HealthServiceAsync healthServiceAsync,
            final int serviceDiscoveryTtl,
            final TimeUnit serviceDiscoveryTtlTimeUnit,
            final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
            final HttpResponseCreator httpResponseCreator, RequestContinuePredicate requestBodyContinuePredicate);
}

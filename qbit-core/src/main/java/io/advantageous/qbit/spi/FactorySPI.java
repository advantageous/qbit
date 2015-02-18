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

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.events.EventManagerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * SPI interface to register default implementations of built-in factories and services.
 * Created by Richard on 9/26/14.
 *
 * @author rhightower
 */
public class FactorySPI {

    private static final AtomicReference<Factory> ref = new AtomicReference<>();

    private static final AtomicReference<HttpServerFactory> httpServerFactoryRef = new AtomicReference<>();

    private static final AtomicReference<ClientFactory> clientFactoryRef = new AtomicReference<>();
    private static final AtomicReference<HttpClientFactory> httpClientFactoryRef = new AtomicReference<>();


    private static final AtomicReference<EventManagerFactory> eventManagerFactoryRef = new AtomicReference<>();

    public static EventManagerFactory getEventManagerFactory() {
        return eventManagerFactoryRef.get();
    }

    public static void setEventManagerFactory(EventManagerFactory factory) {
        eventManagerFactoryRef.set(factory);
    }

    public static Factory getFactory() {
        return ref.get();
    }


    public static void setFactory(Factory factory) {
        ref.set(factory);
    }


    public static HttpServerFactory getHttpServerFactory() {
        return httpServerFactoryRef.get();
    }


    public static void setHttpServerFactory(HttpServerFactory factory) {
        httpServerFactoryRef.set(factory);
    }

    public static HttpClientFactory getHttpClientFactory() {
        return httpClientFactoryRef.get();
    }

    public static void setHttpClientFactory(HttpClientFactory factory) {
        httpClientFactoryRef.set(factory);
    }

    public static ClientFactory getClientFactory() {
        return clientFactoryRef.get();
    }

    public static void setClientFactory(ClientFactory clientFactory) {
        clientFactoryRef.set(clientFactory);

    }
}

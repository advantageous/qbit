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
 *
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
package io.advantageous.consul;

import io.advantageous.consul.domain.ConsulException;
import io.advantageous.consul.endpoints.*;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Consul HTTP API proxy
 * <p>
 * Note: Used Orbitz consul client and others as a guides.
 *
 * @author Richard Hightower
 */
public class Consul {

    private final HttpClient httpClient;
    private final AgentEndpoint agent;
    private final HealthEndpoint health;
    private final KeyValueStoreEndpoint keyValueStore;
    private final CatalogEndpoint catalog;
    private final StatusEndpoint status;
    private boolean started = false;

    /**
     * Private constructor.
     *
     * @param url The full URL of a running Consul instance.
     */
    private Consul(String url) {

        URI uri = URI.create(url + "/v1");

        final HttpClientBuilder httpClientBuilder = HttpClientBuilder
                .httpClientBuilder().setAutoFlush(false).setTimeOutInMilliseconds(120_000).setPoolSize(10).setHost(uri.getHost()).setPort(uri.getPort());
        httpClient = httpClientBuilder.build();

        final String rootPath = uri.getPath();

        this.agent = new AgentEndpoint(httpClient, rootPath + "/agent");
        this.health = new HealthEndpoint(httpClient, rootPath + "/health");
        this.keyValueStore = new KeyValueStoreEndpoint(httpClient, rootPath + "/kv");
        this.catalog = new CatalogEndpoint(httpClient, rootPath + "/catalog");
        this.status = new StatusEndpoint(httpClient, rootPath + "/status");


    }

    /**
     * Creates a new client given a host and a port.
     *
     * @param host The Consul API hostname or IP.
     * @param port The Consul port.
     * @return A new client.
     */
    public static Consul consul(final String host, final int port) {
        try {
            return new Consul(new URL("http", host, port, "").toString());
        } catch (MalformedURLException e) {
            throw new ConsulException("Bad Consul URL", e);
        }
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @return A new client.
     */
    public static Consul consul() {
        return consul("localhost", 8500);
    }

    public void start() {

        if (!started) {
            started = true;
            httpClient.startClient();
            agent.pingAgent();
        }
    }

    public void stop() {

        started = false;
        httpClient.stop();
    }

    /**
     * Catalog HTTP endpoint.
     * <p>
     * /v1/catalog
     *
     * @return The Catalog HTTP endpoint.
     */
    public CatalogEndpoint catalog() {
        return catalog;
    }

    /**
     * Health HTTP endpoint.
     * <p>
     * /v1/health
     *
     * @return The Health HTTP endpoint.
     */
    public HealthEndpoint health() {
        return health;
    }


    /**
     * Status HTTP endpoint.
     * <p>
     * /v1/status
     *
     * @return The Status HTTP endpoint.
     */
    public StatusEndpoint status() {
        return status;
    }

    /**
     * Agent HTTP endpoint.
     * <p>
     * /v1/agent
     *
     * @return The Agent HTTP endpoint.
     */
    public AgentEndpoint agent() {
        return agent;
    }


    /**
     * Key/Value HTTP endpoint.
     * <p>
     * /v1/kv
     *
     * @return The Key/Value HTTP endpoint.
     */
    public KeyValueStoreEndpoint keyValueStore() {
        return keyValueStore;
    }

}

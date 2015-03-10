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
package io.advantageous.consul;

import io.advantageous.consul.endpoints.CatalogEndpoint;
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.CatalogNode;
import io.advantageous.consul.domain.CatalogService;
import io.advantageous.consul.domain.Node;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static io.advantageous.consul.domain.option.RequestOptionsBuilder.requestOptionsBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class CatalogEndpointTest {


    @Test
    public void getServices() throws Exception {
        Consul client = Consul.consul();
        CatalogEndpoint catalogClient = client.catalog();
        ConsulResponse<Map<String, List<String>>> services = catalogClient.getServices();

        assertTrue(services.getResponse().containsKey("consul"));
    }


    @Test
    public void nodes() throws UnknownHostException {
        Consul client = Consul.consul();
        CatalogEndpoint catalogClient = client.catalog();

        assertFalse(catalogClient.getNodes().getResponse().isEmpty());
    }


    @Test
    public void getSingleService() throws Exception {
        Consul client = Consul.consul();
        CatalogEndpoint catalogClient = client.catalog();
        ConsulResponse<List<CatalogService>> services = catalogClient.getService("consul");

        assertEquals("consul", services.getResponse().iterator().next().getServiceName());
    }

    @Test
    public void getSingleNode() throws Exception {
        Consul client = Consul.consul();
        CatalogEndpoint catalogClient = client.catalog();
        ConsulResponse<CatalogNode> node = catalogClient.getNode(catalogClient.getNodes()
                .getResponse().iterator().next().getNode());

        assertNotNull(node);
    }


    @Test
    public void nodesByDataCenter() throws UnknownHostException {
        Consul client = Consul.consul();
        CatalogEndpoint catalogClient = client.catalog();

        assertFalse(catalogClient.getNodes("dc1", null).getResponse().isEmpty());
    }

    @Test
    public void blockingNodesByDataCenter() throws UnknownHostException {
        Consul client = Consul.consul();
        CatalogEndpoint catalogClient = client.catalog();

        long start = System.currentTimeMillis();
        ConsulResponse<List<Node>> response = catalogClient.getNodes("dc1", null,
                requestOptionsBuilder().blockSeconds(2, Integer.MAX_VALUE).build());
        long time = System.currentTimeMillis() - start;

        assertTrue(time >= 2000);
        assertFalse(response.getResponse().isEmpty());
    }

    @Test
    public void datacenters() throws UnknownHostException {
        Consul client = Consul.consul();
        CatalogEndpoint catalogClient = client.catalog();
        List<String> datacenters = catalogClient.getDatacenters();

        assertEquals(1, datacenters.size());
        assertEquals("dc1", datacenters.iterator().next());
    }


}

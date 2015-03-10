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

import io.advantageous.boon.core.Sys;
import io.advantageous.consul.domain.*;
import io.advantageous.consul.domain.option.RequestOptionsBuilder;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class HealthEndpointTests {

    @Test
    public void fetchNode() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agent().registerService(80, 20L, serviceName, serviceId);
        client.agent().pass(serviceId);

        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client.health().getAllNodes(serviceName);
        assertHealth(serviceId, found, response);
    }


    @Test
    public void fetchNodeUsingBlock() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agent().registerService(8080, 20L, serviceName, serviceId);
        client.agent().pass(serviceId);

        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client.health().getAllNodes(serviceName,
                "dc1", null,
                RequestOptionsBuilder.requestOptionsBuilder().blockSeconds(2, 0).build());
        assertHealth(serviceId, found, response);
    }

    @Test
    public void fetchNodeByState() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agent().registerService(8080, 20L, serviceName, serviceId);
        client.agent().warn(serviceId);
        Sys.sleep(100);

        boolean found = false;
        ConsulResponse<List<HealthCheck>> response = client.health().getChecksByState(Status.WARN);

        for(HealthCheck healthCheck : response.getResponse()) {
            if(healthCheck.getServiceId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    private void assertHealth(String serviceId, boolean found, ConsulResponse<List<ServiceHealth>> response) {
        List<ServiceHealth> nodes = response.getResponse();

        assertEquals(1, nodes.size());

        for(ServiceHealth health : nodes) {
            if(health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }


    @Test
    public void fetchPassingNodes() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agent().registerService(80, 20L, serviceName, serviceId);
        client.agent().pass(serviceId);

        Consul client2 = Consul.consul();
        String serviceId2 = UUID.randomUUID().toString();

        client2.agent().registerService(80, 20L, serviceName, serviceId2);
        client2.agent().fail(serviceId2);

        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client2.health().getHealthyServices(serviceName);
        assertHealth(serviceId, found, response);
    }


    @Test
    public void fetchNodeByDatacenter() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agent().registerService(8080, 20L, serviceName, serviceId);
        client.agent().pass(serviceId);

        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client.health().getAllNodes(serviceName,
                "dc1", null);
        assertHealth(serviceId, found, response);
    }

}

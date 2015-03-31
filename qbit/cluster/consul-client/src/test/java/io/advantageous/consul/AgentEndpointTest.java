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
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class AgentEndpointTest {


    @Test
    public void deregister()  throws Exception{
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agent().registerService(8080, 10000L, serviceName, serviceId);
        client.agent().deregister(serviceId);
        Thread.sleep(1000L);
        boolean found = false;

        for (ServiceHealth health : client.health().getAllNodes(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertFalse(found);
    }

    @Test
    public void checks() {
        Consul client = Consul.consul();
        String id = UUID.randomUUID().toString();
        client.agent().registerService(8080, 20L, UUID.randomUUID().toString(), id);

        boolean found = false;

        for (Map.Entry<String, HealthCheck> check : client.agent().getChecks().entrySet()) {
            if (check.getValue().getCheckId().equals("service:" + id)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void services() {
        Consul client = Consul.consul();
        String id = UUID.randomUUID().toString();
        client.agent().registerService(8080, 20L, UUID.randomUUID().toString(), id);

        boolean found = false;

        for (Map.Entry<String, Service> service : client.agent().getServices().entrySet()) {
            if (service.getValue().getId().equals(id)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void warning() throws Exception {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();
        client.agent().registerService(80, 20L, serviceName, serviceId);
        client.agent().warn(serviceId, note);
        Sys.sleep(100);
        verifyState("warning", client, serviceId, serviceName);
    }

    @Test
    public void critical() throws Exception {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();

        client.agent().registerService(80, 20L, serviceName, serviceId);
        client.agent().fail(serviceId, note);

        verifyState("critical", client, serviceId, serviceName);
    }

    private void verifyState(String state, Consul client, String serviceId,
                             String serviceName) throws Exception {
        List<ServiceHealth> nodes = client.health().getAllNodes(serviceName).getResponse();
        boolean found = false;

        for (ServiceHealth health : nodes) {
            if (health.getService().getId().equals(serviceId)) {
                List<HealthCheck> checks = health.getChecks();

                found = true;

                assertEquals(serviceId, health.getService().getId());
                assertEquals(state, checks.get(0).getStatus());
            }
        }

        assertTrue(found);
    }



    @Test
    public void retrieveAgentInformation() throws UnknownHostException {
        Consul client = Consul.consul();
        AgentInfo agentInfo = client.agent().getAgentInfo();

        assertNotNull(agentInfo);
        assertEquals("127.0.0.1", agentInfo.getConfig().getClientAddr());
    }

    @Test
    public void register() throws UnknownHostException {
        Consul client = Consul.consul();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agent().registerService(80, 20000L, serviceName, serviceId);

        boolean found = false;

        for (ServiceHealth health : client.health().getAllNodes(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }
}

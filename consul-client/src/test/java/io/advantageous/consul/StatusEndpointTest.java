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

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test heavily influenced and inspired by StateClientTest in Orbitz Consul client.
 */
public class StatusEndpointTest {

    public static final String IP_PORT_DELIM = ":";
    public static final String CONSUL_PORT = "8300";
    private static Set<InetAddress> ips = new HashSet<>();


    @Test
    public void getLeader() throws UnknownHostException {
        String ipAndPort = Consul.consul().status().getLeader();
        assertLocalIpAndCorrectPort(ipAndPort);

    }

    @Test
    public void getPeers() throws UnknownHostException {
        List<String> peers = Consul.consul().status().getPeers();
        for (String ipAndPort : peers) {
            assertLocalIpAndCorrectPort(ipAndPort);
        }
    }

    @BeforeClass
    public static void getIps() throws RuntimeException {
        try {
            InetAddress[] externalIps = InetAddress.getAllByName(InetAddress.getLocalHost().getCanonicalHostName());
            ips.addAll(Arrays.asList(externalIps));
        } catch (UnknownHostException ex) {
            Logger.getLogger(StatusEndpointTest.class.getName()).log(Level.WARNING, "Could not determine fully qualified host name. Continuing.", ex);
        }
        Enumeration<NetworkInterface> netInts;
        try {
            netInts = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netInt : Collections.list(netInts)) {
                for (InetAddress inetAddress : Collections.list(netInt.getInetAddresses())) {
                    ips.add(inetAddress);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(StatusEndpointTest.class.getName()).log(Level.WARNING, "Could not access local network adapters. Continuing", ex);
        }
        if (ips.isEmpty()) {
            throw new RuntimeException("Unable to discover any local IP addresses");
        }
    }

    public boolean isLocalIp(String ipAddress) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(ipAddress);
        return ips.contains(ip);
    }


    public String getIp(String ipAndPort) {
        return ipAndPort.substring(0, ipAndPort.indexOf(IP_PORT_DELIM));
    }

    public String getPort(String ipAndPort) {
        return ipAndPort.substring(ipAndPort.indexOf(IP_PORT_DELIM) + 1);
    }

    public void assertLocalIpAndCorrectPort(String ipAndPort) throws UnknownHostException {
        String ip = getIp(ipAndPort);
        String port = getPort(ipAndPort);
        assertTrue(isLocalIp(ip));
        assertEquals(CONSUL_PORT, port);
    }

}

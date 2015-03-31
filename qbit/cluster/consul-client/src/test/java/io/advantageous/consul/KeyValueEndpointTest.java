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

import io.advantageous.consul.endpoints.KeyValueStoreEndpoint;
import io.advantageous.consul.domain.KeyValue;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.UUID;

import static io.advantageous.consul.endpoints.RequestUtils.decodeBase64;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class KeyValueEndpointTest {


    @Test
    public void putAndReceiveWithFlags() throws UnknownHostException {
        Consul client = Consul.consul();
        KeyValueStoreEndpoint keyValueStore = client.keyValueStore();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        long flags = Long.MAX_VALUE;

        assertTrue(keyValueStore.putValue(key, value, flags));
        KeyValue received = keyValueStore.getValue(key).get();
        assertEquals(value, decodeBase64(received.getValue()));
        assertEquals(flags, received.getFlags());
    }

    @Test
    public void putAndReceiveStrings() throws UnknownHostException {
        Consul client = Consul.consul();
        KeyValueStoreEndpoint keyValueStore = client.keyValueStore();
        String key = UUID.randomUUID().toString();
        String key2 = key + "/" + UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();

        assertTrue(keyValueStore.putValue(key, value));
        assertTrue(keyValueStore.putValue(key2, value2));
        assertEquals(new HashSet<String>() {
            {
                add(value);
                add(value2);
            }
        }, new HashSet<>(keyValueStore.getValuesAsString(key)));
    }

    @Test
    public void delete() throws Exception {
        Consul client = Consul.consul();
        KeyValueStoreEndpoint keyValueStore = client.keyValueStore();
        String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();

        keyValueStore.putValue(key, value);

        assertTrue(keyValueStore.getValueAsString(key).isPresent());

        keyValueStore.deleteKey(key);

        assertFalse(keyValueStore.getValueAsString(key).isPresent());
    }

    @Test
    public void putAndReceiveString() throws UnknownHostException {
        Consul client = Consul.consul();
        KeyValueStoreEndpoint keyValueStore = client.keyValueStore();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueStore.putValue(key, value));
        assertEquals(value, keyValueStore.getValueAsString(key).get());
    }

    @Test
    public void putAndReceiveValue() throws UnknownHostException {
        Consul client = Consul.consul();
        KeyValueStoreEndpoint keyValueStore = client.keyValueStore();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueStore.putValue(key, value));
        KeyValue received = keyValueStore.getValue(key).get();
        assertEquals(value, decodeBase64(received.getValue()));
        assertEquals(0L, received.getFlags());
    }

}

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

package io.advantageous.qbit.http.jetty.impl.server;

import io.advantageous.qbit.util.MultiMap;

import java.util.*;

/**
 * Created by rhightower on 2/16/15.
 */
public class JettyMultiMapAdapter implements MultiMap<String, String> {


    private final Map<String, List<String>> innerMap;

    public JettyMultiMapAdapter(Map<String, List<String>> innerMap) {
        this.innerMap = innerMap;
    }

    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {
        return (Iterator<Entry<String, Collection<String>>>) innerMap.entrySet();
    }

    @Override
    public String getFirst(String key) {
        final List<String> strings = innerMap.get(key);
        if (strings.size() > 0) {
            return strings.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Iterable<String> getAll(String key) {
        return innerMap.get(key);
    }

    @Override
    public Iterable<String> keySetMulti() {
        return innerMap.keySet();
    }

    @Override
    public String getSingleObject(String name) {
        return getFirst(name);
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return innerMap.containsKey(key);
    }

    @Override
    public String get(Object key) {
        return getFirst(key.toString());
    }

    @Override
    public Set<String> keySet() {
        return innerMap.keySet();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {

        Map<String, String> map = new HashMap<>(this.size());

        for (String key : keySet()) {
            map.put(key, this.getFirst(key));
        }
        return map.entrySet();

    }
}

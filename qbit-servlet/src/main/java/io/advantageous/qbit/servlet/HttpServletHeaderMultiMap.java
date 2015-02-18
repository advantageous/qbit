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
package io.advantageous.qbit.servlet;

import io.advantageous.qbit.util.MultiMap;
import org.boon.Lists;
import org.boon.Sets;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author rhightower on 2/12/15.
 */
public class HttpServletHeaderMultiMap implements MultiMap<String, String> {
    private final HttpServletRequest request;

    public HttpServletHeaderMultiMap(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {

        final Enumeration<String> headerNames = request.getHeaderNames();

        return new Iterator<Entry<String, Collection<String>>>() {
            @Override
            public boolean hasNext() {
                return headerNames.hasMoreElements();
            }

            @Override
            public Entry<String, Collection<String>> next() {
                String currentName = headerNames.nextElement();
                return new Entry<String, Collection<String>>() {
                    @Override
                    public String getKey() {
                        return currentName;
                    }

                    @Override
                    public Collection<String> getValue() {
                        return Lists.list(request.getHeaders(currentName));
                    }

                    @Override
                    public Collection<String> setValue(Collection<String> value) {
                        throw new UnsupportedOperationException("Unsupported");
                    }
                };
            }
        };
    }


    @Override
    public String getFirst(final String key) {
        return request.getHeader(key);
    }

    @Override
    public Iterable<String> getAll(final String key) {
        return Lists.list(request.getHeader(key));
    }


    @Override
    public Iterable<String> keySetMulti() {
        return Lists.list(request.getHeaderNames());
    }


    @Override
    public String getSingleObject(String name) {
        return getFirst(name);
    }

    @Override
    public int size() {
        return Lists.list(request.getHeaderNames()).size();
    }

    @Override
    public boolean isEmpty() {
        return request.getHeaderNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        return request.getHeader(key.toString()) != null;
    }

    @Override
    public String get(Object key) {
        return getFirst(key.toString());
    }


    @Override
    public Set<String> keySet() {
        return Sets.set(request.getHeaderNames());
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

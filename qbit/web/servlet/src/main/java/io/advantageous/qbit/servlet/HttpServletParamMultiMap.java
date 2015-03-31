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

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author rhightower on 2/12/15.
 */
public class HttpServletParamMultiMap implements MultiMap<String, String> {
    private final HttpServletRequest request;

    private final Map<String, String[]> parameterMap;

    public HttpServletParamMultiMap(HttpServletRequest request) {
        this.request = request;
        parameterMap = request.getParameterMap();
    }


    @Override
    public String getFirst(String key) {
        return request.getParameter(key);
    }

    @Override
    public Iterable<String> getAll(String key) {
        return new ArrayList<>(Arrays.asList(request.getParameterValues(key)));
    }

    @Override
    public Iterable<String> keySetMulti() {
        final Enumeration<String> enumeration = request.getParameterNames();
        final List<String> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    @Override
    public String getSingleObject(String name) {
        return getFirst(name);
    }

    @Override
    public int size() {
        return parameterMap.size();
    }

    @Override
    public boolean isEmpty() {
        return Arrays.asList(request.getParameterNames()).size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return parameterMap.containsKey(key);
    }

    @Override
    public String get(Object key) {
        return getFirst(key.toString());
    }

    @Override
    public Set<String> keySet() {
        return parameterMap.keySet();
    }

    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {

        final Iterator<Entry<String, String[]>> iterator = parameterMap.entrySet().iterator();

        return new Iterator<Entry<String, Collection<String>>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<String, Collection<String>> next() {
                final Entry<String, String[]> entry = iterator.next();
                return new Entry<String, Collection<String>>() {
                    @Override
                    public String getKey() {
                        return entry.getKey();
                    }

                    @Override
                    public Collection<String> getValue() {
                        return Arrays.asList(entry.getValue());
                    }

                    @Override
                    public Collection<String> setValue(Collection<String> value) {
                        return null;
                    }
                };
            }
        };
    }


    @Override
    public Set<Entry<String, String>> entrySet() {
        final Map<String, String> map = new HashMap<>(this.size());
        for (final String key : keySet()) {
            map.put(key, this.getFirst(key));
        }
        return map.entrySet();
    }
}

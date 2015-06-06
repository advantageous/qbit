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
package io.advantageous.qbit.meta;


import io.advantageous.boon.core.Lists;

import java.util.Collections;
import java.util.List;

public class ServiceMeta {

    private final String name;
    private final List<String> requestPaths;
    private final List<ServiceMethodMeta> methods;

    public ServiceMeta(final String name, final List<String> requestPaths,
                       final List<ServiceMethodMeta> methods) {
        this.name = name;
        this.requestPaths = Collections.unmodifiableList(requestPaths);
        this.methods = Collections.unmodifiableList(methods);
    }

    public static ServiceMeta serviceMeta(final String name, final String address,
                                          final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, Lists.list(address), Lists.list(serviceMethods));
    }

    public static ServiceMeta service(final String name, final String address,
                                      final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, Lists.list(address), Lists.list(serviceMethods));
    }

    public static ServiceMeta serviceMeta(final String name, final List<String> requestPaths,
                                          final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, requestPaths, Lists.list(serviceMethods));
    }

    public static ServiceMeta service(final String name, final List<String> requestPaths,
                                      final ServiceMethodMeta... serviceMethods) {
        return new ServiceMeta(name, requestPaths, Lists.list(serviceMethods));
    }

    public String getName() {
        return name;
    }

    public List<String> getRequestPaths() {
        return requestPaths;
    }

    public List<ServiceMethodMeta> getMethods() {
        return methods;
    }
}

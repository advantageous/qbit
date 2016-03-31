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


/**
 * Holds meta-data about a single request of a service method.
 */
public class RequestMetaData {


    private final RequestMeta request;
    private final ServiceMethodMeta method;
    private final ServiceMeta service;
    private final String path;
    private final ContextMeta context;

    public RequestMetaData(final String path,
                           final ContextMeta context,
                           final RequestMeta request,
                           final ServiceMethodMeta method,
                           final ServiceMeta service) {
        this.request = request;
        this.method = method;
        this.service = service;
        this.path = path;
        this.context = context;
    }


    public RequestMeta getRequest() {
        return request;
    }

    public ServiceMethodMeta getMethod() {
        return method;
    }

    public ServiceMeta getService() {
        return service;
    }

    public String getPath() {
        return path;
    }

    public ContextMeta getContext() {
        return context;
    }
}

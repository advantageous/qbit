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
package io.advantageous.qbit.meta.provider;


import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.StringScanner;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds a bunch of meta data about a service bundle.
 */
public class StandardMetaDataProvider implements MetaDataProvider {


    private final Map<String, RequestMetaData> metaDataMap = new ConcurrentHashMap<>(100);
    private final NavigableMap<String, NavigableMap<Integer, RequestMetaData>> treeMap = new TreeMap<>();
    private final RequestMethod httpRequestMethod;

    private final Logger logger = LoggerFactory.getLogger(StandardMetaDataProvider.class);
    private final boolean debug = logger.isDebugEnabled();
    private final String rootURI;


    public StandardMetaDataProvider(final ContextMeta context, final RequestMethod method) {
        this.httpRequestMethod = method;
        this.rootURI = context.getRootURI();
        context.getServices().forEach(service -> addService(context, service));
    }

    private void addService(final ContextMeta context,
                            final ServiceMeta service) {

        service.getMethods().forEach(method -> addMethod(context, service, method));
    }

    private void addMethod(final ContextMeta context,
                           final ServiceMeta service,
                           final ServiceMethodMeta method) {

        method.getRequestEndpoints().forEach(requestMeta -> addRequest(context, service, method, requestMeta));
    }

    private void addRequest(final ContextMeta context,
                            final ServiceMeta service,
                            final ServiceMethodMeta method,
                            final RequestMeta requestMeta) {


        service.getRequestPaths().forEach(path -> addEndPoint(context, service, method, requestMeta, path));

    }

    private void addEndPoint(final ContextMeta context,
                             final ServiceMeta service,
                             final ServiceMethodMeta method,
                             final RequestMeta requestMeta,
                             final String servicePath) {

        if (!requestMeta.getRequestMethods().contains(httpRequestMethod)) {
            return;
        }

        if (requestMeta.getCallType() == CallType.ADDRESS) {
            final String requestPath = requestMeta.getRequestURI();


            final String path = Str.join('/', context.getRootURI(), servicePath, requestPath).replaceAll("//*", "/");

            addRequestEndPointUsingPath(context, service, method, requestMeta, path.toLowerCase(),
                    requestMeta.getRequestURI(), servicePath);

        } else if (requestMeta.getCallType() == CallType.ADDRESS_WITH_PATH_PARAMS) {

            final String requestPath =
                    StringScanner.substringBefore(requestMeta.getRequestURI(), "{");


            final String path = Str.join('/', context.getRootURI(), servicePath, requestPath).replaceAll("//*", "/");

            addRequestEndPointUsingPath(context, service, method, requestMeta, path.toLowerCase(), requestMeta.getRequestURI(),
                    servicePath);

        }

    }

    private void addRequestEndPointUsingPath(final ContextMeta context,
                                             final ServiceMeta service,
                                             final ServiceMethodMeta method,
                                             final RequestMeta requestMeta,
                                             final String path,
                                             final String requestURI,
                                             final String servicePath) {
        RequestMetaData metaData = new RequestMetaData(path, context, requestMeta, method, service);

        if (requestMeta.getCallType() == CallType.ADDRESS) {
            metaDataMap.put(path, metaData);
        } else {
            NavigableMap<Integer, RequestMetaData> map = treeMap.get(path);

            if (map == null) {
                map = new TreeMap<>();
                treeMap.put(path, map);
            }


            int count = Str.split(servicePath + requestURI, '/').length - 1;

            map.put(count, metaData);

        }
    }


    private RequestMetaData doGet(final String path) {

        RequestMetaData requestMetaData = metaDataMap.get(path);

        if (requestMetaData == null) {
            Map.Entry<String, NavigableMap<Integer, RequestMetaData>> uriParamNumMapEntry = treeMap.lowerEntry(path);

            if (uriParamNumMapEntry == null) {
                return null;
            }

            final String requestURI = Str.isEmpty(rootURI) ? path : StringScanner.substringAfter(path, rootURI);

            int count = Str.split(requestURI, '/').length - 1;
            NavigableMap<Integer, RequestMetaData> uriParamMap = uriParamNumMapEntry.getValue();

            requestMetaData = uriParamMap.get(count);

            if (requestMetaData != null && path.startsWith(requestMetaData.getPath())) {
                return requestMetaData;
            } else {
                return null;
            }
        } else {
            return requestMetaData;
        }
    }

    @Override
    public RequestMetaData get(final String path) {
        RequestMetaData requestMetaData = doGet(path);
        if (requestMetaData == null) {
            requestMetaData = doGet(path.toLowerCase());
        }

        if (debug && requestMetaData == null) {


            this.metaDataMap.keySet().forEach(mappedPath -> logger.debug("Path not found path {}, mapped path {}", path, mappedPath));
        }
        return requestMetaData;
    }

    public List<String> getPaths() {
        return new ArrayList<>(this.metaDataMap.keySet());
    }
}

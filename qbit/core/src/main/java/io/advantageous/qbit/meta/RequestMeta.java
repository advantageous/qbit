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
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.util.MultiMap;

import java.util.Collections;
import java.util.List;

/**
 * Holds metadata about a request to method call mapping.
 */
public class RequestMeta {


    private final CallType callType;
    private final String requestURI;
    private final List<ParameterMeta> parameters;
    private final List<RequestMethod> requestMethods;
    private final MultiMap<String, String> responseHeaders;
    private final boolean hasResponseHeaders;


    public RequestMeta(final CallType callType,
                       final List<RequestMethod> requestMethods,
                       final String requestURI,
                       final List<ParameterMeta> parameterMetaList,
                       final MultiMap<String, String> responseHeaders,
                       final boolean hasResponseHeaders) {
        this.callType = callType;
        this.requestURI = requestURI;
        this.parameters = parameterMetaList;
        this.requestMethods = requestMethods;
        this.responseHeaders = responseHeaders;
        this.hasResponseHeaders = hasResponseHeaders;
    }

    public static RequestMeta[] requests(final RequestMeta... requests) {
        return requests;
    }

    public static List<RequestMethod> requestMethods(final RequestMethod... methods) {
        return Lists.list(methods);
    }


    public static RequestMeta requestByAddress(
            final RequestMethod requestMethod,
            final String requestURI,
            final ParameterMeta... parameterMetaList) {

        if (!requestURI.contains("{")) {

            return new RequestMeta(CallType.ADDRESS,
                    Collections.singletonList(requestMethod),
                    requestURI, Lists.list(parameterMetaList), MultiMap.empty(), false);
        } else {
            return new RequestMeta(CallType.ADDRESS_WITH_PATH_PARAMS,
                    Collections.singletonList(requestMethod),
                    requestURI, Lists.list(parameterMetaList), MultiMap.empty(), false);
        }
    }

    public static RequestMeta getRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {

        return requestByAddress(RequestMethod.GET, requestURI, parameterMetaList);
    }

    public static RequestMeta postRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.POST, requestURI, parameterMetaList);
    }

    public static RequestMeta putRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.PUT, requestURI, parameterMetaList);
    }

    public static RequestMeta deleteRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.DELETE, requestURI, parameterMetaList);
    }

    public static RequestMeta headRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.HEAD, requestURI, parameterMetaList);
    }

    public CallType getCallType() {
        return callType;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public List<ParameterMeta> getParameters() {
        return parameters;
    }

    public List<RequestMethod> getRequestMethods() {
        return requestMethods;
    }

    public boolean hasResponseHeaders() {
        return hasResponseHeaders;
    }

    public MultiMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

}

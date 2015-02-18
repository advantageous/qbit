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

package io.advantageous.qbit.message;

import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.service.Protocol;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;

import java.util.Collections;
import java.util.List;

/**
 * Created by rhightower on 1/16/15.
 */
public class MethodCallBuilder {

    private static transient Timer timer = Timer.timer();

    private static volatile long idSequence;

    private long timestamp;

    private long id;
    private String name = "";
    private String address = "";
    private MultiMap<String, String> params = MultiMap.empty();
    private MultiMap<String, String> headers = MultiMap.empty();
    private Object body = Collections.emptyList();
    private String objectName;
    private String returnAddress;
    private Request<Object> originatingRequest;

    public static MethodCall<Object> transformed(final MethodCall<Object> methodCall,
                                                 final Object arg) {


        return new MethodCallBuilder()
                .setTimestamp(methodCall.timestamp())
                .setId(methodCall.id())
                .setName(methodCall.name())
                .setAddress(methodCall.address())
                .setParams(methodCall.params())
                .setHeaders(methodCall.headers())
                .setBody(arg)
                .setObjectName(methodCall.objectName())
                .setReturnAddress(methodCall.returnAddress())
                .setOriginatingRequest(methodCall.originatingRequest()).build();

    }

    public static MethodCall<Object> createMethodCallToBeEncodedAndSent(long id, String address,
                                                                        String returnAddress,
                                                                        String objectName,
                                                                        String methodName,
                                                                        long timestamp,
                                                                        Object body,
                                                                        MultiMap<String, String> params) {

        return new MethodCallBuilder().setId(id).setAddress(address).setReturnAddress(returnAddress).setObjectName(objectName).setName(methodName).setTimestamp(timestamp).setBody(body).setParams(params).build();
    }

    public static MethodCall<Object> methodWithArgs(String name, Object... args) {
        return new MethodCallBuilder().setName(name).setBody(args).build();

    }

    public static MethodCall<Object> method(String name, List body) {
        return new MethodCallBuilder().setName(name).setBody(body).build();
    }

    public static MethodCall<Object> method(String name, String body) {
        return new MethodCallBuilder().setName(name).setBody(body).build();
    }

    public static MethodCall<Object> method(String name, String address, String body) {
        return new MethodCallBuilder().setName(name).setBody(body).setAddress(address).build();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MethodCallBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getId() {
        return id;
    }

    public MethodCallBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public MethodCallBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public MethodCallBuilder setAddress(String address) {

        if (address == null) {
            this.address = "";
        } else {
            this.address = address;
        }
        return this;
    }

    public MultiMap<String, String> getParams() {
        return params;
    }

    public MethodCallBuilder setParams(MultiMap<String, String> params) {

        this.params = params;
        return this;
    }

    public MultiMap<String, String> getHeaders() {
        return headers;
    }

    public MethodCallBuilder setHeaders(MultiMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public MethodCallBuilder setBody(Object body) {
        this.body = body;
        return this;
    }

    public String getObjectName() {
        return objectName;
    }

    public MethodCallBuilder setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }

    public String getReturnAddress() {
        return returnAddress;
    }

    public MethodCallBuilder setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
        return this;
    }

    public Request<Object> getOriginatingRequest() {
        return originatingRequest;
    }

    public MethodCallBuilder setOriginatingRequest(Request<Object> originatingRequest) {
        this.originatingRequest = originatingRequest;
        return this;
    }

    public MethodCall<Object> build() {

        if (timestamp == 0L) {
            timestamp = timer.now();
        }

        if (id == 0L) {
            idSequence++;
            id = idSequence;
        }

        return new MethodCallImpl(timestamp, id, name, address, params, headers, body, objectName, returnAddress, originatingRequest);

    }

    public boolean hasParams() {
        return params != null && params.size() > 0;
    }

    public void overridesFromParams() {
        if (params != null && params.size() > 0) {
            final String _addr = params.get(Protocol.ADDRESS_KEY);
            final String _objectName = params.get(Protocol.OBJECT_NAME_KEY);
            final String _methodName = params.get(Protocol.METHOD_NAME_KEY);
            final String _returnAddress = params.get(Protocol.RETURN_ADDRESS_KEY);
            this.address = _addr == null || _addr.isEmpty() ? address : _addr;
            this.returnAddress = _returnAddress == null || _returnAddress.isEmpty() ? returnAddress : _returnAddress;
            this.name = _methodName == null || _methodName.isEmpty() ? name : _methodName;
            this.objectName = _objectName == null || _objectName.isEmpty() ? objectName : _objectName;
        }
    }

}

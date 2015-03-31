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

package io.advantageous.qbit.http.websocket;

import io.advantageous.qbit.http.websocket.impl.WebSocketImpl;
import io.advantageous.qbit.util.MultiMap;


/**
 * Created by rhightower on 2/14/15.
 */
public class WebSocketBuilder {

    private String remoteAddress;
    private String uri;
    private boolean open;
    private WebSocketSender webSocketSender;
    private boolean binary;
    private MultiMap<String, String> headers = MultiMap.empty();
    private MultiMap<String, String> params = MultiMap.empty();

    public static WebSocketBuilder webSocketBuilder() {
        return new WebSocketBuilder();
    }


    public MultiMap<String, String> getHeaders() {
        return headers;
    }

    public WebSocketBuilder setHeaders(MultiMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public MultiMap<String, String> getParams() {
        return params;
    }

    public WebSocketBuilder setParams(MultiMap<String, String> params) {
        this.params = params;
        return this;
    }


    public String getRemoteAddress() {
        return remoteAddress;
    }

    public WebSocketBuilder setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public WebSocketBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public WebSocketSender getWebSocketSender() {
        return webSocketSender;
    }

    public WebSocketBuilder setWebSocketSender(WebSocketSender webSocketSender) {
        this.webSocketSender = webSocketSender;
        return this;
    }

    public boolean isBinary() {
        return binary;
    }

    public WebSocketBuilder setBinary(boolean binary) {
        this.binary = binary;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public WebSocketBuilder setOpen(boolean open) {
        this.open = open;
        return this;
    }

    public WebSocket build() {
        return new WebSocketImpl(getRemoteAddress(), getUri(), isOpen(), isBinary(),
                webSocketSender, headers, params);
    }


}

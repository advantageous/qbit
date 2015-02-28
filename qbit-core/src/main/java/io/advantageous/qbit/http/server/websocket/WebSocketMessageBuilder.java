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

package io.advantageous.qbit.http.server.websocket;

import io.advantageous.qbit.http.websocket.WebSocketSender;

/**
 * Allows one to createWithWorkers a WebSocket message to forwardEvent.
 * Created by rhightower on 10/24/14.
 *
 * @author rhightower
 */
public class WebSocketMessageBuilder {


    private String uri;
    private Object message;
    private WebSocketSender sender;
    private String remoteAddress;
    private long messageId = -1;
    private long timestamp;

    public static WebSocketMessageBuilder webSocketMessageBuilder() {
        return new WebSocketMessageBuilder();
    }

    public String getUri() {
        return uri;
    }

    public WebSocketMessageBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Object getMessage() {
        return message;
    }

    public WebSocketMessageBuilder setMessage(Object message) {
        this.message = message;
        return this;

    }

    public WebSocketSender getSender() {
        return sender;
    }

    public WebSocketMessageBuilder setSender(WebSocketSender sender) {
        this.sender = sender;
        return this;

    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public WebSocketMessageBuilder setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;

    }

    public WebSocketMessage build() {
        return new WebSocketMessage(messageId, timestamp, uri, message, remoteAddress, sender);
    }

    public WebSocketMessageBuilder setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public WebSocketMessageBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}

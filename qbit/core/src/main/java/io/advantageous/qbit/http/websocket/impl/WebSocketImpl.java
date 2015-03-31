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

package io.advantageous.qbit.http.websocket.impl;

import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.network.impl.NetSocketBase;
import io.advantageous.qbit.util.MultiMap;

/**
 * Created by rhightower on 2/14/15.
 */
public class WebSocketImpl extends NetSocketBase implements WebSocket {

    private final MultiMap<String, String> headers;
    private final MultiMap<String, String> params;


    public WebSocketImpl(String remoteAddress, String uri, boolean open, boolean binary,
                         WebSocketSender networkSender, MultiMap<String, String> headers,
                         MultiMap<String, String> params) {
        super(remoteAddress, uri, open, binary, networkSender);
        this.headers = headers;
        this.params = params;
    }

    @Override
    public MultiMap<String, String> headers() {
        return headers;
    }

    @Override
    public MultiMap<String, String> params() {
        return params;
    }

}

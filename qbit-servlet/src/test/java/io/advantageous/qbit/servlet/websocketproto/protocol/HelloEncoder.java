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

package io.advantageous.qbit.servlet.websocketproto.protocol;

import io.advantageous.qbit.servlet.websocketproto.model.Hello;
import io.advantageous.boon.Boon;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import static io.advantageous.boon.Boon.puts;


/**
 * @author rhightower on 2/12/15.
 */
public class HelloEncoder implements Encoder.Text<Hello> {
    @Override
    public void init(final EndpointConfig config) {
    }

    @Override
    public String encode(final Hello hello) throws EncodeException {

        String json = Boon.toJson(hello);
        puts("ENCODER CALLED", json);
        return json;

    }

    @Override
    public void destroy() {
    }
}

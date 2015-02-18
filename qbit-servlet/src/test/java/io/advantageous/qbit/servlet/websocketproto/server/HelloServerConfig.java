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

package io.advantageous.qbit.servlet.websocketproto.server;

import io.advantageous.qbit.servlet.websocketproto.protocol.HelloDecoder;
import io.advantageous.qbit.servlet.websocketproto.protocol.HelloEncoder;
import org.boon.Lists;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author rhightower on 2/12/15.
 */
public class HelloServerConfig implements ServerEndpointConfig {


    @Override
    public Class<?> getEndpointClass() {
        return HelloServerEndpoint.class;
    }

    @Override
    public String getPath() {
        return "/hello";
    }

    @Override
    public List<String> getSubprotocols() {
        return Collections.emptyList();
    }

    @Override
    public List<Extension> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public Configurator getConfigurator() {
        for (ServerEndpointConfig.Configurator impl : ServiceLoader.load(javax.websocket.server.ServerEndpointConfig.Configurator.class)) {
            return impl;
        }
        throw new IllegalStateException("Cannot load platform configurator");
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return Lists.list((Class) HelloEncoder.class);

    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return Lists.list((Class) HelloDecoder.class);

    }

    @Override
    public Map<String, Object> getUserProperties() {
        return Collections.emptyMap();
    }
}


package io.advantageous.qbit.servlet.websocketproto.server;

import io.advantageous.qbit.servlet.websocketproto.protocol.HelloDecoder;
import io.advantageous.qbit.servlet.websocketproto.protocol.HelloEncoder;
import org.boon.Lists;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;
import java.util.*;

/**
 * Created by rhightower on 2/12/15.
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


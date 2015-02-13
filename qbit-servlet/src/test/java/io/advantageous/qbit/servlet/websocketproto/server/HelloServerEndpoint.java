package io.advantageous.qbit.servlet.websocketproto.server;

import io.advantageous.qbit.servlet.websocketproto.model.Hello;
import io.advantageous.qbit.servlet.websocketproto.protocol.HelloDecoder;
import io.advantageous.qbit.servlet.websocketproto.protocol.HelloEncoder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;


/**
 * Created by rhightower on 2/12/15.
 */
@javax.websocket.server.ServerEndpoint(
        value = "/hello",
        encoders = {HelloEncoder.class},
        decoders = {HelloDecoder.class}
)
public class HelloServerEndpoint {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(final Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(final Session session) {
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(final Hello hello, final Session client) throws IOException, EncodeException {
        for (final Session session : sessions) {
            session.getBasicRemote().sendObject(new Hello("RESPONSE FROM SERVER " + hello.getHello()));
        }
    }
}

package io.advantageous.qbit.servlet.websocketproto.client;

import io.advantageous.qbit.servlet.websocketproto.model.Hello;
import io.advantageous.qbit.servlet.websocketproto.protocol.HelloDecoder;
import io.advantageous.qbit.servlet.websocketproto.protocol.HelloEncoder;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/12/15.
 */

@ClientEndpoint(
        encoders = {HelloEncoder.class},
        decoders = {HelloDecoder.class}
)
public class HelloClientEndpoint {

    @OnOpen
    public void onOpen( final Session session ) throws IOException, EncodeException  {
        session.getBasicRemote().sendObject( new Hello( "Hello World From Client!" ) );
    }

    @OnMessage
    public void onMessage( final Hello hello) {
        puts("Message From Server", hello);
    }
}
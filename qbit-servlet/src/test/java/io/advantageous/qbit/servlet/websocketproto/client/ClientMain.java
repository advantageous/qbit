package io.advantageous.qbit.servlet.websocketproto.client;

import java.net.URI;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import io.advantageous.qbit.servlet.websocketproto.model.Hello;
import org.boon.core.Sys;
import org.eclipse.jetty.util.component.LifeCycle;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/12/15.
 */
public class ClientMain {
    public static void main(final String[] args) throws Exception {

        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        final String uri = "ws://localhost:8080/hello";

        Session session = container.connectToServer(HelloClientEndpoint.class, URI.create(uri));

        container.connectToServer(HelloClientEndpoint.class, URI.create(uri));


        for (int index = 0; index < 10; index++) {
            puts("Send message");
            session.getBasicRemote().sendObject(new Hello("Hello world! " + index));
            Sys.sleep(1000);
        }


        if (container instanceof LifeCycle) {
            ((LifeCycle) container).stop();
        }


        while (true) Sys.sleep(1000);

    }
}


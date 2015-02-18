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

package io.advantageous.qbit.servlet.websocketproto.client;

import io.advantageous.qbit.servlet.websocketproto.model.Hello;
import org.boon.core.Sys;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

import static org.boon.Boon.puts;

/**
 * @author rhightower on 2/12/15.
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


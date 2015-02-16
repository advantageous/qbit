/*******************************************************************************
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
 *  ________ __________.______________
 *  \_____  \\______   \   \__    ___/
 *   /  / \  \|    |  _/   | |    |  ______
 *  /   \_/.  \    |   \   | |    | /_____/
 *  \_____\ \_/______  /___| |____|
 *         \__>      \/
 *  ___________.__                  ____.                        _____  .__                                             .__
 *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
 *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
 *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
 *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
 *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
 *  .____    ._____.
 *  |    |   |__\_ |__
 *  |    |   |  || __ \
 *  |    |___|  || \_\ \
 *  |_______ \__||___  /
 *          \/       \/
 *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
 *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
 *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
 *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
 *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
 *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
 *  __________           __  .__              __      __      ___.
 *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
 *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
 *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
 *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
 *          \/     \/             \/     \/         \/       \/    \/
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
 *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 ******************************************************************************/

package io.advantageous.qbit.http.jetty.impl.client;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.network.NetSocket;
import org.boon.Str;
import org.boon.primitive.Byt;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import static org.boon.Boon.puts;


/**
 * @author  rhightower on 2/16/15.
 */
public class JettyClientWebSocketSender implements WebSocketSender {

    private final Logger logger = LoggerFactory.getLogger(JettyClientWebSocketSender.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final URI connectUri;
    private final WebSocketClient webSocketClient;
    private Session session;

    public JettyClientWebSocketSender(
            final String host,
            final int port,
            final String uri,
            final WebSocketClient client) {
        this.webSocketClient = client;
        try {
            connectUri = new URI(Str.add("ws://", host, ":", Integer.toString(port), uri));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void sendText(String message) {
        if (session == null) throw new IllegalStateException("WebSocket not open");
        session.getRemote().sendStringByFuture(message);
    }

    @Override
    public void sendBytes(byte[] message) {
        if (session == null) throw new IllegalStateException("WebSocket not open");
        session.getRemote().sendBytesByFuture(ByteBuffer.wrap(message));
    }

    @Override
    public void close() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    @Override
    public void open(NetSocket netSocket) {
        openWebSocket((WebSocket) netSocket);
    }

    @Override
    public void openWebSocket(final WebSocket webSocket) {
        if (session != null) throw new IllegalStateException("WebSocket open already");
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        try {
            webSocketClient.connect(new WebSocketListener() {
                @Override
                public void onWebSocketBinary(byte[] payload, int offset, int len) {

                    if (debug) {
                        puts("onWebSocketBinary", payload, offset, len);
                    }

                    webSocket.onBinaryMessage(Byt.sliceOf(payload, offset, offset + len));
                }

                @Override
                public void onWebSocketClose(int statusCode, String reason) {
                    if (debug) {
                        puts("onWebSocketClose", statusCode, reason);
                    }

                    webSocket.onClose();

                }

                @Override
                public void onWebSocketConnect(Session session) {

                    JettyClientWebSocketSender.this.session = session;
                    webSocket.onOpen();

                }

                @Override
                public void onWebSocketError(Throwable cause) {


                    if (debug) {
                        puts("onWebSocketError", cause);
                    }

                    if (cause instanceof Exception) {
                        webSocket.onError(((Exception) cause));
                    } else {
                        webSocket.onError(new Exception(cause));
                    }
                }

                @Override
                public void onWebSocketText(final String message) {

                    if (debug) {
                        puts("onWebSocketText", message);
                    }

                    webSocket.onTextMessage(message);
                }
            }, connectUri, request);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public URI getConnectUri() {
        return connectUri;
    }
}

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

package io.advantageous.qbit.http.jetty.impl.server;

import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import org.boon.primitive.Byt;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;

public class JettyNativeWebSocketHandler extends WebSocketAdapter {
    private final ServletUpgradeRequest request;
    private final SimpleHttpServer httpServer;
    private WebSocket webSocket;

    public JettyNativeWebSocketHandler(final ServletUpgradeRequest request,
                                       final SimpleHttpServer httpServer) {
        this.request = request;
        this.httpServer = httpServer;
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        webSocket.onClose();
    }

    @Override
    public void onWebSocketConnect(final Session session) {
        super.onWebSocketConnect(session);

        final Map<String, List<String>> headers = session.getUpgradeRequest().getHeaders();
        final Map<String, List<String>> params = session.getUpgradeRequest().getParameterMap();

        webSocket = webSocketBuilder()
                .setRemoteAddress(request.getRemoteAddress())
                .setUri(request.getRequestURI().getPath())
                .setWebSocketSender(new WebSocketSender() {
                    @Override
                    public void sendText(String message) {

                        getRemote().sendStringByFuture(message);
                    }

                    @Override
                    public void sendBytes(byte[] message) {
                        getRemote().sendBytesByFuture(ByteBuffer.wrap(message));
                    }

                    @Override
                    public void close() {
                        session.close();
                    }

                })
                .setHeaders(new JettyMultiMapAdapter(headers))
                .setParams(new JettyMultiMapAdapter(params))
                .build();

        httpServer.handleOpenWebSocket(webSocket);
        webSocket.onOpen();
    }

    @Override
    public void onWebSocketError(final Throwable cause) {
        if (cause instanceof Exception) {
            webSocket.onError(((Exception) cause));
        } else {
            webSocket.onError(new Exception(cause));
        }
    }

    @Override
    public void onWebSocketText(String webSocketMessageIn) {
        webSocket.onTextMessage(webSocketMessageIn);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        webSocket.onBinaryMessage(Byt.sliceOf(payload, offset, offset + len));
    }
}
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
 * Created by rhightower on 2/16/15.
 */
public class JettyClientWebSocketSender implements WebSocketSender {

    private final Logger logger = LoggerFactory.getLogger(JettyClientWebSocketSender.class);
    private final boolean debug = true || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final URI connectUri;
    private final WebSocketClient webSocketClient;
    private  Session session;

    public JettyClientWebSocketSender(final String host,
                                      final int port,
                                      final String uri,
                                      final WebSocketClient client
                                      ) {

        this.webSocketClient = client;
        try {
            connectUri = new URI(Str.add("ws://", host, ":", Integer.toString(port), uri));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }

    }
    @Override
    public void sendText(String message) {
        if (session==null) throw new IllegalStateException("WebSocket not open");
        session.getRemote().sendStringByFuture(message);
    }

    @Override
    public void sendBytes(byte[] message) {
        if (session==null) throw new IllegalStateException("WebSocket not open");
        session.getRemote().sendBytesByFuture(ByteBuffer.wrap(message));
    }

    @Override
    public void close() {
        session.close();
        session = null;
    }

    @Override
    public void open(NetSocket netSocket) {
        openWebSocket((WebSocket) netSocket);
    }

    @Override
    public void openWebSocket(final WebSocket webSocket) {
        if (session!=null) throw new IllegalStateException("WebSocket open already");
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        try {
            webSocketClient.connect(new WebSocketListener(){
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
}

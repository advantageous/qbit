package io.advantageous.qbit.http.jetty.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import org.boon.Str;
import org.boon.primitive.Byt;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;
import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/15/15.
 */
public class JettyNativeClientWebSocketHandler extends WebSocketAdapter {

    private final Logger logger = LoggerFactory.getLogger(JettyQBitHttpClient.class);
    private final boolean debug = true || GlobalConstants.DEBUG || logger.isDebugEnabled();

    private final String uri;
    private final Consumer<WebSocket> webSocketConsumer;
    private WebSocket webSocket;
    private final String connectUrl;

    public JettyNativeClientWebSocketHandler(
            final String uri, final String host, final int port,
            Consumer<WebSocket> webSocketConsumer) {

        this.webSocketConsumer = webSocketConsumer;

        connectUrl = Str.add("ws://", host, ":", Integer.toString(port), uri);

        this.uri = uri;

        if (debug) {
            puts(connectUrl, uri);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        if (debug) {
            puts("CLOSE CALLED", statusCode, reason);
        }
        webSocket.onClose();
    }

    @Override
    public void onWebSocketConnect(final Session session) {
        super.onWebSocketConnect(session);


        if (debug) {
            puts("onWebSocketConnect");
        }

        webSocket = webSocketBuilder()
                .setUri(uri)
                .setRemoteAddress(connectUrl)
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
                .build();
        webSocketConsumer.accept(webSocket);
        webSocket.onOpen();
    }

    @Override
    public void onWebSocketError(final Throwable cause) {


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
    public void onWebSocketText(String webSocketMessageIn) {

        if (debug) {
            puts("onWebSocketText", webSocketMessageIn);
        }

        webSocket.onTextMessage(webSocketMessageIn);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {

        if (debug) {
            puts("onWebSocketBinary", payload, offset, len);
        }

        webSocket.onBinaryMessage(Byt.sliceOf(payload, offset, offset + len));
    }
}
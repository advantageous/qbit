package io.advantageous.qbit.http.jetty.impl.server;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import org.boon.primitive.Byt;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import java.nio.ByteBuffer;
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
        webSocket.onBinaryMessage(Byt.sliceOf(payload, offset, offset+len));
    }
}
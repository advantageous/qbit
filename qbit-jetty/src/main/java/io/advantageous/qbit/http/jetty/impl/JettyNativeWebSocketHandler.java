package io.advantageous.qbit.http.jetty.impl;
import io.advantageous.qbit.http.websocket.WebSocketMessage;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.util.Timer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import static io.advantageous.qbit.http.websocket.WebSocketMessageBuilder.webSocketMessageBuilder;

public class JettyNativeWebSocketHandler extends WebSocketAdapter {
    private Session session;

    private final ServletUpgradeRequest request;
    private final SimpleHttpServer httpServer;

    public JettyNativeWebSocketHandler(final ServletUpgradeRequest request,
                                       final SimpleHttpServer httpServer) {
        this.request = request;
        this.httpServer = httpServer;
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {

        final WebSocketMessage webSocketMessage = webSocketMessageBuilder()
                .setMessage(reason)
                .setUri(request.getRequestURI().getPath())
                .setRemoteAddress(request.getRemoteAddress())
                .setTimestamp(Timer.timer().now()).build();


        httpServer.handleWebSocketClosedMessage(webSocketMessage);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
    }

    @Override
    public void onWebSocketError(Throwable cause) {

        //do something
    }

    @Override
    public void onWebSocketText(String webSocketMessageIn) {

        final WebSocketMessage webSocketMessage = webSocketMessageBuilder()
                .setMessage(webSocketMessageIn)
                .setUri(request.getRequestURI().getPath())
                .setRemoteAddress(request.getRemoteAddress())
                .setTimestamp(Timer.timer().now()).setSender(
                        message -> {
                            session.getRemote().sendStringByFuture(message);

                        }).build();
        httpServer.handleWebSocketMessage(webSocketMessage);
    }

}
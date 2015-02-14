package io.advantageous.qbit.http.jetty.impl;

import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.http.impl.SimpleHttpServer;
import io.advantageous.qbit.util.Timer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;

import static io.advantageous.qbit.http.WebSocketMessageBuilder.webSocketMessageBuilder;
import static org.boon.Boon.puts;


/**
 * Created by rhightower on 2/13/15.
 */
public class QBitWebSocketHandler extends WebSocketHandler {

    private final SimpleHttpServer httpServer;

//    private static ThreadLocal<WebSocketContext> webSocketContextThreadLocal = new ThreadLocal<>();
//
//    private static class WebSocketContext {
//
//        private final SimpleHttpServer httpServer;
//        private final HttpServletRequest request;
//
//        private WebSocketContext(SimpleHttpServer httpServer, HttpServletRequest request) {
//            this.httpServer = httpServer;
//            this.request = request;
//        }
//    }
    private static Timer timer = new Timer();

    public QBitWebSocketHandler(final SimpleHttpServer httpServer) {
        this.httpServer = httpServer;
    }

    public static class QBitWSHandler implements WebSocketListener {

        private Session session;

        private final ServletUpgradeRequest request;
        private final SimpleHttpServer httpServer;

        public QBitWSHandler(ServletUpgradeRequest request, ServletUpgradeResponse response, SimpleHttpServer httpServer) {
            this.request = request;
            this.httpServer = httpServer;
        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {

        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {

        }

        @Override
        public void onWebSocketConnect(Session session) {

            this.session = session;
        }

        @Override
        public void onWebSocketError(Throwable cause) {

        }

        @Override
        public void onWebSocketText(String webSocketMessageIn) {




            final WebSocketMessage webSocketMessage = webSocketMessageBuilder()
                    .setMessage(webSocketMessageIn)
                    .setUri(request.getRequestURI().getPath())
                    .setRemoteAddress(request.getRemoteAddress())
                    .setTimestamp(timer.now()).setSender(
                            message -> {
                                try {
                                    session.getRemote().sendString(message);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).build();

            httpServer.handleWebSocketMessage(webSocketMessage);


        }
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new WebSocketCreator() {
            @Override
            public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
                return new QBitWSHandler(request, response, httpServer);
            }
        });
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        WebSocketContext webSocketContext = new WebSocketContext(httpServer, request);
//        webSocketContextThreadLocal.set(webSocketContext);
        super.handle(target, baseRequest, request, response);
        //webSocketContextThreadLocal.set(null);

    }
}

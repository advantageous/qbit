package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.network.NetSocket;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;

import java.nio.charset.StandardCharsets;

import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;

/**
 * Created by rhightower on 2/15/15.
 */
public class VertxUtils {


    volatile long requestId;
    volatile long time;
    volatile long webSocketId;

    public void setTime(long time) {
        this.time = time;
    }

    public WebSocketMessage createWebSocketMessage(final ServerWebSocket serverWebSocket, final Buffer buffer) {


        return createWebSocketMessage(serverWebSocket.uri(), serverWebSocket.remoteAddress().toString(),

                new WebSocketSender() {
                    @Override
                    public void sendText(String message) {
                        serverWebSocket.writeTextFrame(message);
                    }
                    @Override
                    public void sendBytes(byte[] message) {
                        serverWebSocket.writeBinaryFrame(new Buffer(message));

                    }
                }, buffer != null ? buffer.toString("UTF-8"): "");
    }

    public  WebSocketMessage createWebSocketMessage(final String address, final String returnAddress, final WebSocketSender webSocketSender, final String message) {


        return new WebSocketMessage(webSocketId, time, address, message, returnAddress, webSocketSender);
    }

    public  HttpRequest createRequest(final HttpServerRequest request, final Buffer buffer) {


        final MultiMap<String, String> params = request.params().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.params());
        final MultiMap<String, String> headers = request.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.headers());
        final byte[] body = buffer == null ? "".getBytes(StandardCharsets.UTF_8) : buffer.getBytes();

        final String contentType = request.headers().get("Content-Type");

        return new HttpRequest(requestId++, request.path(), request.method(), params, headers, body,
                request.remoteAddress().toString(),
                contentType, createResponse(request.response()), time);
    }

    private HttpResponseReceiver createResponse(final HttpServerResponse response) {
        return (code, mimeType, body) -> {

            //TODO put the rest of the headers here
            response.setStatusCode(code).putHeader("Content-Type", mimeType);
            //response.setStatusCode(code).putHeader("Keep-Alive", "timeout=600");
            Buffer buffer = createBuffer(body);
            response.end(buffer);

        };
    }
    private static Buffer createBuffer(Object body) {
        Buffer buffer = null;

        if (body instanceof byte[]) {

            byte[] bBody = ((byte[]) body);
            buffer = new Buffer(bBody);

        } else if (body instanceof String) {

            String sBody = ((String) body);
            buffer = new Buffer(sBody, "UTF-8");
        }
        return buffer;
    }



    public WebSocket createWebSocket(final ServerWebSocket vertxServerWebSocket) {

        /* Create a websocket that uses vertxServerWebSocket to send messages. */
        final WebSocket webSocket = webSocketBuilder().setUri(vertxServerWebSocket.uri())
                .setRemoteAddress(vertxServerWebSocket.remoteAddress().toString())
                .setWebSocketSender(new WebSocketSender() {
                    @Override
                    public void sendText(String message) {
                        vertxServerWebSocket.writeTextFrame(message);
                    }

                    @Override
                    public void sendBytes(byte[] message) {
                        vertxServerWebSocket.writeBinaryFrame(new Buffer(message));
                    }

                    @Override
                    public void close() {
                          vertxServerWebSocket.close();
                    }
                })
                .build();


        /* Handle open. */
        webSocket.onOpen();

        /* Handle close. */
        vertxServerWebSocket.endHandler(event -> {
            webSocket.onClose();
        });

        /* Handle message. */
        vertxServerWebSocket.dataHandler(buffer -> {
            final String message = buffer.toString("UTF-8");
            webSocket.onTextMessage( message );
        });

        /* Handle error. */
        vertxServerWebSocket.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {
                if (event instanceof Exception) {
                    webSocket.onError((Exception) event);
                } else {
                    webSocket.onError(new Exception(event));
                }
            }
        });

        return webSocket;
    }


}

package io.advantageous.qbit.vertx.service;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.BufferUtils;
import org.boon.Str;
import org.boon.core.Sys;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
* Created by rhightower on 1/26/15.
*/
public class BeforeStartHandler implements Consumer<HttpServer> {


    public static final String HTTP_REQUEST_RECEIVE_EVENT = "HTTP_REQUEST_RECEIVE_EVENT";
    public static final String HTTP_WEB_SOCKET_RECEIVE_EVENT = "HTTP_WEB_SOCKET_RECEIVE_EVENT";
    public static final String HTTP_REQUEST_RESPONSE_EVENT = "HTTP_REQUEST_RESPONSE_EVENT";
    public static final String HTTP_WEB_SOCKET_RESPONSE_EVENT = "HTTP_WEB_SOCKET_RESPONSE_EVENT";

    private Map<String, HttpRequest> map = new ConcurrentHashMap<>();


    private Map<String, WebSocketMessage> wsMap = new ConcurrentHashMap<>();

    private Vertx vertx = null;

    private String serverId;

    private String returnAddress;

    private String httpReceiveRequestEventChannel = null;

    private String httpReceiveWebSocketEventChannel = null;
    private String httpRequestResponseEventChannel = null;
    private String webSocketReturnChannel = null;


    public String returnAddress() {
        if (returnAddress==null) {
            returnAddress = UUID.randomUUID().toString();
        }
        return returnAddress;
    }

    public String httpReceiveRequestEventChannel() {
        if (httpReceiveRequestEventChannel==null) {
            httpReceiveRequestEventChannel = Str.add(serverId, ".", HTTP_REQUEST_RECEIVE_EVENT);
        }
        return httpReceiveRequestEventChannel;
    }


    public String httpReceiveWebSocketEventChannel() {
        if (httpReceiveWebSocketEventChannel==null) {
            httpReceiveWebSocketEventChannel = Str.add(serverId, ".", HTTP_WEB_SOCKET_RECEIVE_EVENT);
        }
        return httpReceiveWebSocketEventChannel;
    }
    @Override
    public void accept(final HttpServer httpServer) {

        httpServer.setHttpRequestConsumer(request -> {
            Buffer buffer = new Buffer();
            BufferUtils.writeString(buffer, "" + returnAddress());

            BufferUtils.writeString(buffer, "" + request.id());
            BufferUtils.writeString(buffer, request.getUri());
            BufferUtils.writeString(buffer, request.getMethod());
            BufferUtils.writeString(buffer, request.getRemoteAddress());
            BufferUtils.writeMap(buffer, request.getParams());
            BufferUtils.writeMap(buffer, request.getHeaders());
            BufferUtils.writeString(buffer, request.getBodyAsString());

            vertx.eventBus().send(httpReceiveRequestEventChannel(), buffer);


            String requestKey = Str.add(request.getRemoteAddress(), "|" + request.id());

            map.put(requestKey, request);

        });


        httpServer.setWebSocketMessageConsumer(new Consumer<WebSocketMessage>() {
            @Override
            public void accept(WebSocketMessage webSocketMessage) {

                Buffer buffer = new Buffer();
                BufferUtils.writeString(buffer, "" + returnAddress());
                BufferUtils.writeString(buffer, "" + webSocketMessage.id());
                BufferUtils.writeString(buffer, "" + webSocketMessage.timestamp());
                BufferUtils.writeString(buffer, webSocketMessage.getUri());
                BufferUtils.writeString(buffer, webSocketMessage.getRemoteAddress());
                BufferUtils.writeString(buffer, webSocketMessage.getMessage());


                final WebSocketMessage webSocketMessage1 = wsMap.get(webSocketMessage.getRemoteAddress());

                if (webSocketMessage1 == null) {
                    wsMap.put(webSocketMessage.getRemoteAddress(), webSocketMessage);
                }

                vertx.eventBus().send(httpReceiveWebSocketEventChannel(), buffer);
            }
        });

        System.out.println("REGISTERING HTTP " + httpRequestResponseEventChannel());
        System.out.flush();
        Sys.sleep(10);

        vertx.eventBus().registerHandler(httpRequestResponseEventChannel(), new Handler<Message>() {
            @Override
            public void handle(Message event) {
                Message<Buffer> bufferMessage = (Message<Buffer>) event;

                handleHttpResponse(bufferMessage.body());

            }
        });


        System.out.println("REGISTERING WS " + webSocketReturnChannel());
        System.out.flush();

        vertx.eventBus().registerHandler(webSocketReturnChannel(), new Handler<Message>() {
            @Override
            public void handle(Message event) {
                Message<Buffer> bufferMessage = (Message<Buffer>) event;

                handleWebSocketReturn(bufferMessage.body());

            }
        });

    }

    private void handleWebSocketReturn(Buffer buffer) {


        int [] location = new int[]{0};


        String remoteAddress = BufferUtils.readString(buffer, location);

        String body = BufferUtils.readString(buffer, location);

        final WebSocketMessage webSocketMessage = wsMap.get(remoteAddress);

        if (webSocketMessage != null) {
            webSocketMessage.getSender().send(body);
        }

    }

    private void handleHttpResponse(Buffer buffer) {


        int code = buffer.getShort(0);

        int [] location = new int[]{2};


        String responseKey = BufferUtils.readString(buffer, location);

        String mimeType = BufferUtils.readString(buffer, location);

        String body = BufferUtils.readString(buffer, location);


        final HttpRequest request = map.get(responseKey);

        if (request!=null) {
            request.getResponse().response(code, mimeType, body);
        }


    }


    public String httpRequestResponseEventChannel() {
        if (httpRequestResponseEventChannel==null) {
            httpRequestResponseEventChannel = Str.add(serverId, ".", BeforeStartHandler.HTTP_REQUEST_RESPONSE_EVENT, ".", returnAddress());
        }
        return httpRequestResponseEventChannel;
    }


    public String webSocketReturnChannel() {
        if (webSocketReturnChannel==null) {
            webSocketReturnChannel = Str.add(serverId, ".", BeforeStartHandler.HTTP_WEB_SOCKET_RESPONSE_EVENT, ".", returnAddress());
        }
        return webSocketReturnChannel;
    }

}

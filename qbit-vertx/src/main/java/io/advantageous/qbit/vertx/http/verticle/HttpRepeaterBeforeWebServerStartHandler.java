package io.advantageous.qbit.vertx.http.verticle;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.vertx.BufferUtils;
import org.boon.Str;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
* Created by rhightower on 1/26/15.
*/
public class HttpRepeaterBeforeWebServerStartHandler implements Consumer<HttpServer> {


    public static final String HTTP_REQUEST_RECEIVE_EVENT = "HTTP_REQUEST_RECEIVE_EVENT";
    public static final String HTTP_WEB_SOCKET_RECEIVE_EVENT = "HTTP_WEB_SOCKET_RECEIVE_EVENT";
    public static final String HTTP_REQUEST_RESPONSE_EVENT = "HTTP_REQUEST_RESPONSE_EVENT";
    public static final String HTTP_WEB_SOCKET_RESPONSE_EVENT = "HTTP_WEB_SOCKET_RESPONSE_EVENT";
    public static final String HTTP_WEB_SOCKET_CLOSE_EVENT = "HTTP_WEB_SOCKET_CLOSE_EVENT";

    private Map<String, HttpRequest> map = new ConcurrentHashMap<>();


    private Map<String, WebSocketMessage> wsMap = new ConcurrentHashMap<>();

    private Vertx vertx = null;

    private String serverId;

    private String returnAddress;

    private String httpReceiveRequestEventChannel = null;

    private String httpReceiveWebSocketEventChannel = null;
    private String httpRequestResponseEventChannel = null;
    private String webSocketReturnChannel = null;
    private String httpReceiveWebSocketClosedEventChannel=null;

    private BlockingQueue<HttpRequest> requestQueue = new ArrayBlockingQueue<>(10);


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


    public String httpReceiveWebSocketClosedEventChannel() {
        if (httpReceiveWebSocketClosedEventChannel==null) {
            httpReceiveWebSocketClosedEventChannel = Str.add(serverId, ".", HTTP_WEB_SOCKET_CLOSE_EVENT);
        }
        return httpReceiveWebSocketClosedEventChannel;
    }

    public String httpReceiveWebSocketEventChannel() {
        if (httpReceiveWebSocketEventChannel==null) {
            httpReceiveWebSocketEventChannel = Str.add(serverId, ".", HTTP_WEB_SOCKET_RECEIVE_EVENT);
        }
        return httpReceiveWebSocketEventChannel;
    }
    @Override
    public void accept(final HttpServer httpServer) {

        httpServer.setWebSocketCloseConsumer(
                webSocketMessage -> {
                            wsMap.remove(webSocketMessage.getRemoteAddress());
                    final Buffer buffer = new Buffer();
                    BufferUtils.writeString(buffer, webSocketMessage.getRemoteAddress());
                    vertx.eventBus().send(httpReceiveWebSocketClosedEventChannel(),
                                    buffer);


                });

        httpServer.setHttpRequestConsumer(request -> {

            if (!requestQueue.offer(request)) {
                sendRequests(request);
            }
            String requestKey = Str.add(request.getRemoteAddress(), "|" + request.id());
            map.put(requestKey, request);


        });





        httpServer.setWebSocketMessageConsumer(webSocketMessage -> {

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
        });


        vertx.eventBus().registerHandler(httpRequestResponseEventChannel(), new Handler<Message>() {
            @Override
            public void handle(Message event) {
                Message<Buffer> bufferMessage = (Message<Buffer>) event;
                handleHttpResponse(bufferMessage.body());
            }
        });

        vertx.eventBus().registerHandler(webSocketReturnChannel(), new Handler<Message>() {
            @Override
            public void handle(Message event) {
                Message<Buffer> bufferMessage = (Message<Buffer>) event;
                handleWebSocketReturn(bufferMessage.body());
            }
        });

        vertx.setPeriodic(100, new Handler<Long>() {
            @Override
            public void handle(Long event) {

                if (requestQueue.size()>0) {
                    sendRequests(null);
                }
            }
        });

    }

    private void sendRequests(HttpRequest request) {

        List<HttpRequest> list = new ArrayList<>();
        HttpRequest currentRequest = requestQueue.poll();

        while (currentRequest!=null) {

            list.add(currentRequest);
            currentRequest = requestQueue.poll();

        }

        if (request!=null) {
            list.add(request);
        }


        Buffer buffer = new Buffer();
        buffer.appendShort((short)list.size());
        BufferUtils.writeString(buffer, returnAddress());


        for (HttpRequest item : list) {
            BufferUtils.writeString(buffer, "" + item.id());
            BufferUtils.writeString(buffer, item.getUri());
            BufferUtils.writeString(buffer, item.getMethod());
            BufferUtils.writeString(buffer, item.getRemoteAddress());
            BufferUtils.writeMap(buffer, item.getParams());
            BufferUtils.writeMap(buffer, item.getHeaders());
            BufferUtils.writeString(buffer, item.getBodyAsString());

        }


        vertx.eventBus().send(httpReceiveRequestEventChannel(), buffer);


    }

    private void handleWebSocketReturn(Buffer buffer) {


        int [] location = new int[]{0};
        final String remoteAddress = BufferUtils.readString(buffer, location);
        final String body = BufferUtils.readString(buffer, location);
        final WebSocketMessage webSocketMessage = wsMap.get(remoteAddress);

        if (webSocketMessage != null) {
            webSocketMessage.getSender().send(body);
        }

    }

    private void handleHttpResponse(Buffer buffer) {


        int size = buffer.getShort(0);
        int[] location = new int[]{2};


        for (int index = 0; index < size; index++) {

            int code = buffer.getShort(location[0]);
            location[0] = location[0] + 2;
            String responseKey = BufferUtils.readString(buffer, location);
            String mimeType = BufferUtils.readString(buffer, location);
            String body = BufferUtils.readString(buffer, location);
            final HttpRequest request = map.get(responseKey);
            if (request != null) {
                request.getResponse().response(code, mimeType, body);
            }
        }
    }


    public String httpRequestResponseEventChannel() {
        if (httpRequestResponseEventChannel==null) {
            httpRequestResponseEventChannel = Str.add(serverId, ".", HttpRepeaterBeforeWebServerStartHandler.HTTP_REQUEST_RESPONSE_EVENT, ".", returnAddress());
        }
        return httpRequestResponseEventChannel;
    }


    public String webSocketReturnChannel() {
        if (webSocketReturnChannel==null) {
            webSocketReturnChannel = Str.add(serverId, ".", HttpRepeaterBeforeWebServerStartHandler.HTTP_WEB_SOCKET_RESPONSE_EVENT, ".", returnAddress());
        }
        return webSocketReturnChannel;
    }

}

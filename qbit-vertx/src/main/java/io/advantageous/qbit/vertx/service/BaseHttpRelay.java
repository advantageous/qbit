package io.advantageous.qbit.vertx.service;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpRequestBuilder;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.http.WebSocketMessageBuilder;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.BufferUtils;
import io.advantageous.qbit.vertx.http.HttpServerVerticle;
import org.boon.Str;
import org.boon.core.Sys;
import org.boon.core.reflection.BeanUtils;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.fields.FieldAccess;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by rhightower on 1/27/15.
 */
public abstract class BaseHttpRelay extends Verticle {
    public static final String SERVICE_SERVER_VERTICLE_PORT = "ServiceServerVerticle.port";
    public static final String SERVICE_SERVER_VERTICLE_HTTP_WORKERS = "ServiceServerVerticle.httpWorkers";
    public static final String SERVICE_SERVER_VERTICLE_HOST = "ServiceServerVerticle.host";
    public static final String SERVICE_SERVER_VERTICLE_MANAGE_QUEUES = "ServiceServerVerticle.manageQueues";
    public static final String SERVICE_SERVER_VERTICLE_POLL_TIME = "ServiceServerVerticle.pollTime";
    public static final String SERVICE_SERVER_VERTICLE_MAX_REQUEST_BATCHES = "ServiceServerVerticle.maxRequestBatches";
    public static final String SERVICE_SERVER_VERTICLE_FLUSH_INTERVAL = "ServiceServerVerticle.flushInterval";
    public static final String SERVICE_SERVER_VERTICLE_REQUEST_BATCH_SIZE = "ServiceServerVerticle.requestBatchSize";
    public static final String SERVICE_SERVER_VERTICLE_HANDLER = "ServiceServerVerticle.handler";
    public static final String SERVICE_SERVER_VERTICLE_BUNDLE_URI = "ServiceServerVerticle.bundleUri";
    protected int pollTime;
    protected int requestBatchSize = 20;
    protected int httpWorkers = 4;
    protected int timeoutInSeconds = 30;
    protected Object context = Sys.contextToHold();
    protected AtomicBoolean stop = new AtomicBoolean();
    protected ServiceBundle serviceBundle;
    protected String serverId;
    private int port = 8080;
    private String host = null;
    private boolean manageQueues = false;
    private int flushInterval = 100;
    private int maxRequestBatches = -1;
    private String httpReceiveRequestEventChannel = null;
    private String httpRequestResponseEventChannel = null;
    private String httpReceiveWebSocketEventChannel = null;
    private String webSocketReturnChannel;

    public String webSocketReturnChannel() {
        if (webSocketReturnChannel==null) {
            webSocketReturnChannel = Str.add(serverId, ".", BeforeWebServerStartsHandler.HTTP_WEB_SOCKET_RESPONSE_EVENT);
        }

        return webSocketReturnChannel;
    }

    public String httpRequestResponseEventChannel() {
        if (httpRequestResponseEventChannel==null) {
            httpRequestResponseEventChannel = Str.add(serverId, ".", BeforeWebServerStartsHandler.HTTP_REQUEST_RESPONSE_EVENT);
        }

        return httpRequestResponseEventChannel;
    }

    public String httpReceiveRequestEventChannel() {
        if (httpReceiveRequestEventChannel==null) {
            httpReceiveRequestEventChannel = Str.add(serverId, ".", BeforeWebServerStartsHandler.HTTP_REQUEST_RECEIVE_EVENT);
        }
        return httpReceiveRequestEventChannel;
    }

    public String httpReceiveWebSocketEventChannel() {
        if (httpReceiveWebSocketEventChannel==null) {
            httpReceiveWebSocketEventChannel = Str.add(serverId, ".", BeforeWebServerStartsHandler.HTTP_WEB_SOCKET_RECEIVE_EVENT);
        }
        return httpReceiveWebSocketEventChannel;
    }

    protected void configureEventBus() {
        vertx.eventBus().registerHandler(httpReceiveRequestEventChannel(), event -> {
            Message<Buffer> bufferMessage = event;
            final Buffer request = bufferMessage.body();
            handleHttpRequest(request);
        }
        );

        vertx.eventBus().registerHandler(httpReceiveWebSocketEventChannel(), event -> {
            Message<Buffer> bufferMessage = event;
            final Buffer request = bufferMessage.body();
            handleWebSocketMessage(request);
        }
        );
    }

    protected JsonObject createHttpConfig() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_PORT, this.port);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_FLUSH_INTERVAL, this.flushInterval);
        jsonObject.putBoolean(HttpServerVerticle.HTTP_SERVER_VERTICLE_MANAGE_QUEUES, this.manageQueues);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES, this.maxRequestBatches);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_POLL_TIME, this.pollTime);
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_VERTICLE_HOST, this.host);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE, this.requestBatchSize);
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_HANDLER, BeforeWebServerStartsHandler.class.getName());
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_ID, serverId);
        return jsonObject;
    }

    private void handleWebSocketMessage(Buffer buffer) {
        final WebSocketMessage message = readWebSocketMessage(buffer);

        handleWebSocketMessage(message);
    }


    protected abstract void handleWebSocketMessage(WebSocketMessage message);

    private WebSocketMessage readWebSocketMessage(Buffer buffer) {
        int [] location = new int[]{0};
        String returnEventBusAddress = BufferUtils.readString(buffer, location);
        String id = BufferUtils.readString(buffer, location);
        long messageId = Long.parseLong(id);
        String ts = BufferUtils.readString(buffer, location);
        long timestamp = Long.parseLong(ts);
        String uri = BufferUtils.readString(buffer, location);
        String remoteAddress = BufferUtils.readString(buffer, location);
        String body = BufferUtils.readString(buffer, location);

        return new WebSocketMessageBuilder()
                .setMessage(body)
                .setRemoteAddress(remoteAddress)
                .setUri(uri)
                .setMessageId(messageId)
                .setTimestamp(timestamp)
                .setSender(message1 -> {
                    handleWebSocketReturnCallbackSend(returnEventBusAddress, remoteAddress, message1);

                }).build();
    }

    private void handleWebSocketReturnCallbackSend(String returnEventBusAddress, String remoteAddress, String message1) {
        Buffer buffer1 = new Buffer();
        BufferUtils.writeString(buffer1, remoteAddress);
        BufferUtils.writeString(buffer1, message1);
        String returnEventAddress = Str.add(webSocketReturnChannel(), ".", returnEventBusAddress);
        vertx.eventBus().send(returnEventAddress, buffer1);
    }

    private void handleHttpRequest(Buffer buffer) {
        final HttpRequest request = readHttpRequest(buffer);
        handleHttpRequest(request);

    }

    protected  abstract  void handleHttpRequest(HttpRequest httpRequest);



    private HttpRequest readHttpRequest(Buffer buffer) {
        int [] location = new int[]{0};
        String returnEventBusAddress = BufferUtils.readString(buffer, location);
        String id = BufferUtils.readString(buffer, location);
        long messageId = Long.parseLong(id);
        String uri = BufferUtils.readString(buffer, location);
        String method = BufferUtils.readString(buffer, location);
        String remoteAddress = BufferUtils.readString(buffer, location);
        MultiMap<String, String> params = BufferUtils.readMap(buffer, location);
        MultiMap<String, String> headers = BufferUtils.readMap(buffer, location);
        String body = BufferUtils.readString(buffer, location);

        return new HttpRequestBuilder().setBody(body).setUri(uri).setMethod(method)
                .setRemoteAddress(remoteAddress).setParams(params)
                .setHeaders(headers).setId(messageId)
                .setTextResponse((code, mimeType, body1) -> {


                    handleHttpRequestResponse(returnEventBusAddress, id, remoteAddress, (short) code, mimeType, body1);

                })
                .build();
    }

    private void handleHttpRequestResponse(final String returnEventBusAddress, final String id,
                                           final String remoteAddress, final short code, final String mimeType, final String body) {
        Buffer buffer1 = new Buffer();

        buffer1.appendShort((short) code);

        BufferUtils.writeString(buffer1, Str.add(remoteAddress, "|", id));
        BufferUtils.writeString(buffer1, mimeType);
        BufferUtils.writeString(buffer1, body);

        String returnEventAddress = Str.add(httpRequestResponseEventChannel(), ".", returnEventBusAddress);


        vertx.eventBus().send(returnEventAddress, buffer1);
    }

    protected void extractConfig() {
        JsonObject config = container.config();

        if (config.containsField(SERVICE_SERVER_VERTICLE_PORT)) {
            port = config.getInteger(SERVICE_SERVER_VERTICLE_PORT);
        }
        if (config.containsField(SERVICE_SERVER_VERTICLE_HOST)) {
            host = config.getString(SERVICE_SERVER_VERTICLE_HOST);
        }
        if (config.containsField(SERVICE_SERVER_VERTICLE_MANAGE_QUEUES)) {
            manageQueues = config.getBoolean(SERVICE_SERVER_VERTICLE_MANAGE_QUEUES);
        }
        if (config.containsField(SERVICE_SERVER_VERTICLE_POLL_TIME)) {
            pollTime = config.getInteger(
                    SERVICE_SERVER_VERTICLE_POLL_TIME);
        }
        if (config.containsField(SERVICE_SERVER_VERTICLE_MAX_REQUEST_BATCHES)) {
            maxRequestBatches = config.getInteger(SERVICE_SERVER_VERTICLE_MAX_REQUEST_BATCHES);
        }
        if (config.containsField(SERVICE_SERVER_VERTICLE_FLUSH_INTERVAL)) {
            flushInterval = config.getInteger(SERVICE_SERVER_VERTICLE_FLUSH_INTERVAL);
        }
        if (config.containsField(SERVICE_SERVER_VERTICLE_REQUEST_BATCH_SIZE)) {
            requestBatchSize = config.getInteger(
                    SERVICE_SERVER_VERTICLE_REQUEST_BATCH_SIZE);
        }
        if (config.containsField(SERVICE_SERVER_VERTICLE_HTTP_WORKERS)) {
            httpWorkers = config.getInteger(
                    SERVICE_SERVER_VERTICLE_HTTP_WORKERS);
        }
    }

    public void stop() {
        if (serviceBundle!=null) {
            serviceBundle.stop();
        }
        stop.set(true);
    }
}

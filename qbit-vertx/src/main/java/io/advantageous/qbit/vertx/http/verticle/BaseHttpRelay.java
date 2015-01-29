package io.advantageous.qbit.vertx.http.verticle;

import io.advantageous.qbit.http.*;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.BufferUtils;
import org.boon.Str;
import org.boon.core.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by rhightower on 1/27/15.
 */
public abstract class BaseHttpRelay extends Verticle {

    private final Logger logger = LoggerFactory.getLogger(BaseHttpRelay.class);
    private final boolean debug = logger.isDebugEnabled();
    public static final String HTTP_RELAY_VERTICLE_PORT = "HttpRelay.port";
    public static final String HTTP_RELAY_VERTICLE_HTTP_WORKERS = "HttpRelay.httpWorkers";
    public static final String HTTP_RELAY_VERTICLE_HOST = "HttpRelay.host";
    public static final String HTTP_RELAY_VERTICLE_MANAGE_QUEUES = "HttpRelay.manageQueues";
    public static final String HTTP_RELAY_VERTICLE_POLL_TIME = "HttpRelay.pollTime";
    public static final String HTTP_RELAY_VERTICLE_MAX_REQUEST_BATCHES = "HttpRelay.maxRequestBatches";
    public static final String HTTP_RELAY_VERTICLE_FLUSH_INTERVAL = "HttpRelay.flushInterval";
    public static final String HTTP_RELAY_VERTICLE_REQUEST_BATCH_SIZE = "HttpRelay.requestBatchSize";
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
    private String httpReceiveWebSocketClosedEventChannel=null;

    public String webSocketReturnChannel() {
        if (webSocketReturnChannel==null) {
            webSocketReturnChannel = Str.add(serverId, ".", HttpRepeaterBeforeWebServerStartHandler.HTTP_WEB_SOCKET_RESPONSE_EVENT);
        }

        return webSocketReturnChannel;
    }

    public String httpRequestResponseEventChannel() {
        if (httpRequestResponseEventChannel==null) {
            httpRequestResponseEventChannel = Str.add(serverId, ".", HttpRepeaterBeforeWebServerStartHandler.HTTP_REQUEST_RESPONSE_EVENT);
        }

        return httpRequestResponseEventChannel;
    }

    public String httpReceiveRequestEventChannel() {
        if (httpReceiveRequestEventChannel==null) {
            httpReceiveRequestEventChannel = Str.add(serverId, ".", HttpRepeaterBeforeWebServerStartHandler.HTTP_REQUEST_RECEIVE_EVENT);
        }
        return httpReceiveRequestEventChannel;
    }

    public String httpReceiveWebSocketEventChannel() {
        if (httpReceiveWebSocketEventChannel==null) {
            httpReceiveWebSocketEventChannel = Str.add(serverId, ".", HttpRepeaterBeforeWebServerStartHandler.HTTP_WEB_SOCKET_RECEIVE_EVENT);
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


        vertx.eventBus().registerHandler(httpReceiveWebSocketClosedEventChannel(), event -> {
                    Message<Buffer> bufferMessage = event;
                    final Buffer request = bufferMessage.body();
                    handleWebSocketClosed(request);
                }
        );
    }

    private void handleWebSocketClosed(Buffer request) {
        String remoteAddress = BufferUtils.readString(request, new int[]{0});

        WebSocketMessage webSocketMessage = new WebSocketMessageBuilder().setRemoteAddress(remoteAddress).build();
        handleWebSocketClosed(webSocketMessage);
    }

    protected abstract void handleWebSocketClosed(WebSocketMessage webSocketMessage);

    public String httpReceiveWebSocketClosedEventChannel() {
        if (httpReceiveWebSocketClosedEventChannel==null) {
            httpReceiveWebSocketClosedEventChannel = Str.add(serverId, ".", HttpRepeaterBeforeWebServerStartHandler.HTTP_WEB_SOCKET_CLOSE_EVENT);
        }
        return httpReceiveWebSocketClosedEventChannel;
    }


    protected JsonObject createHttpConfig() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_PORT, this.port);
        jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_FLUSH_INTERVAL, this.flushInterval);
        jsonObject.putBoolean(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_MANAGE_QUEUES, this.manageQueues);
        jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES, this.maxRequestBatches);
        jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_POLL_TIME, this.pollTime);
        jsonObject.putString(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_HOST, this.host);
        jsonObject.putNumber(HttpServerWorkerVerticle.HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE, this.requestBatchSize);
        jsonObject.putString(HttpServerWorkerVerticle.HTTP_SERVER_HANDLER, HttpRepeaterBeforeWebServerStartHandler.class.getName());
        jsonObject.putString(HttpServerWorkerVerticle.HTTP_SERVER_ID, serverId);
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
        final List<HttpRequest> requests = readHttpRequests(buffer);

        for (HttpRequest request : requests) {
            handleHttpRequest(request);
        }

    }


    protected  abstract  void handleHttpRequest(HttpRequest httpRequest);



    private List<HttpRequest> readHttpRequests(Buffer buffer) {

        int [] location = new int[]{2};

        final short size = buffer.getShort(0);

        String returnEventBusAddress = BufferUtils.readString(buffer, location);

        List<HttpRequest> requests = new ArrayList<>(size);



        for (int index=0; index < size; index++) {

            String id = BufferUtils.readString(buffer, location);
            long messageId = Long.parseLong(id);
            String uri = BufferUtils.readString(buffer, location);
            String method = BufferUtils.readString(buffer, location);
            String remoteAddress = BufferUtils.readString(buffer, location);
            MultiMap<String, String> params = BufferUtils.readMap(buffer, location);
            MultiMap<String, String> headers = BufferUtils.readMap(buffer, location);
            String body = BufferUtils.readString(buffer, location);

            requests.add(new HttpRequestBuilder().setBody(body).setUri(uri).setMethod(method)
                    .setRemoteAddress(remoteAddress).setParams(params)
                    .setHeaders(headers).setId(messageId)
                    .setTextResponse((code, mimeType, body1) -> {


                        handleHttpRequestResponse(returnEventBusAddress, id, remoteAddress, (short) code, mimeType, body1);

                    })
                    .build());
        }

        return requests;
    }

    public static class ResponseHolder {

        String remoteAddress;
        String id;
        int code;
        String mimeType;
        String body;

        public ResponseHolder(String remoteAddress, String id, int code, String mimeType, String body) {
            this.remoteAddress = remoteAddress;
            this.id = id;
            this.code = code;
            this.mimeType = mimeType;
            this.body = body;
        }
    }

    private Map<String, BlockingQueue<ResponseHolder>> requestQueueMap = new ConcurrentHashMap<>(100_000);

    private void handleHttpRequestResponse(final String returnEventBusAddress, final String id,
                                           final String remoteAddress, final short code, final String mimeType, final String body) {


        BlockingQueue<ResponseHolder> responses = requestQueueMap.get(returnEventBusAddress);
        if (responses == null) {
            responses = new ArrayBlockingQueue<>(10);

            requestQueueMap.put(returnEventBusAddress, responses);
        }


        ResponseHolder responseHolder = new ResponseHolder(remoteAddress, id, code, mimeType, body);

        if (!responses.offer(responseHolder)) {

            sendResponses(returnEventBusAddress, responseHolder);
        }




    }

    private void sendResponses(String returnEventBusAddress, ResponseHolder responseHolder) {



        BlockingQueue<ResponseHolder> responses = requestQueueMap.get(returnEventBusAddress);

        int queueSize = responses.size();

        if (queueSize==0 && responseHolder==null) {
            return;
        }

        List<ResponseHolder> responseHolders = new ArrayList<>(queueSize);

        ResponseHolder currentResponse = responses.poll();

        while (currentResponse!=null) {

            responseHolders.add(currentResponse);
            currentResponse = responses.poll();

        }

        if (responseHolder!=null) {
            responseHolders.add(responseHolder);
        }

        if (responseHolders.size() > 0) {

            Buffer buffer = new Buffer();

            buffer.appendShort((short) responseHolders.size());

            for (ResponseHolder response : responseHolders) {

                buffer.appendShort((short) response.code);
                BufferUtils.writeString(buffer, Str.add(response.remoteAddress, "|", response.id));
                BufferUtils.writeString(buffer, response.mimeType);
                BufferUtils.writeString(buffer, response.body);

            }


            String returnEventAddress = Str.add(httpRequestResponseEventChannel(), ".", returnEventBusAddress);


            vertx.eventBus().send(returnEventAddress, buffer);
        }


    }

    protected void extractConfig() {
        JsonObject config = container.config();

        if (config.containsField(HTTP_RELAY_VERTICLE_PORT)) {
            port = config.getInteger(HTTP_RELAY_VERTICLE_PORT);
        }
        if (config.containsField(HTTP_RELAY_VERTICLE_HOST)) {
            host = config.getString(HTTP_RELAY_VERTICLE_HOST);
        }
        if (config.containsField(HTTP_RELAY_VERTICLE_MANAGE_QUEUES)) {
            manageQueues = config.getBoolean(HTTP_RELAY_VERTICLE_MANAGE_QUEUES);
        }
        if (config.containsField(HTTP_RELAY_VERTICLE_POLL_TIME)) {
            pollTime = config.getInteger(
                    HTTP_RELAY_VERTICLE_POLL_TIME);
        }
        if (config.containsField(HTTP_RELAY_VERTICLE_MAX_REQUEST_BATCHES)) {
            maxRequestBatches = config.getInteger(HTTP_RELAY_VERTICLE_MAX_REQUEST_BATCHES);
        }
        if (config.containsField(HTTP_RELAY_VERTICLE_FLUSH_INTERVAL)) {
            flushInterval = config.getInteger(HTTP_RELAY_VERTICLE_FLUSH_INTERVAL);
        }
        if (config.containsField(HTTP_RELAY_VERTICLE_REQUEST_BATCH_SIZE)) {
            requestBatchSize = config.getInteger(
                    HTTP_RELAY_VERTICLE_REQUEST_BATCH_SIZE);
        }
        if (config.containsField(HTTP_RELAY_VERTICLE_HTTP_WORKERS)) {
            httpWorkers = config.getInteger(
                    HTTP_RELAY_VERTICLE_HTTP_WORKERS);
        }
    }

    public void stop() {
        if (serviceBundle!=null) {
            serviceBundle.stop();
        }
        stop.set(true);
    }


    @Override
    public void start() {

        extractConfig();

        serverId = UUID.randomUUID().toString();
        final JsonObject httpServerConfig = createHttpConfig();


        container.deployVerticle(HttpServerWorkerVerticle.class.getName(), httpServerConfig, httpWorkers,
                result -> {
                    if (result.succeeded()) {
                        logger.info("Service Server Verticle is Launched");
                    }
                }
        );


        stop.set(false);

        configureEventBus();


        afterStart();

        vertx.setPeriodic(100, new Handler<Long>() {
            @Override
            public void handle(Long event) {


                final Set<String> keys = requestQueueMap.keySet();

                for (String key : keys) {
                    sendResponses(key, null);
                }

                idleRequests();
                idleWebSocket();
            }
        });

    }

    protected abstract void idleWebSocket();

    protected abstract void idleRequests() ;

    protected abstract void afterStart();

}

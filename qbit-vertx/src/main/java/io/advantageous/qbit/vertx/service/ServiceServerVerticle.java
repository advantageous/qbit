package io.advantageous.qbit.vertx.service;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.server.HttpRequestServerHandler;
import io.advantageous.qbit.server.WebSocketServerHandler;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.vertx.BufferUtils;
import io.advantageous.qbit.vertx.http.HttpServerVerticle;
import org.boon.Str;
import org.boon.StringScanner;
import org.boon.core.Sys;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.BeanUtils;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;
import org.boon.core.reflection.fields.FieldAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/26/15.
 */
public class ServiceServerVerticle extends Verticle {

    private final Logger logger = LoggerFactory.getLogger(ServiceServerVerticle.class);
    private final boolean debug = logger.isDebugEnabled();


    int port = 8080;
    String host = null;
    boolean manageQueues = false;

    int pollTime;
    int requestBatchSize = 20;

    int flushInterval = 100;
    int maxRequestBatches = -1;

    int httpWorkers = 4;

    String handerClassName = null;


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


    protected int timeoutInSeconds = 30;
    protected ProtocolEncoder encoder;
    protected JsonMapper jsonMapper;
    protected ProtocolParser parser;
    protected Object context = Sys.contextToHold();


    protected WebSocketServerHandler webSocketHandler;
    protected HttpRequestServerHandler httpRequestServerHandler;



    private AtomicBoolean stop = new AtomicBoolean();


    Consumer<ServiceBundle> beforeCallbackHandler;

    ServiceBundle serviceBundle;
    String bundleUri = "/services";
    String serverId;


    private String httpReceiveRequestEventChannel = null;


    private String httpRequestResponseEventChannel = null;

    private String httpReceiveWebSocketEventChannel = null;
    private String webSocketReturnChannel;


    public String webSocketReturnChannel() {
        if (webSocketReturnChannel==null) {
            webSocketReturnChannel = Str.add(serverId, ".", BeforeStartHandler.HTTP_WEB_SOCKET_RESPONSE_EVENT);
        }

        return webSocketReturnChannel;
    }

    public String httpRequestResponseEventChannel() {
        if (httpRequestResponseEventChannel==null) {
            httpRequestResponseEventChannel = Str.add(serverId, ".", BeforeStartHandler.HTTP_REQUEST_RESPONSE_EVENT);
        }

        return httpRequestResponseEventChannel;
    }

    public String httpReceiveRequestEventChannel() {
        if (httpReceiveRequestEventChannel==null) {
            httpReceiveRequestEventChannel = Str.add(serverId, ".", BeforeStartHandler.HTTP_REQUEST_RECEIVE_EVENT);
        }
        return httpReceiveRequestEventChannel;
    }


    public String httpReceiveWebSocketEventChannel() {
        if (httpReceiveWebSocketEventChannel==null) {
            httpReceiveWebSocketEventChannel = Str.add(serverId, ".", BeforeStartHandler.HTTP_WEB_SOCKET_RECEIVE_EVENT);
        }
        return httpReceiveWebSocketEventChannel;
    }

    @Override
    public void start() {


        jsonMapper = QBit.factory().createJsonMapper();
        encoder = QBit.factory().createEncoder();
        parser = QBit.factory().createProtocolParser();



        extractConfig();


        this.serverId = UUID.randomUUID().toString();




        JsonObject jsonObject = new JsonObject();
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_PORT, this.port);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_FLUSH_INTERVAL, this.flushInterval);
        jsonObject.putBoolean(HttpServerVerticle.HTTP_SERVER_VERTICLE_MANAGE_QUEUES, this.manageQueues);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES, this.maxRequestBatches);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_POLL_TIME, this.pollTime);
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_VERTICLE_HOST, this.host);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE, this.requestBatchSize);
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_HANDLER, BeforeStartHandler.class.getName());
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_ID, serverId);





        container.deployVerticle(HttpServerVerticle.class.getName(), jsonObject,  httpWorkers,
                new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> stringAsyncResult) {
                        if (stringAsyncResult.succeeded()) {
                            puts("Launched verticle");
                        }
                    }
                }
        );

        try {
            beforeCallbackHandler = (Consumer<ServiceBundle>) Class.forName(handerClassName).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        serviceBundle = new ServiceBundleBuilder().setAddress(bundleUri).setPollTime(pollTime).setRequestBatchSize(requestBatchSize).build();



        webSocketHandler = new WebSocketServerHandler(requestBatchSize, serviceBundle, encoder, parser);
        httpRequestServerHandler = new HttpRequestServerHandler(this.timeoutInSeconds, this.encoder, this.parser, serviceBundle, jsonMapper, 1_000_000);

        final Map<String, FieldAccess> fieldMap = ClassMeta
                .classMeta(beforeCallbackHandler.getClass())
                .fieldMap();

        if (fieldMap.containsKey("vertx")) {
            BeanUtils.setPropertyValue(beforeCallbackHandler, vertx, "vertx");
        }

        beforeCallbackHandler.accept(
                new ServiceBundle() {
                    @Override
                    public String address() {
                        return serviceBundle.address();
                    }

                    @Override
                    public void addService(String address, Object object) {

                        serviceBundle.addService(address, object);
                    }

                    @Override
                    public void addService(Object service) {

                        httpRequestServerHandler.addRestSupportFor(service.getClass(), serviceBundle.address());
                        serviceBundle.addService(service);
                    }

                    @Override
                    public Queue<Response<Object>> responses() {
                        return serviceBundle.responses();
                    }

                    @Override
                    public void flushSends() {

                        serviceBundle.flushSends();
                    }

                    @Override
                    public void stop() {
                    }

                    @Override
                    public List<String> endPoints() {
                        return serviceBundle.endPoints();
                    }

                    @Override
                    public void startReturnHandlerProcessor(ReceiveQueueListener<Response<Object>> listener) {


                    }

                    @Override
                    public void startReturnHandlerProcessor() {

                    }

                    @Override
                    public <T> T createLocalProxy(Class<T> serviceInterface, String myService) {
                        return null;
                    }

                    @Override
                    public void call(MethodCall<Object> methodCall) {

                    }

                    @Override
                    public void call(List<MethodCall<Object>> methodCalls) {

                    }

                    @Override
                    public void flush() {

                    }
                }
                );



        vertx.eventBus().registerHandler(httpReceiveRequestEventChannel(), new Handler<Message>() {
                    @Override
                    public void handle(Message event) {

                        Message<Buffer> bufferMessage = event;
                        final Buffer request = bufferMessage.body();
                        handleHttpRequest(request);

                    }
                }
                );


        vertx.eventBus().registerHandler(httpReceiveWebSocketEventChannel(), new Handler<Message>() {
                    @Override
                    public void handle(Message event) {
                        Message<Buffer> bufferMessage = event;
                        final Buffer request = bufferMessage.body();
                        handleWebSocketMessage(request);

                    }
                }
        );


        stop.set(false);



        startResponseQueueListener();

    }

    private void handleWebSocketMessage(Buffer buffer) {


        int [] location = new int[]{0};


        String returnEventBusAddress = BufferUtils.readString(buffer, location);
        String id = BufferUtils.readString(buffer, location);
        long messageId = Long.parseLong(id);
        String ts = BufferUtils.readString(buffer, location);
        long timestamp = Long.parseLong(ts);
        String uri = BufferUtils.readString(buffer, location);
        String remoteAddress = BufferUtils.readString(buffer, location);
        String body = BufferUtils.readString(buffer, location);




        final WebSocketMessage message =
                new WebSocketMessageBuilder()
                        .setMessage(body)
                        .setRemoteAddress(remoteAddress)
                        .setUri(uri)
                        .setMessageId(messageId)
                        .setTimestamp(timestamp)
                        .setSender(new WebSocketSender() {
                    @Override
                    public void send(String message) {



                        Buffer buffer = new Buffer();


                        BufferUtils.writeString(buffer, remoteAddress);
                        BufferUtils.writeString(buffer, body);

                        String returnEventAddress = Str.add(webSocketReturnChannel(), ".", returnEventBusAddress);


                        puts("SENDING WS ", returnEventAddress);
                        vertx.eventBus().send(returnEventAddress, buffer);

                    }
                }).build();


        webSocketHandler.handleWebSocketCall(message);


    }

    private void handleHttpRequest(Buffer buffer) {


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

        final HttpRequest request =
                new HttpRequestBuilder().setBody(body).setUri(uri).setMethod(method)
                        .setRemoteAddress(remoteAddress).setParams(params)
                        .setHeaders(headers).setId(messageId)
                        .setTextResponse(new HttpTextResponse() {
                            @Override
                            public void response(int code, String mimeType, String body) {


                                Buffer buffer = new Buffer();

                                buffer.appendShort((short)code);

                                BufferUtils.writeString(buffer, Str.add(remoteAddress, "|", id ));
                                BufferUtils.writeString(buffer, mimeType);
                                BufferUtils.writeString(buffer, body);

                                String returnEventAddress = Str.add(httpRequestResponseEventChannel(), ".", returnEventBusAddress);

                                puts("SENDING ", returnEventAddress);

                                vertx.eventBus().send(returnEventAddress, buffer);

                            }
                        })
                        .build();



        httpRequestServerHandler.handleRestCall(request);



    }

    private void extractConfig() {
        JsonObject config = container.config();

        if (config.containsField(SERVICE_SERVER_VERTICLE_HANDLER)) {
            handerClassName = config.getString(SERVICE_SERVER_VERTICLE_HANDLER);
        }

        if (config.containsField(SERVICE_SERVER_VERTICLE_BUNDLE_URI)) {
            bundleUri = config.getString(SERVICE_SERVER_VERTICLE_BUNDLE_URI);
        }



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


    /**
     * Sets up the response queue listener so we can send responses
     * to HTTP / WebSocket end points.
     */
    private void startResponseQueueListener() {
        serviceBundle.startReturnHandlerProcessor(createResponseQueueListener());
    }

    /**
     * Creates the queue listener for method call responses from the client bundle.
     *
     * @return the response queue listener to handle the responses to method calls.
     */
    private ReceiveQueueListener<Response<Object>> createResponseQueueListener() {
        return new ReceiveQueueListener<Response<Object>>() {

            List<Response<Object>> responseBatch = new ArrayList<>();

            @Override
            public void receive(final Response<Object> response) {

                responseBatch.add(response);

                if (responseBatch.size() >= requestBatchSize) {
                    handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                    responseBatch.clear();
                }

            }


            @Override
            public void limit() {



                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();

                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }

            @Override
            public void empty() {
                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();

                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();

            }

            @Override
            public void idle() {

                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();


                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }
        };
    }


    /**
     * Handle a response from the server.
     *
     * @param responses responses
     */
    private void handleResponseFromServiceBundle(final List<Response<Object>> responses) {



        for (Response<Object> response : responses) {

            final Request<Object> request = response.request();

            if (request instanceof MethodCall) {


                final MethodCall<Object> methodCall = ((MethodCall<Object>) request);
                final Request<Object> originatingRequest = methodCall.originatingRequest();

                handleResponseFromServiceBundle(response, originatingRequest);

            }
        }

    }

    private void handleResponseFromServiceBundle(final Response<Object> response, final Request<Object> originatingRequest) {

        /* TODO Since websockets can be for many requests, we need a counter of some sort. */

        if (originatingRequest instanceof HttpRequest) {

            if (originatingRequest.isHandled()) {
                return; // the operation timed out
            }
            originatingRequest.handled(); //Let others know that it is handled.

            httpRequestServerHandler.handleResponseFromServiceToHttpResponse(response, (HttpRequest) originatingRequest);
        } else if (originatingRequest instanceof WebSocketMessage) {
            originatingRequest.handled(); //Let others know that it is handled.

            webSocketHandler.handleResponseFromServiceBundleToWebSocketSender(response, (WebSocketMessage) originatingRequest);
        } else {

            throw new IllegalStateException("Unknown response " + response);
        }
    }

}

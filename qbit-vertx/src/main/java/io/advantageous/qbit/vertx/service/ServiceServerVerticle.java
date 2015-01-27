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
    protected HttpServer httpServer;
    protected JsonMapper jsonMapper;
    protected ProtocolParser parser;
    protected Object context = Sys.contextToHold();
    protected volatile long lastTimeoutCheckTime = 0;


    protected final long flushResponseInterval = 200;
    protected volatile long flushResponseLastTimestamp = 0;


    private Set<String> getMethodURIs = new LinkedHashSet<>();
    private Set<String> postMethodURIs = new LinkedHashSet<>();
    private Set<String> objectNameAddressURIWithVoidReturn = new LinkedHashSet<>();
    private Set<String> getMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private Set<String> postMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private BlockingQueue<Request<Object>> outstandingRequests;
    private AtomicBoolean stop = new AtomicBoolean();

    private final Map<String, WebSocketDelegate> webSocketDelegateMap = new ConcurrentHashMap<>(100);


    class WebSocketDelegate {
        final int requestBatchSize;

        final BlockingQueue<Response<Object>> outputMessages;

        final WebSocketMessage serverWebSocket;

        volatile long lastSend;

        private WebSocketDelegate(int requestBatchSize, WebSocketMessage serverWebSocket) {
            this.requestBatchSize = requestBatchSize;
            outputMessages = new ArrayBlockingQueue<>(requestBatchSize);
            this.serverWebSocket = serverWebSocket;
        }


        public void send(final Response<Object> message) {

            if (!outputMessages.offer(message)) {
                buildAndSendMessages(message, Timer.timer().now());
            }
//            } else {
//                uts("OUTPUT QUEUE", outputMessages.size());
//            }
        }

        private void buildAndSendMessages(final Response<Object> message, long now) {

            if (outputMessages.size() == 0 && message == null) {
                return;
            }

            List<Response<Object>> messages = new ArrayList<>(outputMessages.size() + 1);

            //uts("*** SENDING MESSAGES buildAndSendMessages", outputMessages.size() + 1);

            Response<Object> currentMessage = outputMessages.poll();

            while (currentMessage !=null) {

                messages.add(currentMessage);
                currentMessage = outputMessages.poll();

            }

            if (message !=null) {
                messages.add(message);
            }


            final String textMessage = encoder.encodeAsString((Collection<io.advantageous.qbit.message.Message<Object>>) (Object) messages);

            serverWebSocket.getSender().send(textMessage);

            lastSend = now;


            //uts("*** JUST SENT buildAndSendMessages", messages.size(), lastSend);
        }


    }




    Consumer<ServiceBundle> beforeCallbackHandler;

    ServiceBundle serviceBundle;
    String bundleUri = "/services";
    String serverId;


    private String httpReceiveRequestEventChannel = null;


    private String httpRequestResponseEventChannel = null;

    private String httpReceiveWebSocketEventChannel = null;


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

        outstandingRequests = new ArrayBlockingQueue<Request<Object>>(1_000_000);


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

                        addRestSupportFor(service.getClass(), serviceBundle.address());
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

                                puts("SENDING", returnEventAddress);

                                vertx.eventBus().send(returnEventAddress, buffer);

                            }
                        })
                        .build();



        handleRestCall(request);



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




    private void handleWebSocketClose(final WebSocketMessage webSocketMessage) {
        webSocketDelegateMap.remove(webSocketMessage.getRemoteAddress());
    }

    /**
     * All WebSocket calls come through here.
     *
     * @param webSocketMessage
     */
    public void handleWebSocketCall(final WebSocketMessage webSocketMessage) {

        if (GlobalConstants.DEBUG) logger.info("WebSocket message: " + webSocketMessage);




        WebSocketDelegate webSocketDelegate = webSocketDelegateMap.get(webSocketMessage.getRemoteAddress());

        if (webSocketDelegate == null) {
            webSocketDelegate = new WebSocketDelegate(requestBatchSize, webSocketMessage);
            webSocketDelegateMap.put(webSocketMessage.getRemoteAddress(), webSocketDelegate);
        }



        final List<MethodCall<Object>> methodCallListToBeParsedFromBody =
                createMethodCallListToBeParsedFromBody(webSocketMessage.getRemoteAddress(),
                        webSocketMessage.getMessage(), webSocketMessage);


        if (methodCallListToBeParsedFromBody.size() > requestBatchSize) {

            for (MethodCall<Object> methodCall : methodCallListToBeParsedFromBody) {
                serviceBundle.call(methodCall);
            }
        } else {

            serviceBundle.call(methodCallListToBeParsedFromBody);
        }


    }

    private void handleResponseFromServiceBundleToWebSocketSender(Response<Object> response, WebSocketMessage originatingRequest) {
        final WebSocketMessage webSocketMessage = originatingRequest;
        try {

            //uts("handle WebSocket response", webSocketMessage.getRemoteAddress());
            final WebSocketDelegate webSocketDelegate = this.webSocketDelegateMap.get(webSocketMessage.getRemoteAddress());

            if (webSocketDelegate == null) {
                String responseAsText = encoder.encodeAsString(response);


                webSocketMessage.getSender().send(responseAsText);
            } else {
                webSocketDelegate.send(response);
            }
        } catch (Exception ex) {
            logger.warn("websocket unable to send response", ex);
        }
    }

    private void handleResponseFromServiceToHttpResponse(Response<Object> response, HttpRequest originatingRequest) {
        final HttpRequest httpRequest = originatingRequest;

        if (response.wasErrors()) {
            writeResponse(httpRequest.getResponse(), 500, "application/json", jsonMapper.toJson(response.body()), httpRequest.getHeaders());
        } else {
            writeResponse(httpRequest.getResponse(), 200, "application/json", jsonMapper.toJson(response.body()), httpRequest.getHeaders());
        }
    }



    public void stop() {

        if (serviceBundle!=null) {

            serviceBundle.stop();
        }

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

                checkTimeoutsForRequests();
                checkResponseBatchSend();
            }

            @Override
            public void empty() {
                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();

                checkTimeoutsForRequests();
                checkResponseBatchSend();

            }

            @Override
            public void idle() {

                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();


                checkTimeoutsForRequests();
                checkResponseBatchSend();
            }
        };
    }


    private void checkResponseBatchSend() {


        final long now = Timer.timer().now();


        long duration = now - flushResponseLastTimestamp;

        if (duration > flushResponseInterval) {
            flushResponseLastTimestamp = now;

            final Collection<WebSocketDelegate> values = this.webSocketDelegateMap.values();
            for (WebSocketDelegate ws : values) {

                long dur = now - ws.lastSend;

                if (dur > flushResponseInterval) {
                    ws.buildAndSendMessages(null, now);
                }
            }
        }


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

            handleResponseFromServiceToHttpResponse(response, (HttpRequest) originatingRequest);
        } else if (originatingRequest instanceof WebSocketMessage) {
            originatingRequest.handled(); //Let others know that it is handled.

            handleResponseFromServiceBundleToWebSocketSender(response, (WebSocketMessage) originatingRequest);
        } else {

            throw new IllegalStateException("Unknown response " + response);
        }
    }

    /**
     * Register REST and webSocket support for a class and URI.
     *
     * @param cls     class
     * @param baseURI baseURI
     */
    private void addRestSupportFor(Class cls, String baseURI) {

        if (debug) logger.debug("addRestSupportFor " + cls.getName());

        ClassMeta classMeta = ClassMeta.classMeta(cls);

        Iterable<MethodAccess> methods = classMeta.methods();

        final AnnotationData mapping = classMeta.annotation("RequestMapping");

        if (mapping != null) {


            Map<String, Object> requestMapping = mapping.getValues();


            String serviceURI = ((String[]) requestMapping.get("value"))[0];


            registerMethodsToEndPoints(baseURI, serviceURI, methods);

        } else {


            registerMethodsToEndPoints(baseURI, "/" + Str.uncapitalize(classMeta.name()),
                    methods);

        }


    }

    /**
     * Registers methods from a client class or interface to an end point
     *
     * @param baseURI    base URI
     * @param serviceURI client URI
     * @param methods    methods
     */
    private void registerMethodsToEndPoints(String baseURI, String serviceURI, Iterable<MethodAccess> methods) {
        for (MethodAccess method : methods) {
            if (!method.isPublic() || method.method().getName().contains("$")) continue;


            registerMethodToEndPoint(baseURI, serviceURI, method);

        }
    }

    /**
     * Registers a single baseURI, serviceURI and method to a GET or POST URI.
     *
     * @param baseURI    base URI
     * @param serviceURI client URI
     * @param method     method
     */
    private void registerMethodToEndPoint(final String baseURI, final String serviceURI, final MethodAccess method) {
        final AnnotationData data = method.annotation("RequestMapping");
        String methodURI;
        RequestMethod httpMethod = RequestMethod.WEB_SOCKET;
        String objectNameAddress;


        if (data != null) {
            final Map<String, Object> methodValuesForAnnotation = data.getValues();
            methodURI = extractMethodURI(methodValuesForAnnotation);

            if (methodURI==null) {
                methodURI = Str.add("/", method.name());
            }
            httpMethod = extractHttpMethod(methodValuesForAnnotation);


        } else {


            methodURI = Str.add("/", method.name());
        }


        objectNameAddress = Str.add(baseURI, serviceURI, methodURI);

        final boolean voidReturn = method.returnType() == void.class;


        if (voidReturn) {
            objectNameAddressURIWithVoidReturn.add(objectNameAddress);
            objectNameAddressURIWithVoidReturn.add(Str.add(baseURI, serviceURI, "/", method.name()));

        }


        switch (httpMethod) {
            case GET:
                getMethodURIs.add(objectNameAddress);

                getMethodURIs.add(objectNameAddress.toLowerCase());
                if (voidReturn) {
                    getMethodURIsWithVoidReturn.add(objectNameAddress);

                    getMethodURIsWithVoidReturn.add(Str.add(baseURI, serviceURI, "/", method.name()));


                }
                break;
            case POST:
                postMethodURIs.add(objectNameAddress);
                postMethodURIs.add(objectNameAddress.toLowerCase());
                if (voidReturn) {
                    postMethodURIsWithVoidReturn.add(objectNameAddress);
                    postMethodURIsWithVoidReturn.add(Str.add(baseURI, serviceURI, "/", method.name()));

                }
                break;
        }
    }

    /**
     * gets the HTTP method from an annotation.
     *
     * @param methodValuesForAnnotation methods
     * @return request method
     */
    private RequestMethod extractHttpMethod(Map<String, Object> methodValuesForAnnotation) {
        RequestMethod httpMethod = null;

        RequestMethod[] httpMethods = (RequestMethod[]) methodValuesForAnnotation.get("method");

        if (httpMethods != null && httpMethods.length > 0) {
            httpMethod = httpMethods[0];

        }

        httpMethod = httpMethod == null ? RequestMethod.GET : httpMethod;

        return httpMethod;
    }

    /**
     * Gets the URI from a method annotation
     *
     * @param methodValuesForAnnotation
     * @return URI
     */
    private String extractMethodURI(Map<String, Object> methodValuesForAnnotation) {


        String[] values = (String[]) methodValuesForAnnotation.get("value");

        if (values == null || values.length ==0) {
            return null;
        }
        String methodURI = values[0];
        if (methodURI.contains("{")) {
            methodURI = StringScanner.split(methodURI, '{', 1)[0];
        }

        return methodURI;
    }

    /**
     * Add a request to the timeout queue. Server checks for timeouts when it is idle or when
     * the max outstanding outstandingRequests is met.
     *
     * @param request request.
     */
    private boolean addRequestToCheckForTimeouts(final Request<Object> request) {

        return outstandingRequests.offer(request);
    }

    /**
     *
     */
    private void checkTimeoutsForRequests() {

        final long now = Timer.timer().now();

        if (!(now - lastTimeoutCheckTime > 500)) {
            return;
        }




        lastTimeoutCheckTime = now;
        long duration;

        Request<Object> request = outstandingRequests.poll();

        List<Request<Object>> notTimedOutRequests = new ArrayList<>();

        while (request != null) {
            duration = now - request.timestamp();

            if (duration > (timeoutInSeconds * 1000)) {
                if (!request.isHandled()) {
                    handleMethodTimedOut(request);
                }
            } else {
                notTimedOutRequests.add(request);
            }
            request = outstandingRequests.poll();
        }


        /* Add the outstandingRequests that have not timed out back to the queue. */
        if (notTimedOutRequests.size() > 0) {
            outstandingRequests.addAll(notTimedOutRequests);

        }


    }

    /**
     * Handle a method timeout.
     *
     * @param request request
     */
    private void handleMethodTimedOut(final Request<Object> request) {


        if (request instanceof HttpRequest) {

            final HttpResponse httpResponse = ((HttpRequest) request).getResponse();

            try {
                httpResponse.response(408, "application/json", "\"timed out\"");
            } catch (Exception ex) {
                logger.debug("Response not marked handled and it timed out, but could not be written " + request, ex);

            }
        } else if (request instanceof WebSocketMessage) {

            final WebSocketMessage webSocketMessage = (WebSocketMessage) request;

            final WebSocketSender webSocket = webSocketMessage.getSender();

            if (webSocket != null) {

                Response<Object> response = new Response<Object>() {
                    @Override
                    public boolean wasErrors() {
                        return true;
                    }

                    @Override
                    public void body(Object body) {

                    }

                    @Override
                    public String returnAddress() {
                        return request.returnAddress();
                    }

                    @Override
                    public String address() {
                        return request.address();
                    }

                    @Override
                    public long timestamp() {
                        return request.timestamp();
                    }

                    @Override
                    public Request<Object> request() {
                        return request;
                    }

                    @Override
                    public long id() {
                        return request.id();
                    }

                    @Override
                    public Object body() {
                        return new TimeoutException("Request timed out");
                    }

                    @Override
                    public boolean isSingleton() {
                        return true;
                    }
                };
                String responseAsText = encoder.encodeAsString(response);
                webSocket.send(responseAsText);

            }
        } else {
            throw new IllegalStateException("Unexpected request type " + request);
        }
    }



    public List<MethodCall<Object>> createMethodCallListToBeParsedFromBody(String addressPrefix, Object body, Request<Object> originatingRequest) {

        List<MethodCall<Object>> methodCalls;

        if (body != null) {

            methodCalls = parser.parseMethodCallListUsingAddressPrefix(addressPrefix, body);

        } else {
            methodCalls = Collections.emptyList();

        }

        if (methodCalls == null || methodCalls.size() == 0) {

            if (originatingRequest instanceof WebSocketMessage) {
                WebSocketMessage webSocketMessage = ((WebSocketMessage) originatingRequest);

                final Response<Object> response = ResponseImpl.response(-1, Timer.timer().now(), "SYSTEM", "ERROR", "CAN'T HANDLE CALL", originatingRequest, true);
                final WebSocketSender sender = webSocketMessage.getSender();
                sender.send(encoder.encodeAsString(response));

            }

            return Collections.emptyList();
        }


        for (MethodCall<Object> methodCall : methodCalls) {
            if (methodCall instanceof MethodCallImpl) {

                MethodCallImpl method = ((MethodCallImpl) methodCall);

                method.originatingRequest(originatingRequest);
            }
        }

        return methodCalls;

    }


    private void writeResponse(HttpResponse response, int code, String mimeType, String responseString, MultiMap<String, String> headers) {

        if (response.isText()) {
            response.response(code, mimeType, responseString, headers);
        } else {
            response.response(code, mimeType, responseString.getBytes(StandardCharsets.UTF_8), headers);
        }
    }


    /**
     * All REST calls come through here.
     * Handles a REST call.
     *
     * @param request http request
     */
    public void handleRestCall(final HttpRequest request) {


        boolean knownURI = false;

        final String uri = request.getUri();


        Object args = null;

        switch (request.getMethod()) {
            case "GET":
                knownURI = getMethodURIs.contains(uri);
                if (getMethodURIsWithVoidReturn.contains(uri)) {
                    writeResponse(request.getResponse(), 200, "application/json", "\"success\"", request.getHeaders());

                } else {
                    if (!addRequestToCheckForTimeouts(request)) {

                        writeResponse(request.getResponse(), 429, "application/json", "\"too many outstanding requests\"", request.getHeaders());
                        return;
                    }
                }
                break;

            case "POST":
                knownURI = postMethodURIs.contains(uri);
                if (postMethodURIsWithVoidReturn.contains(uri)) {
                    writeResponse(request.getResponse(), 200, "application/json", "\"success\"", request.getHeaders());
                } else {
                    if (!addRequestToCheckForTimeouts(request)) {

                        writeResponse(request.getResponse(), 429, "application/json", "\"too many outstanding requests\"", request.getHeaders());
                        return;
                    }
                }
                if (!Str.isEmpty(request.getBody())) {
                    args = jsonMapper.fromJson(new String(request.getBody(), StandardCharsets.UTF_8));
                }
                break;
        }


        if (!knownURI) {
            request.handled(); //Mark the request as handled.

            writeResponse(request.getResponse(), 404, "application/json",
                    Str.add("\"No service method for URI ", request.getUri(), "\""), request.getHeaders());

            return;

        }

        final MethodCall<Object> methodCall =
                QBit.factory().createMethodCallFromHttpRequest(request, args);


        if (GlobalConstants.DEBUG) {
            logger.info("Handle REST Call for MethodCall " + methodCall);
        }
        serviceBundle.call(methodCall);

    }
}

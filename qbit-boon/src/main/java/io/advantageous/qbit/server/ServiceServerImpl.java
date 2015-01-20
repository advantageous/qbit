package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import org.boon.Str;
import org.boon.StringScanner;
import org.boon.core.Sys;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by rhightower on 10/22/14.
 *
 * @author rhightower
 */
public class ServiceServerImpl implements ServiceServer {


    private final Logger logger = LoggerFactory.getLogger(ServiceServerImpl.class);
    private final boolean debug = logger.isDebugEnabled();
    protected int timeoutInSeconds = 30;
    protected ProtocolEncoder encoder;
    protected HttpServer httpServer;
    protected ServiceBundle serviceBundle;
    protected JsonMapper jsonMapper;
    protected ProtocolParser parser;
    Object context = Sys.contextToHold();
    long lastTimeoutCheckTime = 0;
    private Set<String> getMethodURIs = new LinkedHashSet<>();
    private Set<String> postMethodURIs = new LinkedHashSet<>();
    private Set<String> objectNameAddressURIWithVoidReturn = new LinkedHashSet<>();
    private Set<String> getMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private Set<String> postMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private BlockingQueue<Request<Object>> outstandingRequests;
    private AtomicBoolean stop = new AtomicBoolean();


    public ServiceServerImpl(final HttpServer httpServer,
                             final ProtocolEncoder encoder,
                             final ProtocolParser parser,
                             final ServiceBundle serviceBundle,
                             final JsonMapper jsonMapper,
                             final int timeOutInSeconds,
                             final int numberOfOutstandingRequests) {
        this.encoder = encoder;
        this.parser = parser;
        this.httpServer = httpServer;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
        this.timeoutInSeconds = timeOutInSeconds;
        this.outstandingRequests = new ArrayBlockingQueue<>(numberOfOutstandingRequests);
    }


    private void writeResponse(HttpResponse response, int code, String mimeType, String responseString) {

        writeResponse(response, code, mimeType, responseString, MultiMap.EMPTY);
    }

    private void writeResponse(HttpResponse response, int code, String mimeType, String responseString, MultiMap<String, String> headers) {

        if (response.isText()) {
            response.response(code, mimeType, responseString);
        } else {
            response.response(code, mimeType, responseString.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * All REST calls come through here.
     * Handles a REST call.
     *
     * @param request http request
     */
    private void handleRestCall(final HttpRequest request) {


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

        }

        final MethodCall<Object> methodCall =
                QBit.factory().createMethodCallFromHttpRequest(request, args);


        if (GlobalConstants.DEBUG) {
            logger.info("Handle REST Call for MethodCall " + methodCall);
        }
        serviceBundle.call(methodCall);

    }

    public List<MethodCall<Object>> createMethodCallListToBeParsedFromBody(String addressPrefix, Object body, Request<Object> originatingRequest) {

        List<MethodCall<Object>> methodCalls;

        if (body != null) {

            methodCalls = parser.parseMethodCallListUsingAddressPrefix(addressPrefix, body);

        } else {
            methodCalls = Collections.emptyList();

        }

        if (methodCalls == null) {

            if (originatingRequest instanceof WebSocketMessage) {
                WebSocketMessage webSocketMessage = ((WebSocketMessage) originatingRequest);

                final Response<Object> response = ResponseImpl.response(-1, Timer.timer().now(), "SYSTEM", "ERROR", "CAN'T HANDLE CALL", originatingRequest, true);
                final WebsSocketSender sender = webSocketMessage.getSender();
                sender.send(encoder.encodeAsString(response));

            }

            return Collections.emptyList();
        }


        for (MethodCall<Object> methodCall : methodCalls) {
            if (methodCall instanceof MethodCallImpl) {
                MethodCallImpl impl = ((MethodCallImpl) methodCall);
                impl.originatingRequest(originatingRequest);
            }
        }

        return methodCalls;

    }

    /**
     * All WebSocket calls come through here.
     *
     * @param webSocketMessage
     */
    private void handleWebSocketCall(final WebSocketMessage webSocketMessage) {

        if (GlobalConstants.DEBUG) logger.info("websocket message: " + webSocketMessage);

        final List<MethodCall<Object>> methodCallListToBeParsedFromBody = createMethodCallListToBeParsedFromBody(webSocketMessage.getRemoteAddress(), webSocketMessage.getMessage(), webSocketMessage);


        for (MethodCall<Object> methodCall : methodCallListToBeParsedFromBody) {
            serviceBundle.call(methodCall);
        }
    }

    private void handleResponseFromServiceBundleToWebSocketSender(Response<Object> response, WebSocketMessage originatingRequest) {
        final WebSocketMessage webSocketMessage = originatingRequest;
        String responseAsText = encoder.encodeAsString(response);
        try {
            webSocketMessage.getSender().send(responseAsText);
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


    @Override
    public void start() {

        stop.set(false);

        httpServer.setHttpRequestConsumer(this::handleRestCall);
        httpServer.setWebSocketMessageConsumer(this::handleWebSocketCall);
        httpServer.start();


        startResponseQueueListener();

    }

    public void stop() {

        serviceBundle.stop();

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
            @Override
            public void receive(final Response<Object> response) {

                handleResponseFromServiceBundle(response);

            }

            @Override
            public void idle() {

                checkTimeoutsForRequests();
            }
        };
    }

    /**
     * Handle a response from the server.
     *
     * @param response response
     */
    private void handleResponseFromServiceBundle(final Response<Object> response) {

        final Request<Object> request = response.request();

        if (request instanceof MethodCall) {
            final MethodCall<Object> methodCall = ((MethodCall<Object>) request);
            final Request<Object> originatingRequest = methodCall.originatingRequest();

            handleResponseFromServiceBundle(response, originatingRequest);
        } else {
            throw new IllegalStateException("Unknown response " + response);
        }

    }

    private void handleResponseFromServiceBundle(final Response<Object> response, final Request<Object> originatingRequest) {

        if (originatingRequest.isHandled()) {
            return; // the operation timed out
        }
        originatingRequest.handled();
        if (originatingRequest instanceof HttpRequest) {
            handleResponseFromServiceToHttpResponse(response, (HttpRequest) originatingRequest);
        } else if (originatingRequest instanceof WebSocketMessage) {
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

            final WebsSocketSender webSocket = webSocketMessage.getSender();

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


    @Override
    public void initServices(Iterable services) {


        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            this.addRestSupportFor(service.getClass(), serviceBundle.address());
        }

    }

    @Override
    public void flush() {
        this.serviceBundle.flush();
    }


    @Override
    public void initServices(Object... services) {


        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            this.addRestSupportFor(service.getClass(), serviceBundle.address());
        }

    }
}

package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.ReceiveQueueManager;
import io.advantageous.qbit.queue.impl.BasicReceiveQueueManager;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.util.Timer;
import org.boon.Str;
import org.boon.StringScanner;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 10/22/14.
 *
 * @author rhightower
 */
public class Server {


    private final Logger logger = LoggerFactory.getLogger(Server.class);


    protected int timeoutInSeconds = 30;

    protected int outstandingRequestSize = 20_000_000;


    protected ProtocolEncoder encoder;
    protected HttpServer httpServer;
    protected ServiceBundle serviceBundle;
    protected JsonMapper jsonMapper;

    public Server() {
    }

    public Server(final HttpServer httpServer, final ProtocolEncoder encoder, final ServiceBundle serviceBundle,
                  final JsonMapper jsonMapper) {
        this.encoder = encoder;
        this.httpServer = httpServer;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
    }

    public Server(final HttpServer httpServer, final ProtocolEncoder encoder, final ServiceBundle serviceBundle,
                  final JsonMapper jsonMapper,
                  final int timeOutInSeconds) {
        this.encoder = encoder;
        this.httpServer = httpServer;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
        this.timeoutInSeconds = timeOutInSeconds;
    }


    private Set<String> getMethodURIs = new LinkedHashSet<>();
    private Set<String> postMethodURIs = new LinkedHashSet<>();


    private Set<String> getMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private Set<String> postMethodURIsWithVoidReturn = new LinkedHashSet<>();

    private Queue<Request<Object>> requests = new LinkedBlockingQueue<>(outstandingRequestSize);


    private final boolean debug = logger.isDebugEnabled();



    private ReceiveQueue<Response<Object>> responses;


    private AtomicBoolean stop = new AtomicBoolean();

    private ScheduledExecutorService monitor;
    private ScheduledFuture<?> future;
    private ReceiveQueueManager<Response<Object>> receiveQueueManager;



    protected void initServices(Set<Object> services) {

        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            this.addRestSupportFor(service.getClass(), serviceBundle.address());
        }
    }

    public void stop() {
        stop.set(true);

        try {
            if (future != null) {
                future.cancel(true);
            }
        } catch (Exception ex) {
            logger.warn("shutting down", ex);
        }


        try {
            if (monitor != null) {
                monitor.shutdown();
            }
        } catch (Exception ex) {
            logger.warn("shutting down", ex);
        }


    }

    /**
     * Run this server.
     */
    public void run() {
        stop.set(false);

        httpServer.setHttpRequestConsumer(this::handleRestCall);
        httpServer.setWebSocketMessageConsumer(this::handleWebSocketCall);
        httpServer.run();


        startResponseQueueListener();
    }

    /**
     * Sets up the response queue listener so we can send responses to HTTP / Websocket end points.
     */
    private void startResponseQueueListener() {
        receiveQueueManager = new BasicReceiveQueueManager<>();

        monitor = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("QBit Server");
                    return thread;
                }
        );

        responses = serviceBundle.responses();


        /*
         * Setup thread to monitor the receive queue and manage it with the queue listener.
         */
        future = monitor.scheduleAtFixedRate(() -> {
            try {
                receiveQueueManager.manageQueue(responses, createResponseQueueListener(), 50, stop);
            } catch (Exception ex) {
                logger.error(this.getClass().getName() + " Problem running queue manager", ex);
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }


    /**
     *
     * Creates the queue listener for method call responses from the service bundle.
     * @return the response queue listener to handle the responses to method calls.
     */
    private ReceiveQueueListener<Response<Object>> createResponseQueueListener() {
        return new ReceiveQueueListener<Response<Object>>() {
            @Override
            public void receive(final Response<Object> response) {

                handleResponseFromServiceBundle(response);

            }

            @Override
            public void empty() {



            }

            @Override
            public void limit() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {
                checkTimeoutsForRequests();
            }
        };
    }




    /**
     * Handle a response from the server.
     * @param response
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

    private void handleResponseFromServiceBundle(Response<Object> response, Request<Object> originatingRequest) {
        if (originatingRequest instanceof HttpRequest) {
            handleResponseFromServiceToHttpResponse(response, (HttpRequest) originatingRequest);
        } else if (originatingRequest instanceof WebSocketMessage) {
            handleResponseFromServiceBundleToWebSocketSender(response, (WebSocketMessage) originatingRequest);
        } else {

            throw new IllegalStateException("Unknown response " + response);
        }
    }

    private void handleResponseFromServiceBundleToWebSocketSender(Response<Object> response, WebSocketMessage originatingRequest) {
        final WebSocketMessage webSocketMessage = originatingRequest;
        String responseAsText = encoder.encodeAsString(response);
        webSocketMessage.getSender().send(responseAsText);
    }

    private void handleResponseFromServiceToHttpResponse(Response<Object> response, HttpRequest originatingRequest) {
        final HttpRequest httpRequest = originatingRequest;
        httpRequest.getResponse().response(200, "application/json", jsonMapper.toJson(response.body()));
    }


    /**
     * Register REST and webSocket support for a class and URI.
     * @param cls class
     * @param baseURI baseURI
     */
    private void addRestSupportFor(Class cls, String baseURI) {

        if (debug) logger.debug("addRestSupportFor " + cls.getName());

        ClassMeta classMeta = ClassMeta.classMeta(cls);

        Map<String, Object> requestMapping = classMeta.annotation("RequestMapping").getValues();

        if (requestMapping == null) {
            return;
        }

        String serviceURI = ((String[]) requestMapping.get("value"))[0];

        Iterable<MethodAccess> methods = classMeta.methods();

        registerMethodsToEndPoints(baseURI, serviceURI, methods);

    }

    /**
     * Registers methods from a service class or interface to an end point
     * @param baseURI base URI
     * @param serviceURI service URI
     * @param methods methods
     */
    private void registerMethodsToEndPoints(String baseURI, String serviceURI, Iterable<MethodAccess> methods) {
        for (MethodAccess method : methods) {
            if (!method.isPublic() || method.method().getName().contains("$")) continue;

            if (!method.hasAnnotation("RequestMapping")) {
                continue;
            }


            registerMethodToEndPoint(baseURI, serviceURI, method);

        }
    }

    /**
     * Registers a single baseURI, serviceURI and method to a GET or POST URI.
     * @param baseURI base URI
     * @param serviceURI service URI
     * @param method method
     */
    private void registerMethodToEndPoint(final String baseURI, final String serviceURI, final MethodAccess method) {
        AnnotationData data = method.annotation("RequestMapping");
        Map<String, Object> methodValuesForAnnotation = data.getValues();
        String methodURI = extractMethodURI(methodValuesForAnnotation);
        RequestMethod httpMethod = extractHttpMethod(methodValuesForAnnotation);
        String uri = Str.add(baseURI, serviceURI, methodURI);

        switch (httpMethod) {
            case GET:
                getMethodURIs.add(uri);
                if (method.returnType() == void.class) {
                    getMethodURIsWithVoidReturn.add(uri);
                }
                break;
            case POST:
                postMethodURIs.add(uri);
                if (method.returnType() == void.class) {
                    postMethodURIsWithVoidReturn.add(uri);
                }
                break;
            default:
                die("Not supported yet HTTP METHOD", httpMethod, methodURI);
        }
    }

    /**
     * gets the HTTP method from an annotation.
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
     * @param methodValuesForAnnotation
     * @return URI
     */
    private String extractMethodURI(Map<String, Object> methodValuesForAnnotation) {


        String[] values = (String[]) methodValuesForAnnotation.get("value");
        String methodURI = values[0];
        if (methodURI.contains("{")) {
            methodURI = StringScanner.split(methodURI, '{', 1)[0];
        }

        return methodURI;
    }


    /**
     * Handles a REST call.
     * @param request http request
     */
    private void handleRestCall(HttpRequest request) {

        requests.add(request);


        boolean knownURI = false;

        final String uri = request.getUri();


        Object args = null;

        switch (request.getMethod()) {
            case "GET":
                knownURI = getMethodURIs.contains(uri);
                if (getMethodURIsWithVoidReturn.contains(uri)) {
                    request.getResponse().response(200, "application/json", "\"success\"");
                }
                break;

            case "POST":
                knownURI = postMethodURIs.contains(uri);
                if (postMethodURIsWithVoidReturn.contains(uri)) {
                    request.getResponse().response(200, "application/json", "\"success\"");
                }
                if (!Str.isEmpty(request.getBody())) {
                    args = jsonMapper.fromJson(request.getBody());
                }
                break;
        }


        if (!knownURI) {
            request.getResponse().response(404, "application/json",
                    Str.add("\"No service method for URI\"", request.getUri()));

        }

        final MethodCall<Object> methodCall =
                QBit.factory().createMethodCallFromHttpRequest(request, args);



        if (GlobalConstants.DEBUG) {
            logger.info("Handle REST Call for MethodCall " + methodCall);
        }
        serviceBundle.call(methodCall);

    }


    private void handleWebSocketCall(final WebSocketMessage webSocketMessage) {

        if (GlobalConstants.DEBUG) logger.info("websocket message: " + webSocketMessage);



        final MethodCall<Object> methodCall =
                QBit.factory().createMethodCallToBeParsedFromBody(webSocketMessage.getRemoteAddress(),
                        webSocketMessage.getMessage(), webSocketMessage);


        if (GlobalConstants.DEBUG) logger.info("websocket message for method call: " + methodCall);


        addRequestToCheckForTimeouts(webSocketMessage);

        serviceBundle.call(methodCall);




    }

    /** Add a request to the timeout queue. Server checks for timeouts when it is idle or when
     * the max outstanding requests is met.
     * @param request request.
     */
    private void addRequestToCheckForTimeouts(final Request<Object> request) {

        if (!requests.offer(request)) {
            checkTimeoutsForRequests();
            requests.add(request);
        }
    }

    /**
     *
     */
    private void checkTimeoutsForRequests() {

        final long now = Timer.timer().now();
        long duration;

        Request<Object> request = requests.poll();

        List<Request<Object>> notTimedOutRequests = new ArrayList<>(requests.size());

        while (request!=null) {
            duration = now - request.timestamp();

            if (duration > (timeoutInSeconds *1000)) {
                if (!request.isHandled()) {
                    handleMethodTimedOut(request);
                }
            } else {
                notTimedOutRequests.add(request);
            }
            request = requests.poll();

        }

        /* Add the requests that have not timed out back to the queue. */
        if (notTimedOutRequests.size() > 0) {
            requests.addAll(notTimedOutRequests);
        }


    }


    /**
     * Handle a method timeout.
     * @param request request
     */
    private void handleMethodTimedOut(final Request<Object> request) {

        if (request instanceof HttpRequest) {

            final HttpResponse httpResponse = ((HttpRequest) request).getResponse();

            httpResponse.response(408, "application/json", "\"timed out\"");
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


}

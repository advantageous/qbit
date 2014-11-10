package io.advantageous.qbit.server;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.util.Timer;
import org.boon.Sets;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.boon.Boon.puts;
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

    private Set<String> getMethodURIs = new LinkedHashSet<>();
    private Set<String> postMethodURIs = new LinkedHashSet<>();
    private Set<String> objectNameAddressURIWithVoidReturn = new LinkedHashSet<>();
    private Set<String> getMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private Set<String> postMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private Queue<Request<Object>> outstandingRequests = new LinkedBlockingQueue<>(outstandingRequestSize);
    private final boolean debug = logger.isDebugEnabled();
    private AtomicBoolean stop = new AtomicBoolean();


    public Server() {
        this("localhost", 8080, "/services/");
    }


    public Server(final String host, final int port, final String uri) {
        final Factory factory = QBit.factory();
        httpServer = factory.createHttpServer(host, port);
        encoder = factory.createEncoder();
        serviceBundle = factory.createServiceBundle(uri);
        jsonMapper = factory.createJsonMapper();

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




    /**
     * All REST calls come through here.
     * Handles a REST call.
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
                    request.getResponse().response(200, "application/json", "\"success\"");
                } else {
                    addRequestToCheckForTimeouts(request);
                }
                break;

            case "POST":
                knownURI = postMethodURIs.contains(uri);
                if (postMethodURIsWithVoidReturn.contains(uri)) {
                    request.getResponse().response(200, "application/json", "\"success\"");
                } else {
                    addRequestToCheckForTimeouts(request);
                }
                if (!Str.isEmpty(request.getBody())) {
                    args = jsonMapper.fromJson(new String(request.getBody(), StandardCharsets.UTF_8));
                }
                break;
        }


        if (!knownURI) {
            request.handled(); //Mark the request as handled.
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


    /**
     * All WebSocket calls come through here.
     * @param webSocketMessage
     */
    private void handleWebSocketCall(final WebSocketMessage webSocketMessage) {

        if (GlobalConstants.DEBUG) logger.info("websocket message: " + webSocketMessage);



        final MethodCall<Object> methodCall =
                QBit.factory().createMethodCallToBeParsedFromBody(webSocketMessage.getRemoteAddress(),
                        webSocketMessage.getMessage(), webSocketMessage);

        if (GlobalConstants.DEBUG) logger.info("websocket message for method call: " + methodCall);

        if (!objectNameAddressURIWithVoidReturn.contains(methodCall.address())) {

            addRequestToCheckForTimeouts(webSocketMessage);
        }

        serviceBundle.call(methodCall);

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


    protected void initServices(final Set<Object> services) {

        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            this.addRestSupportFor(service.getClass(), serviceBundle.address());
        }
    }

    public void stop() {

        serviceBundle.stop();

    }

    /**
     * Run this server.
     * @param services services
     */
    public void run(Object... services) {

        initServices(Sets.set(services));
        stop.set(false);

        httpServer.setHttpRequestConsumer(this::handleRestCall);
        httpServer.setWebSocketMessageConsumer(this::handleWebSocketCall);
        httpServer.run();


        startResponseQueueListener();
    }

    /**
     * Sets up the response queue listener so we can send responses
     * to HTTP / WebSocket end points.
     */
    private void startResponseQueueListener() {
        serviceBundle.startReturnHandlerProcessor( createResponseQueueListener() );
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

                if (outstandingRequests.size()>0) {
                    checkTimeoutsForRequests();
                }
                Sys.sleep(100);
            }
        };
    }




    /**
     * Handle a response from the server.
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
     * @param cls class
     * @param baseURI baseURI
     */
    private void addRestSupportFor(Class cls, String baseURI) {

        if (debug) logger.debug("addRestSupportFor " + cls.getName());

        ClassMeta classMeta = ClassMeta.classMeta(cls);

        final AnnotationData mapping = classMeta.annotation("RequestMapping");

        if (mapping == null) {
            return;
        }

        Map<String, Object> requestMapping = mapping.getValues();


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
        final AnnotationData data = method.annotation("RequestMapping");
        final Map<String, Object> methodValuesForAnnotation = data.getValues();
        final String methodURI = extractMethodURI(methodValuesForAnnotation);
        final RequestMethod httpMethod = extractHttpMethod(methodValuesForAnnotation);

        final String objectNameAddress = Str.add(baseURI, serviceURI, "/", method.name());

        final boolean voidReturn = method.returnType() == void.class;



        if (voidReturn) {
            objectNameAddressURIWithVoidReturn.add(objectNameAddress);
        }


        final String uri = Str.add(baseURI, serviceURI, methodURI);


        switch (httpMethod) {
            case GET:
                getMethodURIs.add(uri);
                if (voidReturn) {
                    getMethodURIsWithVoidReturn.add(uri);
                }
                break;
            case POST:
                postMethodURIs.add(uri);
                if (voidReturn) {
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



    /** Add a request to the timeout queue. Server checks for timeouts when it is idle or when
     * the max outstanding outstandingRequests is met.
     * @param request request.
     */
    private void addRequestToCheckForTimeouts(final Request<Object> request) {

        if (!outstandingRequests.offer(request)) {
            checkTimeoutsForRequests();
            outstandingRequests.add(request);
        }
    }

    /**
     *
     */
    private void checkTimeoutsForRequests() {

        final long now = Timer.timer().now();
        long duration;

        Request<Object> request = outstandingRequests.poll();

        List<Request<Object>> notTimedOutRequests = new ArrayList<>(outstandingRequests.size());

        while (request!=null) {
            duration = now - request.timestamp();

            if (duration > (timeoutInSeconds *1000)) {
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
     * @param request request
     */
    private void handleMethodTimedOut(final Request<Object> request) {

        if (request instanceof HttpRequest) {

            final HttpResponse httpResponse = ((HttpRequest) request).getResponse();

            try {
                httpResponse.response(408, "application/json", "\"timed out\"");
            }catch (Exception ex) {
                logger.debug("Response not marked handled and it timed out, but could not be written " + request, ex);
                puts(request);

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


}

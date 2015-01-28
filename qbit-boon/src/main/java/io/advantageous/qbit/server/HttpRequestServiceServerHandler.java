package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import org.boon.Str;
import org.boon.StringScanner;
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

/**
 * Created by rhightower on 1/27/15.
 */
public class HttpRequestServiceServerHandler {

    private final Logger logger = LoggerFactory.getLogger(WebSocketServiceServerHandler.class);
    private final boolean debug = logger.isDebugEnabled();

    protected final int timeoutInSeconds;
    protected final ProtocolEncoder encoder;
    protected final ProtocolParser parser;
    protected final ServiceBundle serviceBundle;
    protected final JsonMapper jsonMapper;
    protected volatile long lastTimeoutCheckTime = 0;



    private final Set<String> getMethodURIs = new LinkedHashSet<>();
    private final Set<String> postMethodURIs = new LinkedHashSet<>();
    private final Set<String> objectNameAddressURIWithVoidReturn = new LinkedHashSet<>();
    private final Set<String> getMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private final Set<String> postMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private BlockingQueue<Request<Object>> outstandingRequests;
    private final int numberOfOutstandingRequests;



    public HttpRequestServiceServerHandler(int timeoutInSeconds, ProtocolEncoder encoder, ProtocolParser parser, ServiceBundle serviceBundle, JsonMapper jsonMapper,
                                           final int numberOfOutstandingRequests
    ) {
        this.timeoutInSeconds = timeoutInSeconds;
        this.encoder = encoder;
        this.parser = parser;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
        this.numberOfOutstandingRequests = numberOfOutstandingRequests;

        this.outstandingRequests = new ArrayBlockingQueue<>(numberOfOutstandingRequests);
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
     * Register REST and webSocket support for a class and URI.
     *
     * @param cls     class
     * @param baseURI baseURI
     */
    public void addRestSupportFor(Class cls, String baseURI) {

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
    public void checkTimeoutsForRequests() {

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

            try {
                outstandingRequests.addAll(notTimedOutRequests);
            }catch (Exception ex) {
                for (Request item : notTimedOutRequests) {
                    if (!outstandingRequests.offer(item)) {
                        logger.warn("Unable to track outstanding request " + item.address());
                    }
                }
            }

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


    private void writeResponse(HttpResponse response, int code, String mimeType, String responseString, MultiMap<String, String> headers) {

        if (response.isText()) {
            response.response(code, mimeType, responseString, headers);
        } else {
            response.response(code, mimeType, responseString.getBytes(StandardCharsets.UTF_8), headers);
        }
    }





    public void handleResponseFromServiceToHttpResponse(Response<Object> response, HttpRequest originatingRequest) {
        final HttpRequest httpRequest = originatingRequest;

        if (response.wasErrors()) {
            writeResponse(httpRequest.getResponse(), 500, "application/json", jsonMapper.toJson(response.body()), httpRequest.getHeaders());
        } else {
            writeResponse(httpRequest.getResponse(), 200, "application/json", jsonMapper.toJson(response.body()), httpRequest.getHeaders());
        }
    }


}

package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.ReceiveQueueManager;
import io.advantageous.qbit.queue.impl.BasicReceiveQueueManager;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
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

    private final MultiMap<String, MethodCall<Object>> methodCallMap = new MultiMapImpl<>();

    protected int timeoutInSeconds = 10;

    protected int methodFlushInMilliSeconds = 10;

    protected ProtocolEncoder encoder;
    protected HttpServer httpServer;
    protected ServiceBundle serviceBundle;
    protected JsonMapper jsonMapper;

    public Server() {
    }

    public Server(HttpServer httpServer, ProtocolEncoder encoder, ServiceBundle serviceBundle, JsonMapper jsonMapper) {
        this.encoder = encoder;
        this.httpServer = httpServer;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
    }

    private Set<String> getMethodURIs = new LinkedHashSet<>();
    private Set<String> postMethodURIs = new LinkedHashSet<>();


    private Set<String> getMethodURIsWithVoidReturn = new LinkedHashSet<>();
    private Set<String> postMethodURIsWithVoidReturn = new LinkedHashSet<>();

    private List<MethodCall<Object>> methodCalls = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private final boolean debug = logger.isDebugEnabled();

    private final Timer timer = Timer.timer();


    private Map<String, WebsSocketSender> webSocketMap = new ConcurrentHashMap<>();
    private Map<String, HttpResponse> responseMap = new ConcurrentHashMap<>();
    private ReceiveQueue<Response<Object>> responses;


    private AtomicBoolean stop = new AtomicBoolean();

    private ScheduledExecutorService monitor;
    private ScheduledFuture<?> future;
    private ReceiveQueueManager<Response<Object>> receiveQueueManager;

    private AtomicLong lastFlushTime = new AtomicLong();

    private AtomicLong lastCheckMethodTime = new AtomicLong();


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

    public void run() {
        startResponseQueueListener();
        httpServer.setHttpRequestConsumer(this::handleRestCall);
        httpServer.setWebSocketMessageConsumer(this::handleWebSocketCall);
        httpServer.run();
    }

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

        stop.set(false);

        future = monitor.scheduleAtFixedRate(() -> {
            try {
                receiveQueueManager.manageQueue(responses, queueListener(), 50, stop);
            } catch (Exception ex) {
                logger.error(this.getClass().getName() + " Problem running queue manager", ex);
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    private ReceiveQueueListener<Response<Object>> queueListener() {
        return new ReceiveQueueListener<Response<Object>>() {
            @Override
            public void receive(final Response<Object> response) {

                handleResponseFromServiceBundle(response);

            }

            @Override
            public void empty() {


                long now = timer.now();

                handleServiceBundleFlush(now);

            }

            @Override
            public void limit() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {


                long now = timer.now();

                handleMethodTimeout(now);
                handleServiceBundleFlush(now);
            }
        };
    }

    private void handleMethodTimeout(long now) {
        long lastTime = lastCheckMethodTime.get();

        long duration = now - lastTime;

        int timeout = (timeoutInSeconds * 1000);
        if (duration > timeout) {

            List<MethodCall<Object>> methodCallsToRemove = new ArrayList<>();

            for (MethodCall<Object> methodCall : methodCalls) {
                long timestamp = methodCall.timestamp();
                long invokeDuration = now - timestamp;
                if (invokeDuration > timeout) {
                    methodCallsToRemove.add(methodCall);

                    logger.error("Server MethodCall Timed out " + methodCall);

                    handleMethodTimedOut(methodCall);
                }
            }

            methodCallsToRemove.forEach(methodCalls::remove);
        }

    }

    private void handleMethodTimedOut(final MethodCall<Object> methodCall) {

        HttpResponse httpResponse = responseMap.get(methodCall.returnAddress());


        if (httpResponse != null) {
            httpResponse.response(408, "application/json", "\"timed out\"");
        } else {

            WebsSocketSender webSocket = webSocketMap.get(methodCall.returnAddress());
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
                        return methodCall.returnAddress();
                    }

                    @Override
                    public String address() {
                        return methodCall.address();
                    }

                    @Override
                    public long timestamp() {
                        return methodCall.timestamp();
                    }

                    @Override
                    public long id() {
                        return methodCall.id();
                    }

                    @Override
                    public Object body() {
                        return new TimeoutException("Method call timed out");
                    }

                    @Override
                    public boolean isSingleton() {
                        return true;
                    }
                };
                String responseAsText = encoder.encodeAsString(response);
                webSocket.send(responseAsText);

            } else {
                throw new IllegalStateException(
                        "Unable to find response handler to send back http or websocket response");
            }
        }
    }

    private void handleServiceBundleFlush(final long now) {

        /* Force a flush every methodFlushInMilliSeconds milliseconds. */
        if (now > lastFlushTime.get() + methodFlushInMilliSeconds) {
            serviceBundle.flushSends();
        }
    }

    private void handleResponseFromServiceBundle(Response<Object> response) {

        String address = response.returnAddress();

        removeMethodCall(response, address);

        if (debug) logger.debug("RESPONSE CALLBACK TO HTTP " + address + " " + response);

        HttpResponse httpResponse = responseMap.get(address);

        if (debug) logger.debug("RESPONSE CALLBACK TO HTTP " + address + " " + response + " " + httpResponse);


        if (httpResponse != null) {
            httpResponse.response(200, "application/json", jsonMapper.toJson(response.body()));
        } else {

            WebsSocketSender webSocket = webSocketMap.get(address);
            if (webSocket != null) {

                String responseAsText = encoder.encodeAsString(response);
                webSocket.send(responseAsText);

            } else {
                throw new IllegalStateException(
                        "Unable to find response handler to send back http or websocket response");
            }
        }
    }

    private void removeMethodCall(Response<Object> response, String address) {
        final Iterable<MethodCall<Object>> methodsForAddress = methodCallMap.getAll(address);


        MethodCall<Object> methodCallResponsePair = null;

        for (MethodCall<Object> methodCall : methodsForAddress) {


            if (response.id() == methodCall.id()) {
                methodCallResponsePair = methodCall;
                break;
            }

        }

        if (methodCallResponsePair != null) {
            methodCallMap.remove(address, methodCallResponsePair);
            methodCalls.remove(methodCallResponsePair);
        }

    }


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

    private void registerMethodsToEndPoints(String baseURI, String serviceURI, Iterable<MethodAccess> methods) {
        for (MethodAccess method : methods) {
            if (!method.isPublic() || method.method().getName().contains("$")) continue;

            if (!method.hasAnnotation("RequestMapping")) {
                continue;
            }


            registerMethodToEndPoint(baseURI, serviceURI, method);

        }
    }

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

    private RequestMethod extractHttpMethod(Map<String, Object> methodValuesForAnnotation) {
        RequestMethod httpMethod = null;

        RequestMethod[] httpMethods = (RequestMethod[]) methodValuesForAnnotation.get("method");

        if (httpMethods != null && httpMethods.length > 0) {
            httpMethod = httpMethods[0];

        }

        httpMethod = httpMethod == null ? RequestMethod.GET : httpMethod;

        return httpMethod;
    }

    private String extractMethodURI(Map<String, Object> methodValuesForAnnotation) {


        String[] values = (String[]) methodValuesForAnnotation.get("value");
        String methodURI = values[0];
        if (methodURI.contains("{")) {
            methodURI = StringScanner.split(methodURI, '{', 1)[0];
        }

        return methodURI;
    }


    private void handleRestCall(HttpRequest request) {


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

        MethodCall<Object> methodCall =
                QBit.factory().createMethodCallToBeParsedFromBody(request.getUri(),
                        request.getRemoteAddress(),
                        null,
                        null, args, request.getParams()

                );

        methodCalls.add(methodCall);
        methodCallMap.add(methodCall.returnAddress(), methodCall);


        if (GlobalConstants.DEBUG) {
            logger.info("Handle REST Call for MethodCall " + methodCall);
        }
        serviceBundle.call(methodCall);

        if (debug)
            logger.debug("RESPONSE CALLBACK TO HTTP " + methodCall.returnAddress() + " " + request.getResponse());
        responseMap.put(methodCall.returnAddress(), request.getResponse());
    }


    private void handleWebSocketCall(final WebSocketMessage webSocketMessage) {

        if (debug) logger.debug("websocket message: " + webSocketMessage);

        final MethodCall<Object> methodCall =
                QBit.factory().createMethodCallToBeParsedFromBody(webSocketMessage.getRemoteAddress(),
                        webSocketMessage.getMessage());

        serviceBundle.call(methodCall);


        webSocketMap.put(methodCall.returnAddress(), webSocketMessage.getSender());


        methodCalls.add(methodCall);
        methodCallMap.add(methodCall.returnAddress(), methodCall);

    }

}

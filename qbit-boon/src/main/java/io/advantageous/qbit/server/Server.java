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
import io.advantageous.qbit.util.Timer;
import org.boon.Str;
import org.boon.StringScanner;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.boon.Boon.puts;
import static org.boon.Boon.resource;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 10/22/14.
 * @author rhightower
 */
public class Server {

    protected  ProtocolEncoder encoder;
    protected  HttpServer httpServer;
    protected  ServiceBundle serviceBundle;
    protected  JsonMapper jsonMapper;


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


    private final Logger logger = LoggerFactory.getLogger(Server.class);

    private final Timer timer = Timer.timer();



    private Map<String, WebsSocketSender> webSocketMap = new ConcurrentHashMap<>();
    private Map<String, HttpResponse> responseMap = new ConcurrentHashMap<>();
    private ReceiveQueue<Response<Object>> responses;


    private AtomicBoolean stop = new AtomicBoolean();

    private ScheduledExecutorService monitor;
    private ScheduledFuture<?> future;
    private ReceiveQueueManager<Response<Object>> receiveQueueManager;

    private AtomicLong lastFlushTime = new AtomicLong();


    protected void initServices(Set<Object> services) {


        for (Object service : services) {
            puts(service.getClass().getName());
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

        httpServer.setHttpRequestConsumer((final HttpRequest request) -> {
            handleRestCall(request);
        });


        httpServer.setWebSocketMessageConsumer((final WebSocketMessage webSocketMessage) -> {
            handleWebSocketCall(webSocketMessage);
        });


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


                handleServiceBundleFlush();

            }

            @Override
            public void limit() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {

                handleServiceBundleFlush();
            }
        };
    }

    private void handleServiceBundleFlush() {
        long now = timer.now();

        /* Force a flush every 10 milliseconds. */
        if (now > lastFlushTime.get() + 10L) {
            serviceBundle.flushSends();
        }
    }

    private void handleResponseFromServiceBundle(Response<Object> response) {

        String address = response.returnAddress();


        puts("RESPONSE CALLBACK TO HTTP ", address, response);

        HttpResponse httpResponse = responseMap.get(address);

        puts("RESPONSE CALLBACK TO HTTP ", address, response, httpResponse);


        if (httpResponse != null) {
            httpResponse.response(200, "application/json", jsonMapper.toJson(response.body()));
        } else {

            WebsSocketSender webSocket = webSocketMap.get(address);
            if (webSocket != null) {

                String responseAsText = encoder.encodeAsString(response);
                webSocket.send(responseAsText);

            } else {
                throw new IllegalStateException("Unable to find response handler to send back http or websocket response");
            }
        }
    }


    private void addRestSupportFor(Class cls, String baseURI) {

        System.out.println("addRestSupportFor " + cls.getName());

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

    private void registerMethodToEndPoint(String baseURI, String serviceURI, MethodAccess method) {
        AnnotationData data = method.annotation("RequestMapping");
        Map<String, Object> methodValuesForAnnotation = data.getValues();

        if (data == null) return;

        String methodURI = extractMethodURI(methodValuesForAnnotation);

        RequestMethod httpMethod = extractHttpMethod(methodValuesForAnnotation);

        switch (httpMethod) {
            case GET:
                getMethodURIs.add(Str.add(baseURI, serviceURI, methodURI));
                break;
            case POST:
                postMethodURIs.add(Str.add(baseURI, serviceURI, methodURI));
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


        Object args =  null;

        switch (request.getMethod()) {
            case "GET":
                knownURI = getMethodURIs.contains(uri);
                break;

            case "POST":
                knownURI = postMethodURIs.contains(uri);
                args = jsonMapper.fromJson(request.getBody());
                break;
        }


        if (!knownURI) {
            request.getResponse().response(404, "application/json", Str.add("No service method for URI ", request.getUri()));

        }


        MethodCall<Object> methodCall =
                QBit.factory().createMethodCallToBeParsedFromBody(request.getUri(),
                        request.getRemoteAddress(),
                        null,
                        null, args, request.getParams()

                );

        if (GlobalConstants.DEBUG) {
            logger.info("Handle REST Call for MethodCall " + methodCall);
        }
        serviceBundle.call(methodCall);

        puts("RESPONSE CALLBACK TO HTTP", methodCall.returnAddress(), request.getResponse());
        responseMap.put(methodCall.returnAddress(), request.getResponse());
    }


    private void handleWebSocketCall(final WebSocketMessage webSocketMessage) {


        puts(webSocketMessage);

        final MethodCall<Object> methodCall = QBit.factory().createMethodCallToBeParsedFromBody(webSocketMessage.getRemoteAddress(),
                webSocketMessage.getMessage());

        serviceBundle.call(methodCall);


        webSocketMap.put(methodCall.returnAddress(), webSocketMessage.getSender());

    }

}

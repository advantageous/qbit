package io.advantageous.qbit.server;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.*;
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
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 10/22/14.
 */
public class Server {

    protected ProtocolEncoder encoder;
    protected HttpServer httpServer;
    protected ServiceBundle serviceBundle;

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

        serviceBundle.startReturnHandlerProcessor();


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

        HttpResponse httpResponse = responseMap.get(address);

        String responseAsText = encoder.encodeAsString(response);

        if (httpResponse != null) {
            httpResponse.response(200, "application/json", responseAsText);
        } else {

            WebsSocketSender webSocket = webSocketMap.get(address);
            if (webSocket != null) {
                webSocket.send(responseAsText);

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


        puts(request);

        boolean knownURI = false;

        String uri = request.getUri();


        switch (request.getMethod()) {
            case "GET":
                knownURI = getMethodURIs.contains(uri);
                break;

            case "POST":
                knownURI = postMethodURIs.contains(uri);
                break;
        }


        if (!knownURI) {
            request.getResponse().response(404, "application/json", Str.add("No service method for URI ", request.getUri()));

        }

        MethodCall<Object> methodCall =
                QBit.factory().createMethodCallToBeParsedFromBody(request.getUri(),
                        request.getRemoteAddress(),
                        null,
                        null, request.getBody(), request.getParams()

                );

        puts("RETURN ADDRESS", methodCall.returnAddress());
        serviceBundle.call(methodCall);

        responseMap.put(request.getRemoteAddress(), request.getResponse());
    }


    private void handleWebSocketCall(final WebSocketMessage webSocketMessage) {


        puts(webSocketMessage);

        final MethodCall<Object> methodCall = QBit.factory().createMethodCallToBeParsedFromBody(webSocketMessage.getRemoteAddress(),
                webSocketMessage.getMessage());

        serviceBundle.call(methodCall);


        webSocketMap.put(methodCall.returnAddress(), webSocketMessage.getSender());

    }

}

package io.advantageous.qbit.vertx.service;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.server.HttpRequestServiceServerHandler;
import io.advantageous.qbit.server.WebSocketServiceServerHandler;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.vertx.http.verticle.BaseHttpRelay;
import org.boon.core.reflection.BeanUtils;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.fields.FieldAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by rhightower on 1/26/15.
 */
public class ServiceServerVerticle extends BaseHttpRelay {


    private final Logger logger = LoggerFactory.getLogger(ServiceServerVerticle.class);
    private final boolean debug = logger.isDebugEnabled();

    public static final String SERVICE_SERVER_VERTICLE_HANDLER = "ServiceServerVerticle.handler";
    public static final String SERVICE_SERVER_VERTICLE_BUNDLE_URI = "ServiceServerVerticle.bundleUri";



    protected WebSocketServiceServerHandler webSocketHandler;
    protected HttpRequestServiceServerHandler httpRequestServerHandler;
    private Consumer<ServiceBundle> beforeCallbackHandler;
    private String bundleUri = "/services";

    private String handlerClassName = null;
    protected ProtocolEncoder encoder;
    protected JsonMapper jsonMapper;
    protected ProtocolParser parser;


    @Override
    protected void extractConfig() {
        super.extractConfig();
        if (container.config().containsField(SERVICE_SERVER_VERTICLE_BUNDLE_URI)) {
            bundleUri = container.config().getString(SERVICE_SERVER_VERTICLE_BUNDLE_URI);
        }
        if (container.config().containsField(SERVICE_SERVER_VERTICLE_HANDLER)) {
            handlerClassName = container.config().getString(SERVICE_SERVER_VERTICLE_HANDLER);
        }
    }

    @Override
    protected void idleWebSocket() {
        webSocketHandler.webSocketQueueIdle(null);
    }

    @Override
    protected void idleRequests() {

        httpRequestServerHandler.httpRequestQueueIdle(null);
    }


    @Override
    protected void afterStart() {

        jsonMapper = QBit.factory().createJsonMapper();
        encoder = QBit.factory().createEncoder();
        parser = QBit.factory().createProtocolParser();
        serviceBundle = new ServiceBundleBuilder().setAddress(bundleUri).setPollTime(pollTime).setRequestBatchSize(requestBatchSize).build();
        webSocketHandler = new WebSocketServiceServerHandler(requestBatchSize, serviceBundle, encoder, parser);
        httpRequestServerHandler = new HttpRequestServiceServerHandler(this.timeoutInSeconds, this.encoder, this.parser, serviceBundle, jsonMapper, 1_000_000);


        configureBeforeStartCallback();
        startResponseQueueListener();


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
                harvestMessages();
            }

            private void harvestMessages() {


                if (responseBatch.size() > 0) {
                    handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                    responseBatch.clear();
                }
                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }

            @Override
            public void empty() {
                harvestMessages();

            }

            @Override
            public void idle() {

                harvestMessages();
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


    protected void configureBeforeStartCallback() {
        try {
            beforeCallbackHandler = (Consumer<ServiceBundle>) Class.forName(handlerClassName).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

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
                    public SendQueue<MethodCall<Object>> methodSendQueue() {
                        return null;
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
    }


    @Override
    protected void handleWebSocketClosed(WebSocketMessage webSocketMessage) {

        webSocketHandler.handleWebSocketClose(webSocketMessage);
    }

    public void handleWebSocketMessage(WebSocketMessage message) {
        webSocketHandler.handleWebSocketCall(message);

    }

    public void handleHttpRequest(HttpRequest httpRequest) {
        httpRequestServerHandler.handleRestCall(httpRequest);

    }

}
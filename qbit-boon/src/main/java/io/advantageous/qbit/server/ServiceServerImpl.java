package io.advantageous.qbit.server;

import io.advantageous.qbit.http.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.*;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import org.boon.core.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by rhightower on 10/22/14.
 *
 * @author rhightower
 */
public class ServiceServerImpl implements ServiceServer {


    private final Logger logger = LoggerFactory.getLogger(ServiceServerImpl.class);
    private final boolean debug = logger.isDebugEnabled();



    protected WebSocketServiceServerHandler webSocketHandler;
    protected HttpRequestServiceServerHandler httpRequestServerHandler;

    protected int timeoutInSeconds = 30;
    protected final int batchSize;
    protected ProtocolEncoder encoder;
    protected HttpServer httpServer;
    protected ServiceBundle serviceBundle;
    protected JsonMapper jsonMapper;
    protected ProtocolParser parser;
    protected Object context = Sys.contextToHold();




    private AtomicBoolean stop = new AtomicBoolean();




    public ServiceServerImpl(final HttpServer httpServer,
                             final ProtocolEncoder encoder,
                             final ProtocolParser parser,
                             final ServiceBundle serviceBundle,
                             final JsonMapper jsonMapper,
                             final int timeOutInSeconds,
                             final int numberOfOutstandingRequests,
                             final int batchSize) {
        this.encoder = encoder;
        this.parser = parser;
        this.httpServer = httpServer;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
        this.timeoutInSeconds = timeOutInSeconds;
        this.batchSize = batchSize;

        webSocketHandler = new WebSocketServiceServerHandler(batchSize, serviceBundle, encoder, parser);
        httpRequestServerHandler = new HttpRequestServiceServerHandler(this.timeoutInSeconds, this.encoder, this.parser, serviceBundle, jsonMapper, numberOfOutstandingRequests);
    }




    @Override
    public void start() {

        stop.set(false);

        httpServer.setHttpRequestConsumer(httpRequestServerHandler::handleRestCall);
        httpServer.setWebSocketMessageConsumer(webSocketHandler::handleWebSocketCall);
        httpServer.setWebSocketCloseConsumer(webSocketHandler::handleWebSocketClose);
        httpServer.setHttpRequestsIdleConsumer(httpRequestServerHandler::httpRequestQueueIdle);

        httpServer.setWebSocketIdleConsume(webSocketHandler::webSocketQueueIdle);
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

            List<Response<Object>> responseBatch = new ArrayList<>();

            @Override
            public void receive(final Response<Object> response) {

                responseBatch.add(response);

                if (responseBatch.size() >= batchSize) {
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

    @Override
    public void flush() {
        this.serviceBundle.flush();
    }



    @Override
    public void initServices(Iterable services) {


        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            httpRequestServerHandler.addRestSupportFor(service.getClass(), serviceBundle.address());
        }

    }


    @Override
    public void initServices(Object... services) {


        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            httpRequestServerHandler.addRestSupportFor(service.getClass(), serviceBundle.address());
        }

    }


}

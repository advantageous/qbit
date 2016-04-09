/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.HttpTransport;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.Stoppable;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;


/**
 * Implementation of a service endpoint server.  This is the server that exposes a set of qbit services to the network.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author gcc@rd.io (Geoff Chandler)
 */
public class ServiceEndpointServerImpl implements ServiceEndpointServer {
    protected final WebSocketServiceServerHandler webSocketHandler;
    protected final HttpRequestServiceServerHandler httpRequestServerHandler;
    protected final ProtocolEncoder encoder;
    protected final HttpTransport httpServer;
    protected final ServiceBundle serviceBundle;
    protected final JsonMapper jsonMapper;
    protected final ProtocolParser parser;
    protected final Consumer<Throwable> errorHandler;
    private final Logger logger = LoggerFactory.getLogger(ServiceEndpointServerImpl.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final QBitSystemManager systemManager;
    private final EndpointDefinition endpoint;
    private final HealthServiceAsync healthServiceAsync;
    private final AtomicBoolean stop = new AtomicBoolean();
    /* Used for service discovery and registration. */
    private final ServiceDiscovery serviceDiscovery;
    protected int timeoutInSeconds = 30;


    public ServiceEndpointServerImpl(final HttpTransport httpServer, final ProtocolEncoder encoder,
                                     final ProtocolParser parser,
                                     final ServiceBundle serviceBundle,
                                     final JsonMapper jsonMapper,
                                     final int timeOutInSeconds,
                                     final int numberOfOutstandingRequests,
                                     final int protocolBatchSize,
                                     final int flushInterval,
                                     final QBitSystemManager systemManager,
                                     final String endpointName,
                                     final ServiceDiscovery serviceDiscovery,
                                     final int port,
                                     final int ttlSeconds,
                                     final HealthServiceAsync healthServiceAsync,
                                     final Consumer<Throwable> errorHandler,
                                     final long flushResponseInterval,
                                     final int parserWorkerCount,
                                     final int encoderWorkerCount) {

        this.systemManager = systemManager;
        this.encoder = encoder;
        this.parser = parser;
        this.httpServer = httpServer;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
        this.timeoutInSeconds = timeOutInSeconds;

        this.errorHandler = errorHandler;

        this.healthServiceAsync = healthServiceAsync;

        this.webSocketHandler = new WebSocketServiceServerHandler(protocolBatchSize, serviceBundle,
                parserWorkerCount, encoderWorkerCount, flushResponseInterval);

        this.serviceDiscovery = serviceDiscovery;

        httpRequestServerHandler =
                new HttpRequestServiceServerHandlerUsingMetaImpl(this.timeoutInSeconds,
                        serviceBundle, jsonMapper, numberOfOutstandingRequests, flushInterval, errorHandler);

        this.endpoint = createEndpoint(endpointName, port, ttlSeconds);


    }

    private EndpointDefinition createEndpoint(String endpointName, int port, int ttlSeconds) {

        if (serviceDiscovery != null) {

            if (ttlSeconds > 0) {
                return serviceDiscovery.registerWithTTL(endpointName, port, ttlSeconds);
            } else {
                return serviceDiscovery.register(endpointName, port);
            }
        }

        return null;
    }

    @Override
    public void start() {
        doStart();
        httpServer.start();
    }

    @Override
    public void startWithNotify(Runnable runnable) {
        doStart();
        httpServer.startWithNotify(runnable);
    }

    @Override
    public ServiceEndpointServer startServerAndWait() {
        doStart();
        if (httpServer instanceof HttpServer) {
            ((HttpServer) httpServer).startServerAndWait();
        } else {
            httpServer.start();
        }
        return this;
    }

    private void doStart() {
        stop.set(false);

        httpRequestServerHandler.start();

        httpServer.setHttpRequestConsumer(httpRequestServerHandler::handleRestCall);
        httpServer.setWebSocketMessageConsumer(webSocketHandler::handleWebSocketCall);
        httpServer.setWebSocketCloseConsumer(webSocketHandler::handleWebSocketClose);


        if (endpoint != null && endpoint.getTimeToLive() > 0) {
            handleServiceDiscoveryCheckIn();
        } else {
            httpServer.setHttpRequestsIdleConsumer(httpRequestServerHandler::httpRequestQueueIdle);
        }

        httpServer.setWebSocketIdleConsume(webSocketHandler::webSocketQueueIdle);

        serviceBundle.startUpCallQueue();
        startResponseQueueListener();
    }

    private void handleServiceDiscoveryCheckIn() {
        final AtomicLong lastCheckIn = new AtomicLong(Timer.clockTime());

        final long checkinDuration = endpoint.getTimeToLive() * 1000 / 2;

        if (healthServiceAsync == null) {
            handleDiscoveryCheckInNoHealth(lastCheckIn, checkinDuration);
        } else {
            handleDiscoveryCheckInWithHealth(lastCheckIn, checkinDuration);
        }
    }

    private void handleDiscoveryCheckInNoHealth(final AtomicLong lastCheckIn,
                                                final long checkInDuration) {
        httpServer.setHttpRequestsIdleConsumer(aVoid -> {
            httpRequestServerHandler.httpRequestQueueIdle(null);

            long now = Timer.clockTime();

            if (now > lastCheckIn.get() + checkInDuration) {
                lastCheckIn.set(now);
                serviceDiscovery.checkInOk(endpoint.getId());
            }

        });
    }


    private void handleDiscoveryCheckInWithHealth(final AtomicLong lastCheckIn,
                                                  final long checkInDuration) {
        final AtomicBoolean ok = new AtomicBoolean(true);

        httpServer.setHttpRequestsIdleConsumer(aVoid -> {
            httpRequestServerHandler.httpRequestQueueIdle(null);

            long now = Timer.clockTime();

            if (now > lastCheckIn.get() + checkInDuration) {
                lastCheckIn.set(now);
                if (ok.get()) {
                    serviceDiscovery.checkInOk(endpoint.getId());
                } else {
                    serviceDiscovery.checkIn(endpoint.getId(), HealthStatus.FAIL);
                }
                healthServiceAsync.ok(ok::set);
                ServiceProxyUtils.flushServiceProxy(healthServiceAsync);
            }
        });
    }

    public void stop() {

        try {
            serviceBundle.stop();
        } catch (Exception ex) {
            if (debug) logger.debug("Unable to cleanly shutdown bundle", ex);
        }

        try {
            if (httpServer instanceof Stoppable) {
                ((Stoppable) httpServer)
                        .stop();
            }
        } catch (Exception ex) {
            if (debug) logger.debug("Unable to cleanly shutdown httpServer", ex);
        }


        if (systemManager != null) systemManager.serviceShutDown();

    }


    /**
     * Sets up the response queue listener so we can sendText responses
     * to HTTP / WebSocket end points.
     */
    private void startResponseQueueListener() {
        serviceBundle.startReturnHandlerProcessor();
        serviceBundle.startWebResponseReturnHandler(createResponseQueueListener());
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

                if (debug) {
                    logger.debug("createResponseQueueListener() Received a response: " + response);
                }
                handleResponseFromServiceBundle(response, response.request().originatingRequest());
            }


            @Override
            public void limit() {
                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }

            @Override
            public void empty() {
                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }

            @Override
            public void idle() {
                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }
        };
    }


    private void handleResponseFromServiceBundle(final Response<Object> response, final Request<Object> originatingRequest) {


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
    public ServiceEndpointServer flush() {
        this.serviceBundle.flush();
        return this;
    }


    @Override
    public ServiceEndpointServer initServices(Iterable<Object> services) {


        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            httpRequestServerHandler.addRestSupportFor(service.getClass(), serviceBundle.address());
        }

        return this;

    }


    public ServiceEndpointServer addServiceQueue(final String address, final ServiceQueue serviceQueue) {


        serviceBundle().addServiceQueue(address, serviceQueue);
        httpRequestServerHandler.addRestSupportFor(serviceQueue.service().getClass(), serviceBundle().address());
        return this;
    }


    @Override
    public ServiceEndpointServer initServices(Object... services) {
        for (Object service : services) {
            if (debug) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            httpRequestServerHandler.addRestSupportFor(service.getClass(), serviceBundle.address());
        }
        return this;
    }

    public ServiceEndpointServer addServiceObject(String address, Object serviceObject) {

        if (debug) logger.debug("registering service: " + serviceObject.getClass().getName());

        serviceBundle.addServiceObject(address, serviceObject);
        httpRequestServerHandler.addRestSupportFor(address, serviceObject.getClass(), serviceBundle.address());

        return this;
    }

    public ServiceBundle serviceBundle() {
        return this.serviceBundle;
    }

}

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

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.http.HttpTransport;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.CallbackManagerBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.service.impl.CallbackManager;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.service.stats.StatCollection;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.Timer;

import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import static io.advantageous.qbit.http.server.HttpServerBuilder.httpServerBuilder;

/**
 * Allows for the programmatic construction of a service.
 *
 * @author rhightower
 *         created by Richard on 11/14/14.
 */

public class EndpointServerBuilder {
    public static final String QBIT_ENDPOINT_SERVER_BUILDER = "qbit.endpoint.server.builder.";
    private Queue<Response<Object>> responseQueue;
    private String host;
    private int port;
    private boolean manageQueues = false;
    private int pollTime = 25;
    private int requestBatchSize = 250;
    private int flushInterval = 200;
    private String uri;
    private int numberOfOutstandingRequests;
    private int maxRequestBatches = 10_000;
    private int timeoutSeconds = 30;
    private boolean invokeDynamic = true;
    private QueueBuilder httpRequestQueueBuilder;
    private QueueBuilder webSocketMessageQueueBuilder;
    private QueueBuilder requestQueueBuilder;
    private QueueBuilder responseQueueBuilder;
    private QueueBuilder webResponseQueueBuilder;
    private boolean eachServiceInItsOwnThread = true;
    private HttpTransport httpServer;
    private QBitSystemManager qBitSystemManager;
    private HealthServiceAsync healthService = null;
    private StatsCollector statsCollector = null;
    private Timer timer;
    private boolean enableHealthEndpoint;
    private boolean enableStatEndpoint;

    private  int statsFlushRateSeconds;
    private  int checkTimingEveryXCalls;


    private CallbackManager callbackManager;
    private CallbackManagerBuilder callbackManagerBuilder;
    private HealthServiceBuilder healthServiceBuilder;
    private StatCollection statsCollection;


    public boolean isEnableHealthEndpoint() {
        return enableHealthEndpoint;
    }

    public EndpointServerBuilder setEnableHealthEndpoint(boolean enableHealthEndpoint) {
        this.enableHealthEndpoint = enableHealthEndpoint;
        return this;
    }

    public boolean isEnableStatEndpoint() {
        return enableStatEndpoint;
    }

    public EndpointServerBuilder setEnableStatEndpoint(boolean enableStatEndpoint) {
        this.enableStatEndpoint = enableStatEndpoint;
        return this;
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public EndpointServerBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    /**
     * Allows interception of method calls before they get sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows interception of method calls before they get transformed and sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows transformation of arguments, for example from JSON to Java objects.
     */
    private Transformer<Request, Object> argTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;


    public EndpointServerBuilder(PropertyResolver propertyResolver) {
        this.eachServiceInItsOwnThread = propertyResolver.getBooleanProperty("eachServiceInItsOwnThread", true);
        this.manageQueues = propertyResolver.getBooleanProperty("manageQueues", false);
        this.invokeDynamic = propertyResolver.getBooleanProperty("invokeDynamic", true);
        this.host = propertyResolver.getStringProperty("host", null);
        this.port = propertyResolver.getIntegerProperty("port", 8080);
        this.pollTime = propertyResolver.getIntegerProperty("pollTime", pollTime);
        this.requestBatchSize = propertyResolver
                .getIntegerProperty("requestBatchSize", requestBatchSize);

        this.numberOfOutstandingRequests = propertyResolver
                .getIntegerProperty("numberOfOutstandingRequests", 1_000_000);
        this.maxRequestBatches = propertyResolver
                .getIntegerProperty("maxRequestBatches", 10_000);

        this.flushInterval = propertyResolver.getIntegerProperty("flushInterval", 500);
        this.uri = propertyResolver.getStringProperty("uri", "/services");
        this.timeoutSeconds = propertyResolver.getIntegerProperty("timeoutSeconds", 30);
        this.statsFlushRateSeconds = propertyResolver.getIntegerProperty("statsFlushRateSeconds", 5);
        this.checkTimingEveryXCalls = propertyResolver.getIntegerProperty("checkTimingEveryXCalls", 1000);

    }



    public EndpointServerBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(QBIT_ENDPOINT_SERVER_BUILDER));
    }
    public EndpointServerBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(
                QBIT_ENDPOINT_SERVER_BUILDER, properties));
    }

    public static EndpointServerBuilder endpointServerBuilder() {
        return new EndpointServerBuilder();
    }



    public CallbackManagerBuilder getCallbackManagerBuilder() {
        if (callbackManagerBuilder == null) {
            callbackManagerBuilder = CallbackManagerBuilder.callbackManagerBuilder();
            callbackManagerBuilder.setName("Endpoint-" + this.getUri() + " port " + getPort());
        }
        return callbackManagerBuilder;
    }

    public EndpointServerBuilder setCallbackManagerBuilder(CallbackManagerBuilder callbackManagerBuilder) {
        this.callbackManagerBuilder = callbackManagerBuilder;
        return this;
    }

    public CallbackManager getCallbackManager() {
        if (callbackManager == null) {

            callbackManager = this.getCallbackManagerBuilder().build();
        }
        return callbackManager;
    }

    public EndpointServerBuilder setCallbackManager(CallbackManager callbackManager) {
        this.callbackManager = callbackManager;
        return this;
    }

    public HealthServiceAsync getHealthService() {
        if (healthService == null) {
            HealthServiceBuilder builder = getHealthServiceBuilder();
            healthService = builder.setAutoFlush().buildAndStart();
        }
        return healthService;
    }

    public EndpointServerBuilder setHealthService(HealthServiceAsync healthServiceAsync) {
        this.healthService = healthServiceAsync;
        return this;
    }

    public StatsCollector getStatsCollector() {
        return statsCollector;
    }

    public EndpointServerBuilder setStatsCollector(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        return this;
    }

    public QueueBuilder getRequestQueueBuilder() {

        if (requestQueueBuilder == null) {
            requestQueueBuilder = new QueueBuilder().setBatchSize(this.getRequestBatchSize())
                    .setPollWait(this.getPollTime());
        }

        return requestQueueBuilder;
    }

    public EndpointServerBuilder setRequestQueueBuilder(QueueBuilder requestQueueBuilder) {
        this.requestQueueBuilder = requestQueueBuilder;
        return this;
    }


    public QueueBuilder getWebResponseQueueBuilder() {
        if (webResponseQueueBuilder == null) {
            webResponseQueueBuilder = QueueBuilder.queueBuilder()
                    .setBatchSize(requestBatchSize).setPollWait(pollTime);
        }
        return webResponseQueueBuilder;
    }

    public EndpointServerBuilder setWebResponseQueueBuilder(QueueBuilder webResponseQueueBuilder) {
        this.webResponseQueueBuilder = webResponseQueueBuilder;
        return this;
    }


    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public EndpointServerBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }

    public HttpTransport getHttpServer() {
        return httpServer;
    }

    public EndpointServerBuilder setHttpServer(HttpServer httpServer) {
        this.httpServer = httpServer;
        return this;
    }

    public HttpTransport getHttpTransport() {
        return httpServer;
    }

    public EndpointServerBuilder setHttpTransport(HttpTransport httpTransport) {
        this.httpServer = httpTransport;
        return this;
    }

    public QueueBuilder getHttpRequestQueueBuilder() {
        return httpRequestQueueBuilder;
    }

    public EndpointServerBuilder setHttpRequestQueueBuilder(QueueBuilder httpRequestQueueBuilder) {
        this.httpRequestQueueBuilder = httpRequestQueueBuilder;
        return this;
    }

    public QueueBuilder getWebSocketMessageQueueBuilder() {
        return webSocketMessageQueueBuilder;
    }

    public EndpointServerBuilder setWebSocketMessageQueueBuilder(QueueBuilder webSocketMessageQueueBuilder) {
        this.webSocketMessageQueueBuilder = webSocketMessageQueueBuilder;
        return this;
    }

    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public EndpointServerBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }

    public int getMaxRequestBatches() {
        return maxRequestBatches;
    }

    public EndpointServerBuilder setMaxRequestBatches(int maxRequestBatches) {
        this.maxRequestBatches = maxRequestBatches;
        return this;
    }

    public boolean isEachServiceInItsOwnThread() {
        return eachServiceInItsOwnThread;
    }

    public EndpointServerBuilder setEachServiceInItsOwnThread(boolean eachServiceInItsOwnThread) {
        this.eachServiceInItsOwnThread = eachServiceInItsOwnThread;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public EndpointServerBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;
    }

    public EndpointServerBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;
    }

    public Transformer<Request, Object> getArgTransformer() {
        return argTransformer;

    }

    public EndpointServerBuilder setArgTransformer(Transformer<Request, Object> argTransformer) {
        this.argTransformer = argTransformer;
        return this;
    }

    public int getNumberOfOutstandingRequests() {
        return numberOfOutstandingRequests;
    }

    public EndpointServerBuilder setNumberOfOutstandingRequests(int numberOfOutstandingRequests) {
        this.numberOfOutstandingRequests = numberOfOutstandingRequests;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public EndpointServerBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public EndpointServerBuilder setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public String getHost() {
        return host;
    }

    public EndpointServerBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public EndpointServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isManageQueues() {
        return manageQueues;
    }

    public EndpointServerBuilder setManageQueues(boolean manageQueues) {
        this.manageQueues = manageQueues;
        return this;
    }

    public int getPollTime() {
        return pollTime;
    }

    public EndpointServerBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public EndpointServerBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public EndpointServerBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }

    public QueueBuilder getResponseQueueBuilder() {

        if (responseQueueBuilder == null) {

            if (responseQueue == null) {
                responseQueueBuilder = new QueueBuilder().setBatchSize(this.getRequestBatchSize())
                        .setPollWait(this.getPollTime());
            } else {


                responseQueueBuilder = new QueueBuilder() {

                    @Override
                    public <T> Queue<T> build() {
                        //noinspection unchecked
                        return (Queue<T>) responseQueue;
                    }
                };
            }


        }

        return responseQueueBuilder;
    }

    public EndpointServerBuilder setResponseQueueBuilder(QueueBuilder responseQueueBuilder) {
        this.responseQueueBuilder = responseQueueBuilder;
        return this;
    }

    public Queue<Response<Object>> getResponseQueue() {
        return responseQueue;
    }

    public EndpointServerBuilder setResponseQueue(final Queue<Response<Object>> responseQueue) {
        this.responseQueue = responseQueue;
        return this;
    }


    public ServiceEndpointServer build() {

        if (httpServer == null) {
            httpServer = createHttpServer();
        }


        if (isEnableStatEndpoint() || isEnableHealthEndpoint()) {
            setupHealthAndStats();
        }



        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();
        final ProtocolEncoder encoder = QBit.factory().createEncoder();


        final ServiceBundle serviceBundle;


        serviceBundle = QBit.factory().createServiceBundle(uri,
                getRequestQueueBuilder(),
                getResponseQueueBuilder(),
                getWebResponseQueueBuilder(),
                QBit.factory(),
                eachServiceInItsOwnThread, this.getBeforeMethodCall(),
                this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer(), true,
                getSystemManager(),
                getHealthService(),
                getStatsCollector(), getTimer(),
                getStatsFlushRateSeconds(),
                getCheckTimingEveryXCalls(),
                getCallbackManager());

        final ProtocolParser parser = QBit.factory().createProtocolParser();


        final ServiceEndpointServer serviceEndpointServer = QBit.factory().createServiceServer(httpServer,
                encoder, parser, serviceBundle, jsonMapper, this.getTimeoutSeconds(),
                this.getNumberOfOutstandingRequests(), this.getRequestBatchSize(),
                this.getFlushInterval(), this.getSystemManager());


        if (serviceEndpointServer != null && qBitSystemManager != null) {
            qBitSystemManager.registerServer(serviceEndpointServer);
        }
        return serviceEndpointServer;
    }

    private void setupHealthAndStats() {


        final boolean healthEnabled = isEnableHealthEndpoint();
        final boolean statsEnabled = isEnableStatEndpoint();


        final HealthServiceAsync healthServiceAsync = healthEnabled ? getHealthService() : null;

        final StatCollection statCollection = statsEnabled ? getStatsCollection() : null;

        httpServer.setShouldContinueHttpRequest(httpRequest -> {

            if (httpRequest.getUri().startsWith("/__")) {
                handleHealthAndStats(healthEnabled, statsEnabled, healthServiceAsync, statCollection, httpRequest);
                return false;
            } else {
                return true;
            }
        });
    }

    private void handleHealthAndStats(boolean healthEnabled, boolean statsEnabled, HealthServiceAsync healthServiceAsync,
                                      StatCollection statCollection, HttpRequest httpRequest) {
        if (healthEnabled && httpRequest.getUri().startsWith("/__health")) {
            healthServiceAsync.ok(ok -> {
                if (ok) {
                    httpRequest.getReceiver().respondOK("\"ok\"");
                } else {
                    httpRequest.getReceiver().error("\"fail\"");
                }
            });
        } else if (statsEnabled && httpRequest.getUri().startsWith("/__stats")) {

            if (httpRequest.getUri().equals("/__stats/instance")) {
                if (statCollection != null) {
                    statCollection.collect(stats -> {
                        String json = JsonFactory.toJson(stats);
                        httpRequest.getReceiver().respondOK(json);
                    });
                } else {
                    httpRequest.getReceiver().error("\"failed to load stats collector\"");
                }
            } else if (httpRequest.getUri().equals("/__stats/global")) {
                /* We don't support global stats, yet. */
                httpRequest.getReceiver().respondOK("{}");
            }
        }
    }

    private HttpServer createHttpServer() {

        return httpServerBuilder().setPort(port)
                .setHost(host)
                .setManageQueues(this.isManageQueues())
                .setFlushInterval(this.getFlushInterval())
                .setPollTime(this.getPollTime())
                .setRequestBatchSize(this.getRequestBatchSize())
                .setMaxRequestBatches(this.getMaxRequestBatches())
                .setRequestQueueBuilder(this.getHttpRequestQueueBuilder())
                .setSystemManager(getSystemManager())
                .setWebSocketMessageQueueBuilder(this.getWebSocketMessageQueueBuilder()).build();
    }


    public int getStatsFlushRateSeconds() {
        return statsFlushRateSeconds;
    }

    public EndpointServerBuilder setStatsFlushRateSeconds(int statsFlushRateSeconds) {
        this.statsFlushRateSeconds = statsFlushRateSeconds;
        return this;
    }

    public int getCheckTimingEveryXCalls() {
        return checkTimingEveryXCalls;
    }

    public EndpointServerBuilder setCheckTimingEveryXCalls(int checkTimingEveryXCalls) {
        this.checkTimingEveryXCalls = checkTimingEveryXCalls;
        return this;
    }

    public HealthServiceBuilder getHealthServiceBuilder() {

        if (healthServiceBuilder == null) {
            healthServiceBuilder = HealthServiceBuilder.healthServiceBuilder();
        }
        return healthServiceBuilder;
    }

    public StatCollection getStatsCollection() {
        return statsCollection;
    }

    public EndpointServerBuilder setStatsCollection(final StatCollection statsCollection) {
        this.statsCollection = statsCollection;
        return this;
    }
}

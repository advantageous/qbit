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

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.http.HttpTransport;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.AfterMethodCall;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.CallbackManagerBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
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

import java.util.*;
import java.util.function.Consumer;

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
    private int port = 8080;
    private int flushInterval = 50;
    private String uri = "/services";
    private int numberOfOutstandingRequests = 1_000_000;
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

    private int statsFlushRateSeconds = 5;
    private int checkTimingEveryXCalls = 1000;
    private int protocolBatchSize = 80;
    private long flushResponseInterval = 25;
    private int parserWorkerCount = 4;
    private int encoderWorkerCount = 2;


    private CallbackManager callbackManager;
    private CallbackManagerBuilder callbackManagerBuilder;
    private HealthServiceBuilder healthServiceBuilder;
    private StatCollection statsCollection;
    private List<Object> services;
    private Map<String, Object> servicesWithAlias;

    private String endpointName;
    private ServiceDiscovery serviceDiscovery;
    private int ttlSeconds;

    private Factory factory;

    private JsonMapper jsonMapper;
    private ProtocolEncoder encoder;
    private HttpServerBuilder httpServerBuilder;
    private EventManager eventManager;

    private BeforeMethodSent beforeMethodSent;
    private BeforeMethodCall beforeMethodCallOnServiceQueue;
    private AfterMethodCall afterMethodCallOnServiceQueue;

    private Consumer<Throwable> errorHandler;
    private ProtocolParser parser;
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
        this.eachServiceInItsOwnThread = propertyResolver.getBooleanProperty("eachServiceInItsOwnThread", eachServiceInItsOwnThread);
        this.invokeDynamic = propertyResolver.getBooleanProperty("invokeDynamic", invokeDynamic);
        this.host = propertyResolver.getStringProperty("host", host);
        this.port = propertyResolver.getIntegerProperty("port", port);
        this.numberOfOutstandingRequests = propertyResolver
                .getIntegerProperty("numberOfOutstandingRequests", numberOfOutstandingRequests);
        this.flushInterval = propertyResolver.getIntegerProperty("flushInterval", flushInterval);
        this.uri = propertyResolver.getStringProperty("uri", uri);
        this.timeoutSeconds = propertyResolver.getIntegerProperty("timeoutSeconds", timeoutSeconds);
        this.statsFlushRateSeconds = propertyResolver.getIntegerProperty("statsFlushRateSeconds", statsFlushRateSeconds);
        this.checkTimingEveryXCalls = propertyResolver.getIntegerProperty("checkTimingEveryXCalls", checkTimingEveryXCalls);
        this.encoderWorkerCount = propertyResolver.getIntegerProperty("encoderWorkerCount", encoderWorkerCount);
        this.parserWorkerCount = propertyResolver.getIntegerProperty("parserWorkerCount", parserWorkerCount);
        this.flushResponseInterval = propertyResolver.getLongProperty("flushResponseInterval", flushResponseInterval);
        this.protocolBatchSize = propertyResolver.getIntegerProperty("protocolBatchSize", protocolBatchSize);


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

    public Map<String, Object> getServicesWithAlias() {

        if (servicesWithAlias == null) {
            servicesWithAlias = new TreeMap<>();
        }
        return servicesWithAlias;
    }

    public EndpointServerBuilder setServicesWithAlias(Map<String, Object> servicesWithAlias) {
        this.servicesWithAlias = servicesWithAlias;
        return this;
    }

    public BeforeMethodSent getBeforeMethodSent() {

        if (beforeMethodSent == null) {
            beforeMethodSent = new BeforeMethodSent() {
            };
        }
        return beforeMethodSent;
    }

    public EndpointServerBuilder setBeforeMethodSent(BeforeMethodSent beforeMethodSent) {
        this.beforeMethodSent = beforeMethodSent;
        return this;
    }

    public ProtocolParser getParser() {
        if (parser == null) {
            parser = getFactory().createProtocolParser();
        }
        return parser;
    }

    public EndpointServerBuilder setParser(ProtocolParser parser) {
        this.parser = parser;
        return this;
    }

    public Factory getFactory() {
        if (factory == null) {
            factory = QBit.factory();
        }
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    public JsonMapper getJsonMapper() {
        if (jsonMapper == null) {

            jsonMapper = getFactory().createJsonMapper();
        }
        return jsonMapper;
    }

    public EndpointServerBuilder setJsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }

    public ProtocolEncoder getEncoder() {
        if (encoder == null) {
            encoder = getFactory().createEncoder();
        }
        return encoder;
    }

    public EndpointServerBuilder setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
        return this;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public EndpointServerBuilder setEndpointName(String endpointName) {
        this.endpointName = endpointName;
        return this;
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public EndpointServerBuilder setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        return this;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public EndpointServerBuilder setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        return this;
    }

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
            requestQueueBuilder = QueueBuilder.queueBuilder();
        }

        return requestQueueBuilder;
    }

    public EndpointServerBuilder setRequestQueueBuilder(QueueBuilder requestQueueBuilder) {
        this.requestQueueBuilder = requestQueueBuilder;
        return this;
    }


    public QueueBuilder getWebResponseQueueBuilder() {
        if (webResponseQueueBuilder == null) {
            webResponseQueueBuilder = QueueBuilder.queueBuilder();
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
        if (httpServer == null) {
            httpServer = getHttpServerBuilder().build();
        }
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
                responseQueueBuilder = QueueBuilder.queueBuilder();
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


        final ServiceBundle serviceBundle;


        serviceBundle = getFactory().createServiceBundle(uri,
                getRequestQueueBuilder(),
                getResponseQueueBuilder(),
                getWebResponseQueueBuilder(),
                getFactory(),
                eachServiceInItsOwnThread, this.getBeforeMethodCall(),
                this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer(), true,
                getSystemManager(),
                getHealthService(),
                getStatsCollector(), getTimer(),
                getStatsFlushRateSeconds(),
                getCheckTimingEveryXCalls(),
                getCallbackManager(),
                getEventManager(),
                getBeforeMethodSent(),
                getBeforeMethodCallOnServiceQueue(), getAfterMethodCallOnServiceQueue());


        final ServiceEndpointServer serviceEndpointServer = new ServiceEndpointServerImpl(getHttpServer(),
                getEncoder(), getParser(), serviceBundle, getJsonMapper(), this.getTimeoutSeconds(),
                this.getNumberOfOutstandingRequests(), getProtocolBatchSize(),
                this.getFlushInterval(), this.getSystemManager(), getEndpointName(),
                getServiceDiscovery(), getPort(), getTtlSeconds(), getHealthService(), getErrorHandler(),
                getFlushResponseInterval(), getParserWorkerCount(), getEncoderWorkerCount());


        if (serviceEndpointServer != null && qBitSystemManager != null) {
            qBitSystemManager.registerServer(serviceEndpointServer);
        }

        if (services != null) {
            serviceEndpointServer.initServices(services);
        }

        if (servicesWithAlias != null) {
            servicesWithAlias.entrySet().forEach(entry -> serviceEndpointServer.addServiceObject(entry.getKey(), entry.getValue()));
        }
        return serviceEndpointServer;
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

    public List<Object> getServices() {
        if (services == null) {
            services = new ArrayList<>();
        }
        return services;
    }

    public void setServices(List<Object> services) {
        this.services = services;
    }

    public EndpointServerBuilder addService(String alias, Object service) {
        getServicesWithAlias().put(alias, service);
        return this;
    }

    public EndpointServerBuilder addService(Object service) {
        getServices().add(service);
        return this;
    }


    public EndpointServerBuilder addServices(Object... services) {
        for (Object service : services) {
            getServices().add(service);
        }
        return this;
    }


    public HttpServerBuilder getHttpServerBuilder() {

        if (httpServerBuilder == null) {

            httpServerBuilder = httpServerBuilder().setPort(getPort())
                    .setHost(getHost())
                    .setFlushInterval(this.getFlushInterval())
                    .setSystemManager(getSystemManager());


            setupHealthAndStats(httpServerBuilder);

        }

        return httpServerBuilder;
    }

    public EndpointServerBuilder setHttpServerBuilder(HttpServerBuilder httpServerBuilder) {
        this.httpServerBuilder = httpServerBuilder;
        return this;
    }


    public EndpointServerBuilder setupHealthAndStats(final HttpServerBuilder httpServerBuilder) {

        if (isEnableStatEndpoint() || isEnableHealthEndpoint()) {
            final boolean healthEnabled = isEnableHealthEndpoint();
            final boolean statsEnabled = isEnableStatEndpoint();


            final HealthServiceAsync healthServiceAsync = healthEnabled ? getHealthService() : null;

            final StatCollection statCollection = statsEnabled ? getStatsCollection() : null;

            httpServerBuilder.addShouldContinueHttpRequestPredicate(
                    new EndPointHealthPredicate(healthEnabled, statsEnabled,
                            healthServiceAsync, statCollection));
        }


        return this;
    }


    public EventManager getEventManager() {
        return eventManager;
    }

    public EndpointServerBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallOnServiceQueue() {
        return beforeMethodCallOnServiceQueue;
    }

    public EndpointServerBuilder setBeforeMethodCallOnServiceQueue(BeforeMethodCall beforeMethodCallOnServiceQueue) {
        this.beforeMethodCallOnServiceQueue = beforeMethodCallOnServiceQueue;
        return this;
    }

    public AfterMethodCall getAfterMethodCallOnServiceQueue() {
        return afterMethodCallOnServiceQueue;
    }

    public EndpointServerBuilder setAfterMethodCallOnServiceQueue(AfterMethodCall afterMethodCallOnServiceQueue) {
        this.afterMethodCallOnServiceQueue = afterMethodCallOnServiceQueue;
        return this;
    }

    public Consumer<Throwable> getErrorHandler() {
        return errorHandler;
    }

    public EndpointServerBuilder setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public long getFlushResponseInterval() {
        return flushResponseInterval;
    }

    public EndpointServerBuilder setFlushResponseInterval(long flushResponseInterval) {
        this.flushResponseInterval = flushResponseInterval;
        return this;
    }

    public int getParserWorkerCount() {
        return parserWorkerCount;
    }

    public EndpointServerBuilder setParserWorkerCount(int parserWorkerCount) {
        this.parserWorkerCount = parserWorkerCount;
        return this;
    }

    public int getEncoderWorkerCount() {
        return encoderWorkerCount;
    }

    public EndpointServerBuilder setEncoderWorkerCount(int encoderWorkerCount) {
        this.encoderWorkerCount = encoderWorkerCount;
        return this;
    }

    public int getProtocolBatchSize() {
        return protocolBatchSize;
    }

    public EndpointServerBuilder setProtocolBatchSize(int protocolBatchSize) {
        this.protocolBatchSize = protocolBatchSize;
        return this;
    }
}

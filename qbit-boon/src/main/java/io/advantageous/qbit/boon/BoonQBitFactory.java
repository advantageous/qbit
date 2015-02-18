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

package io.advantageous.qbit.boon;

import io.advantageous.qbit.BoonJsonMapper;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.impl.BoonEventBusProxyCreator;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.sender.Sender;
import io.advantageous.qbit.sender.SenderEndPoint;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerImpl;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceMethodHandler;
import io.advantageous.qbit.service.impl.BoonServiceMethodCallHandler;
import io.advantageous.qbit.service.impl.ServiceBundleImpl;
import io.advantageous.qbit.service.impl.ServiceImpl;
import io.advantageous.qbit.spi.*;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;


/**
 * Created by Richard on 9/26/14.
 *
 * @author rhightower
 *         This factory uses Boon reflection and JSON support.
 *         The Factory is a facade over other factories providing a convienient unified interface to QBIT.
 */
public class BoonQBitFactory implements Factory {

    private final Logger logger = LoggerFactory.getLogger(BoonQBitFactory.class);
    private AtomicReference<Service> systemEventManager = new AtomicReference<>();
    private ThreadLocal<EventManager> eventManagerThreadLocal = new ThreadLocal<>();
    private ProtocolParser defaultProtocol = new BoonProtocolParser();
    private ServiceProxyFactory serviceProxyFactory = new BoonServiceProxyFactory(this);
    private ServiceProxyFactory remoteServiceProxyFactory = new BoonServiceProxyFactory(this);
    private ThreadLocal<List<ProtocolParser>> protocolParserListRef = new ThreadLocal<List<ProtocolParser>>() {

        @Override
        protected List<ProtocolParser> initialValue() {
            ArrayList<ProtocolParser> list = new ArrayList<>();
            list.add(createProtocolParser());
            return list;
        }
    };

    @Override
    public EventManager systemEventManager() {

        final EventManager eventManager = eventManagerThreadLocal.get();
        if ( eventManager != null ) {
            return eventManager;
        }

        EventManager proxy;
        if ( systemEventManager.get() == null ) {
            final Service service = serviceBuilder().setInvokeDynamic(false).setServiceObject(createEventManager()).build().start();

            systemEventManager.set(service);
            proxy = service.createProxy(EventManager.class);
        } else {
            proxy = systemEventManager.get().createProxy(EventManager.class);
        }

        eventManagerThreadLocal.set(proxy);
        return proxy;
    }

    @Override
    public void shutdownSystemEventBus() {
        final Service service = systemEventManager.get();
        if ( service != null ) {
            service.stop();
        }
    }

    public EventManager eventManagerProxy() {
        return eventManagerThreadLocal.get();
    }

    public void clearEventManagerProxy() {
        eventManagerThreadLocal.set(null);
    }

    @Override
    public EventManager createEventManager() {
        return FactorySPI.getEventManagerFactory().createEventManager();
    }

    @Override
    public MethodCall<Object> createMethodCallToBeEncodedAndSent(long id, String address, String returnAddress, String objectName, String methodName, long timestamp, Object body, MultiMap<String, String> params) {

        return MethodCallBuilder.createMethodCallToBeEncodedAndSent(id, address, returnAddress, objectName, methodName, timestamp, body, params);
    }

    @Override
    public <T> T createLocalProxy(Class<T> serviceInterface, String serviceName, ServiceBundle serviceBundle) {

        return this.serviceProxyFactory.createProxy(serviceInterface, serviceName, serviceBundle);
    }


    @Override
    public <T> T createRemoteProxyWithReturnAddress(Class<T> serviceInterface, String address, String serviceName, String returnAddressArg, Sender<String> sender, BeforeMethodCall beforeMethodCall, int requestBatchSize) {
        return remoteServiceProxyFactory.createProxyWithReturnAddress(serviceInterface, serviceName, returnAddressArg, new SenderEndPoint(this.createEncoder(), address, sender, beforeMethodCall, requestBatchSize));
    }


    @Override
    public MethodCall<Object> createMethodCallFromHttpRequest(final Request<Object> request, Object args) {

        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setOriginatingRequest(request);
        methodCallBuilder.setBody(args);
        methodCallBuilder.setHeaders(request.headers());
        methodCallBuilder.setParams(request.params());
        methodCallBuilder.setAddress(request.address());
        methodCallBuilder.overridesFromParams();
        return methodCallBuilder.build();

    }

    @Override
    public JsonMapper createJsonMapper() {
        return new BoonJsonMapper();
    }


    public HttpServer createHttpServer(HttpServerOptions options, QueueBuilder requestQueueBuilder, QueueBuilder responseQueueBuilder, QueueBuilder webSocketMessageQueueBuilder, QBitSystemManager systemManager) {

        return FactorySPI.getHttpServerFactory().create(options, requestQueueBuilder, responseQueueBuilder, webSocketMessageQueueBuilder, systemManager);
    }

    @Override
    public HttpClient createHttpClient(String host, int port, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush, int flushRate, boolean keepAlive, boolean pipeline) {
        return FactorySPI.getHttpClientFactory().create(host, port, requestBatchSize, timeOutInMilliseconds, poolSize, autoFlush, flushRate, keepAlive, pipeline);
    }

    @Override
    public ServiceServer createServiceServer(final HttpServer httpServer, final ProtocolEncoder encoder, final ProtocolParser protocolParser, final ServiceBundle serviceBundle, final JsonMapper jsonMapper, final int timeOutInSeconds, final int numberOfOutstandingRequests, final int batchSize, final int flushInterval, final QBitSystemManager systemManager) {
        return new ServiceServerImpl(httpServer, encoder, protocolParser, serviceBundle, jsonMapper, timeOutInSeconds, numberOfOutstandingRequests, batchSize, flushInterval, systemManager);
    }


    @Override
    public Client createClient(String uri, HttpClient httpClient, int requestBatchSize) {
        return FactorySPI.getClientFactory().create(uri, httpClient, requestBatchSize);
    }

    @Override
    public ProtocolParser createProtocolParser() {
        return new BoonProtocolParser();
    }


    @Override
    public MethodCall<Object> createMethodCallToBeParsedFromBody(String address, String returnAddress, String objectName, String methodName, Object body, MultiMap<String, String> params) {
        MethodCall<Object> parsedMethodCall = null;
        if ( body != null ) {
            ProtocolParser parser = selectProtocolParser(body, params);

            if ( parser != null ) {
                parsedMethodCall = parser.parseMethodCall(body);
            } else {
                parsedMethodCall = defaultProtocol.parseMethodCall(body);
            }
        }

        if ( parsedMethodCall != null ) {
            return parsedMethodCall;
        }

        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setName(methodName);
        methodCallBuilder.setBody(body);
        methodCallBuilder.setObjectName(objectName);
        methodCallBuilder.setAddress(address);
        methodCallBuilder.setReturnAddress(returnAddress);
        if ( params != null ) {
            methodCallBuilder.setParams(params);
        }
        methodCallBuilder.overridesFromParams();
        return methodCallBuilder.build();
    }

    @Override
    public MethodCall<Object> createMethodCallByAddress(String address, String returnAddress, Object args, MultiMap<String, String> params) {
        return createMethodCallToBeParsedFromBody(address, returnAddress, "", "", args, params);
    }

    @Override
    public MethodCall<Object> createMethodCallByNames(String methodName, String objectName, String returnAddress, Object args, MultiMap<String, String> params) {
        return createMethodCallToBeParsedFromBody("", returnAddress, objectName, methodName, args, params);
    }

    private ProtocolParser selectProtocolParser(Object args, MultiMap<String, String> params) {
        for ( ProtocolParser parser : protocolParserListRef.get() ) {
            if ( parser.supports(args, params) ) {
                return parser;
            }
        }
        return null;
    }


    @Override
    public Service createService(final String rootAddress, final String serviceAddress, final Object service, final Queue<Response<Object>> responseQueue, final QBitSystemManager systemManager) {


        return new ServiceImpl(rootAddress, serviceAddress, service, null, new BoonServiceMethodCallHandler(true), responseQueue, true, false, systemManager);

    }

    @Override
    public Service createService(String rootAddress, String serviceAddress, Object object, Queue<Response<Object>> responseQueue, final QueueBuilder queueBuilder, boolean async, boolean invokeDynamic, boolean handleCallbacks, final QBitSystemManager systemManager) {

        return new ServiceImpl(rootAddress, serviceAddress, object, queueBuilder, new BoonServiceMethodCallHandler(invokeDynamic), responseQueue, async, handleCallbacks, systemManager);

    }


    @Override
    public ServiceBundle createServiceBundle(String address, QueueBuilder queueBuilder, final Factory factory, final boolean asyncCalls, final BeforeMethodCall beforeMethodCall, final BeforeMethodCall beforeMethodCallAfterTransform, final Transformer<Request, Object> argTransformer, boolean invokeDynamic, final QBitSystemManager systemManager) {
        return new ServiceBundleImpl(address, queueBuilder, factory, asyncCalls, beforeMethodCall, beforeMethodCallAfterTransform, argTransformer, invokeDynamic, systemManager);
    }


    @Override
    public ServiceMethodHandler createServiceMethodHandler(boolean invokeDynamic) {

        return new BoonServiceMethodCallHandler(invokeDynamic);
    }


    @Override
    public ProtocolEncoder createEncoder() {
        return new BoonProtocolEncoder();
    }


    public EventBusProxyCreator eventBusProxyCreator() {

        return new BoonEventBusProxyCreator();
    }

}

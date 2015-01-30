package io.advantageous.qbit.boon;

import io.advantageous.qbit.BoonJsonMapper;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.client.ServiceProxyFactory;
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
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.service.impl.ServiceImpl;
import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.spi.*;
import io.advantageous.qbit.transforms.Transformer;
import io.advantageous.qbit.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Richard on 9/26/14.
 *
 * @author rhightower
 *         This factory uses Boon reflection and JSON support.
 *         The Factory is a facade over other factories providing a convienient unified interface to QBIT.
 */
public class BoonQBitFactory implements Factory {

    private ProtocolParser defaultProtocol = new BoonProtocolParser();
    private ServiceProxyFactory serviceProxyFactory = new BoonServiceProxyFactory(this);

    private ServiceProxyFactory remoteServiceProxyFactory = new BoonServiceProxyFactory(this);


    private final Logger logger = LoggerFactory.getLogger(BoonQBitFactory.class);

    private ThreadLocal<List<ProtocolParser>> protocolParserListRef = new ThreadLocal<List<ProtocolParser>>(){

        @Override
        protected List<ProtocolParser> initialValue() {
            ArrayList<ProtocolParser> list = new ArrayList<>();
            list.add(createProtocolParser());
            return list;
        }
    };

    @Override
    public MethodCall<Object> createMethodCallToBeEncodedAndSent(long id, String address,
                                                                 String returnAddress,
                                                                 String objectName,
                                                                 String methodName,
                                                                 long timestamp,
                                                                 Object body,
                                                                 MultiMap<String, String> params) {

        return MethodCallBuilder.createMethodCallToBeEncodedAndSent(id, address, returnAddress, objectName, methodName, timestamp, body, params);
    }

    @Override
    public <T> T createLocalProxy(Class<T> serviceInterface,
                                  String serviceName,
                                  ServiceBundle serviceBundle) {

        return this.serviceProxyFactory.createProxy(serviceInterface, serviceName, serviceBundle);
    }

    @Override
    public Response<Object> createResponse(String message) {
        final ProtocolParser parser = selectProtocolParser(message, null);
        return parser.parseResponse(message);
    }


    @Override
    public <T> T createRemoteProxyWithReturnAddress(Class<T> serviceInterface, String address, String serviceName, String returnAddressArg, Sender<String> sender, BeforeMethodCall beforeMethodCall, int requestBatchSize) {
        return remoteServiceProxyFactory.createProxyWithReturnAddress(serviceInterface, serviceName, returnAddressArg,
                new SenderEndPoint(this.createEncoder(), address, sender, beforeMethodCall, requestBatchSize));
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


    @Override
    public HttpServer createHttpServer(String host, int port, boolean manageQueues, int pollTime, int requestBatchSize, int flushInterval, int maxRequests) {
        return FactorySPI.getHttpServerFactory().create(host, port, manageQueues, pollTime, requestBatchSize, flushInterval, maxRequests);
    }


    @Override
    public HttpServer createHttpServer(String host, int port, boolean manageQueues, int pollTime, int requestBatchSize,
                                       int flushInterval, int maxRequests, int httpWorkers, Class handler) {
        return FactorySPI.getHttpServerFactory().create(host, port, manageQueues, pollTime, requestBatchSize,
                flushInterval, maxRequests, httpWorkers, handler);
    }


    @Override
    public HttpClient createHttpClient(String host, int port, int pollTime, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush, boolean keepAlive, boolean pipeline) {
        return FactorySPI.getHttpClientFactory().create(host, port, pollTime, requestBatchSize, timeOutInMilliseconds, poolSize, autoFlush, keepAlive, pipeline);
    }

    @Override
    public ServiceServer createServiceServer(
            final HttpServer httpServer, final ProtocolEncoder encoder,
            final ProtocolParser protocolParser,
            final ServiceBundle serviceBundle,
            final JsonMapper jsonMapper,
            final int timeOutInSeconds,
            final int numberOfOutstandingRequests,
            final int batchSize) {
        return new ServiceServerImpl(httpServer, encoder, protocolParser, serviceBundle,
                jsonMapper, timeOutInSeconds, numberOfOutstandingRequests, batchSize);
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
    public MethodCall<Object> createMethodCallToBeParsedFromBody(String address,
                                                                 String returnAddress,
                                                                 String objectName,
                                                                 String methodName,
                                                                 Object body,
                                                                 MultiMap<String, String> params) {


        MethodCall<Object> parsedMethodCall = null;

        if (body != null) {
            ProtocolParser parser = selectProtocolParser(body, params);

            if (parser != null) {
                parsedMethodCall= parser.parseMethodCall(body);
            } else {
                parsedMethodCall = defaultProtocol.parseMethodCall(body);
            }
        }


        if (parsedMethodCall!=null) {
            return parsedMethodCall;
        }


        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();

        methodCallBuilder.setName(methodName);
        methodCallBuilder.setBody(body);
        methodCallBuilder.setObjectName(objectName);
        methodCallBuilder.setAddress(address);
        methodCallBuilder.setReturnAddress(returnAddress);
        if (params!=null) {
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
        for (ProtocolParser parser : protocolParserListRef.get()) {
            if (parser.supports(args, params)) {
                return parser;
            }
        }
        return null;
    }



    @Override
    public Service createService(String rootAddress, String serviceAddress, Object service, Queue<Response<Object>> responseQueue) {


        return new ServiceImpl(rootAddress,
                serviceAddress, service, null, new BoonServiceMethodCallHandler(true), responseQueue, true);

    }

    @Override
    public Service createService(String rootAddress,
                                 String serviceAddress,
                                 Object object,
                                 Queue<Response<Object>> responseQueue,
                                 final QueueBuilder queueBuilder,
                                 boolean async, boolean invokeDynamic) {

        return new ServiceImpl(
                rootAddress,
                serviceAddress,
                object,
                queueBuilder,
                new BoonServiceMethodCallHandler(invokeDynamic),
                responseQueue, async
        );

    }


    @Override
    public ServiceBundle createServiceBundle(String address, QueueBuilder queueBuilder,
                                      final Factory factory, final boolean asyncCalls,
                                      final BeforeMethodCall beforeMethodCall,
                                      final BeforeMethodCall beforeMethodCallAfterTransform,
                                      final Transformer<Request, Object> argTransformer, boolean invokeDynamic){
        return new ServiceBundleImpl(address, queueBuilder, factory,
                asyncCalls, beforeMethodCall, beforeMethodCallAfterTransform, argTransformer, invokeDynamic);
    }


    @Override
    public ServiceMethodHandler createServiceMethodHandler(boolean invokeDynamic) {

        return new BoonServiceMethodCallHandler(invokeDynamic);
    }


    @Override
    public ProtocolEncoder createEncoder() {
        return new BoonProtocolEncoder();
    }
}

package io.advantageous.qbit.server;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.HttpStatus;
import io.advantageous.qbit.http.request.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.meta.RequestMetaData;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.provider.StandardMetaDataProvider;
import io.advantageous.qbit.meta.transformer.StandardRequestTransformer;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceMethodNotFoundException;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HttpRequestServiceServerHandlerUsingMetaImpl implements HttpRequestServiceServerHandler {


    private final int timeoutInSeconds;
    private final AtomicLong lastTimeoutCheckTime = new AtomicLong();
    private final int numberOfOutstandingRequests;
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final int flushInterval;
    private final JsonMapper jsonMapper;
    private final Map<String, Request<Object>> outstandingRequestMap = new ConcurrentHashMap<>(100_000);
    private final Logger logger = LoggerFactory.getLogger(HttpRequestServiceServerHandlerUsingMetaImpl.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final Lock lock = new ReentrantLock();
    private long lastFlushTime;
    private ContextMetaBuilder contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
    private StandardRequestTransformer standardRequestTransformer;
    private final Map<RequestMethod, StandardMetaDataProvider> metaDataProviderMap = new ConcurrentHashMap<>();

    public HttpRequestServiceServerHandlerUsingMetaImpl(int timeoutInSeconds, ServiceBundle serviceBundle,
                                                        JsonMapper jsonMapper,
                                                        final int numberOfOutstandingRequests,
                                                        int flushInterval) {
        this.timeoutInSeconds = timeoutInSeconds;
        lastTimeoutCheckTime.set(Timer.timer().now() + (timeoutInSeconds * 1000));
        this.numberOfOutstandingRequests = numberOfOutstandingRequests;
        this.jsonMapper = jsonMapper;

        this.methodCallSendQueue = serviceBundle.methodSendQueue();
        this.flushInterval = flushInterval;

        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
    }

    @Override
    public void httpRequestQueueIdle(Void v) {
        long lastFlush = lastFlushTime;
        long now = Timer.timer().now();
        long duration = now - lastFlush;

        if (duration > flushInterval) {
            lastFlushTime = now;
            try {
                lock.lock();
                methodCallSendQueue.flushSends();
            } finally {
                lock.unlock();
            }
        }

    }

    public void start() {


        metaDataProviderMap.put(RequestMethod.GET, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.GET));
        metaDataProviderMap.put(RequestMethod.POST, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.POST));
        metaDataProviderMap.put(RequestMethod.PUT, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.PUT));
        metaDataProviderMap.put(RequestMethod.DELETE, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.DELETE));
        metaDataProviderMap.put(RequestMethod.HEAD, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.HEAD));
        metaDataProviderMap.put(RequestMethod.OPTIONS, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.OPTIONS));
        metaDataProviderMap.put(RequestMethod.TRACE, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.TRACE));
        metaDataProviderMap.put(RequestMethod.CONNECT, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.CONNECT));

        standardRequestTransformer = new StandardRequestTransformer(metaDataProviderMap);
    }

    @Override
    public void handleRestCall(final HttpRequest request) {

        List<String> errorList = new ArrayList<>(0);
        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorList);

        if (methodCall != null && errorList.size() == 0) {
            if (!addRequestToCheckForTimeouts(request)) {
                handleOverflow(request);
                return;
            }
            sendMethodToServiceBundle(methodCall);
        } else {
            handleErrorConverting(request, errorList, methodCall);
            return;
        }

        final RequestMetaData requestMetaData = metaDataProviderMap
                .get(RequestMethod.valueOf(request.getMethod())).get(request.address());

        if (requestMetaData.getMethod().getMethodAccess().returnType() == void.class
                && !requestMetaData.getMethod().hasCallBack()) {

            request.handled();
            writeResponse(request.getReceiver(), HttpStatus.ACCEPTED,
                    "application/json", "\"success\"", MultiMap.empty());

        }


    }

    private void handleOverflow(HttpRequest request) {
        writeResponse(request.getReceiver(), HttpStatus.TOO_MANY_REQUEST, "application/json",
                "\"too many outstanding requests\"", MultiMap.empty());
    }

    private void sendMethodToServiceBundle(MethodCall<Object> methodCall) {

        try {
            lock.lock();
            methodCallSendQueue.send(methodCall);
        } finally {
            lock.unlock();
        }
    }

    private void handleErrorConverting(HttpRequest request, List<String> errorList, MethodCall<Object> methodCall) {
        if (methodCall == null) {
            if (errorList.size() > 0) {
                request.getReceiver().response(HttpStatus.NOT_FOUND, "application/json", jsonMapper.toJson(errorList));
            } else {
                request.getReceiver().response(HttpStatus.NOT_FOUND, "application/json", "\"not found\"");
            }
        } else {
            if (errorList.size() > 0) {
                request.getReceiver().response(HttpStatus.ERROR, "application/json", jsonMapper.toJson(errorList));
            } else {
                request.getReceiver().response(HttpStatus.ERROR, "application/json", "\"unable to make call\"");
            }
        }
    }

    @Override
    public void addRestSupportFor(Class cls, String baseURI) {

        contextMetaBuilder.setRootURI(baseURI).addService(cls);

    }

    @Override
    public void checkTimeoutsForRequests() {

//        if (outstandingRequestMap.size()==0) {
//            return;
//        }

        final long now = Timer.timer().now();
        final long durationSinceLastCheck = now - lastTimeoutCheckTime.get();
        final long timeoutInMS = timeoutInSeconds * 1000;
        final boolean timedOut = durationSinceLastCheck > timeoutInMS;


        if (!(timedOut)) {
            return;
        }

        lastTimeoutCheckTime.set(now);

        long duration;


        final Set<Map.Entry<String, Request<Object>>> entries = outstandingRequestMap.entrySet();

        for (Map.Entry<String, Request<Object>> requestEntry : entries) {

            final Request<Object> request = requestEntry.getValue();
            final String key = requestEntry.getKey();

            if (request.isHandled()) {
                request.handled();
                outstandingRequestMap.remove(key);
                continue;
            }

            duration = now - request.timestamp();

            if (duration > timeoutInMS) {
                final HttpResponseReceiver httpResponse = ((HttpRequest) request).getReceiver();
                try {
                    //noinspection unchecked
                    httpResponse.response(HttpStatus.TIMED_OUT, "application/json", "\"timed out\"");
                } catch (Exception ex) {
                    logger.debug("Response not marked handled and it timed out, but could not be written " + request, ex);
                }
            }
        }
    }


    @Override
    public void handleResponseFromServiceToHttpResponse(final Response<Object> response, final HttpRequest originatingRequest) {

        String key = Str.add("" + originatingRequest.id(), "|", originatingRequest.returnAddress());
        this.outstandingRequestMap.remove(key);

        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("UnnecessaryLocalVariable") final HttpRequest httpRequest = originatingRequest;

        if (response.wasErrors()) {

            Object obj = response.body();

            if (obj instanceof ServiceMethodNotFoundException) {
                writeResponse(httpRequest.getReceiver(), HttpStatus.NOT_FOUND, "application/json", jsonMapper.toJson(response.body()), response.headers());

            } else {
                writeResponse(httpRequest.getReceiver(), HttpStatus.ERROR, "application/json", jsonMapper.toJson(response.body()), response.headers());

            }
        } else {
            if (response.body() instanceof HttpResponse) {
                writeHttpResponse(httpRequest.getReceiver(), ((HttpResponse) response.body()));
            } else {
                writeResponse(httpRequest.getReceiver(), HttpStatus.OK, "application/json", jsonMapper.toJson(response.body()), response.headers());

            }
        }


    }

    private void writeHttpResponse(HttpResponseReceiver<Object> receiver, HttpResponse httpResponse) {
        if (httpResponse instanceof HttpTextResponse) {
            HttpTextResponse httpTextResponse = (HttpTextResponse) httpResponse;
            writeResponse(receiver, httpResponse.code(),
                    httpResponse.contentType(), httpTextResponse.body(), httpTextResponse.headers());
        } else if (httpResponse instanceof HttpBinaryResponse) {
            HttpBinaryResponse httpBinaryResponse = ((HttpBinaryResponse) httpResponse);
            receiver.response(httpResponse.code(), httpResponse.contentType(), httpBinaryResponse.body(),
                    httpBinaryResponse.headers());
        }
    }



    private void writeResponse(HttpResponseReceiver response, int code, String mimeType, String responseString,
                               MultiMap<String, String> headers) {

        if (response.isText()) {
            //noinspection unchecked
            response.response(code, mimeType, responseString, headers);
        } else {
            //noinspection unchecked
            response.response(code, mimeType, responseString.getBytes(StandardCharsets.UTF_8), headers);
        }
    }


    /**
     * Add a request to the timeout queue. Server checks for timeouts when it is idle or when
     * the max outstanding outstandingRequests is met.
     *
     * @param request request.
     */
    private boolean addRequestToCheckForTimeouts(final Request<Object> request) {

        String key = Str.add("" + request.id(), "|", request.returnAddress());
        this.outstandingRequestMap.put(key, request);

        return outstandingRequestMap.size() < numberOfOutstandingRequests;
    }
}

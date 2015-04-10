package io.advantageous.qbit.server;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.advantageous.boon.core.IO.puts;

public class HttpRequestServiceServerHandlerUsingMetaImpl implements HttpRequestServiceServerHandler {


    private final int timeoutInSeconds;
    private long lastTimeoutCheckTime;
    private long lastFlushTime;
    private final int numberOfOutstandingRequests;
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final int flushInterval;
    private final JsonMapper jsonMapper;
    private ContextMetaBuilder contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
    private StandardRequestTransformer standardRequestTransformer;
    private StandardMetaDataProvider metaDataProvider;
    private final Map<String, Request<Object>> outstandingRequestMap = new ConcurrentHashMap<>(100_000);

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final Logger logger = LoggerFactory.getLogger(HttpRequestServiceServerHandlerUsingMetaImpl.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();


    private final Lock lock = new ReentrantLock();

    @Override
    public  void httpRequestQueueIdle(Void v) {
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



    public HttpRequestServiceServerHandlerUsingMetaImpl(int timeoutInSeconds, ServiceBundle serviceBundle,
                                                        JsonMapper jsonMapper,
                                                        final int numberOfOutstandingRequests,
                                               int flushInterval) {
        this.timeoutInSeconds = timeoutInSeconds;
        lastTimeoutCheckTime = Timer.timer().now() + (timeoutInSeconds * 1000);
        this.numberOfOutstandingRequests = numberOfOutstandingRequests;
        this.jsonMapper = jsonMapper;

        this.methodCallSendQueue = serviceBundle.methodSendQueue();
        this.flushInterval = flushInterval;
    }


    public void start() {

        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
        metaDataProvider = new StandardMetaDataProvider(contextMetaBuilder.build());
        standardRequestTransformer = new StandardRequestTransformer(metaDataProvider);
    }

    @Override
    public  void handleRestCall(final HttpRequest request) {

        List<String> errorList = new ArrayList<>();
        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorList);

        if (methodCall!=null && errorList.size()==0) {
            if (!addRequestToCheckForTimeouts(request)) {
                handleOverflow(request);
                return;
            }
            sendMethodToServiceBundle(methodCall);
        } else {
            handleErrorConverting(request, errorList, methodCall);
            return;
        }

        final RequestMetaData requestMetaData = metaDataProvider.get(request.address());

        if (requestMetaData.getMethod().getMethodAccess().returnType() == void.class) {

            writeResponse(request.getReceiver(), 200,
                    "application/json", "\"success\"", request.getHeaders());

        }


    }

    private void handleOverflow(HttpRequest request) {
        writeResponse(request.getReceiver(), 429, "application/json",
                "\"too many outstanding requests\"", request.getHeaders());
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
            if (errorList.size()>0) {
                request.getReceiver().response(404, "application/json", jsonMapper.toJson(errorList));
            } else {
                request.getReceiver().response(404, "application/json", "\"not found\"");
            }
        } else {
            if (errorList.size()>0) {
                request.getReceiver().response(500, "application/json", jsonMapper.toJson(errorList));
            } else {
                request.getReceiver().response(500, "application/json", "\"unable to make call\"");
            }
        }
    }

    @Override
    public void addRestSupportFor(Class cls, String baseURI) {

        contextMetaBuilder.setRootURI(baseURI).addService(cls);

    }

    @Override
    public void checkTimeoutsForRequests() {

        final long now = Timer.timer().now();
        final long durationSinceLastCheck = now - lastTimeoutCheckTime;
        final long timeoutInMS = timeoutInSeconds * 1000;
        final boolean timedOut = durationSinceLastCheck > timeoutInMS;


        if (!(timedOut)) {
            return;
        }


        if (debug) {
            puts("Checking for timeout.", "duration", durationSinceLastCheck, "ms timeout", timeoutInMS);
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                lastTimeoutCheckTime = now;
                long duration;

                final Set<Map.Entry<String, Request<Object>>> entries = outstandingRequestMap.entrySet();

                for (Map.Entry<String, Request<Object>> requestEntry : entries) {
                    final Request<Object> request = requestEntry.getValue();
                    duration = now - request.timestamp();

                    if (duration > timeoutInMS) {
                        if (!request.isHandled()) {
                            if (debug) {
                                puts("Request timed out.", "duration", duration, "ms timeout", timeoutInMS);
                            }
                            handleMethodTimedOut(requestEntry.getKey(), request);
                        }
                    }
                }

            }
        });


    }




    /**
     * Handle a method timeout.
     *
     * @param request request
     */
    private void handleMethodTimedOut(String key, final Request<Object> request) {
        this.outstandingRequestMap.remove(key);
        if (request.isHandled()) {
            return;
        }
        request.handled();

        final HttpResponseReceiver httpResponse = ((HttpRequest) request).getReceiver();

        try {
            httpResponse.response(408, "application/json", "\"timed out\"");
        } catch (Exception ex) {
            logger.debug("Response not marked handled and it timed out, but could not be written " + request, ex);
        }
    }

    @Override
    public void handleResponseFromServiceToHttpResponse(Response<Object> response, HttpRequest originatingRequest) {

        String key = Str.add("" + originatingRequest.id(), "|", originatingRequest.returnAddress());
        this.outstandingRequestMap.remove(key);

        final HttpRequest httpRequest = originatingRequest;

        if (response.wasErrors()) {

            Object obj = response.body();

            if (obj instanceof ServiceMethodNotFoundException) {
                writeResponse(httpRequest.getReceiver(), 404, "application/json", jsonMapper.toJson(response.body()), httpRequest.getHeaders());

            } else {
                writeResponse(httpRequest.getReceiver(), 500, "application/json", jsonMapper.toJson(response.body()), httpRequest.getHeaders());

            }
        } else {
            writeResponse(httpRequest.getReceiver(), 200, "application/json", jsonMapper.toJson(response.body()), httpRequest.getHeaders());
        }


    }



    private void writeResponse(HttpResponseReceiver response, int code, String mimeType, String responseString,
                               MultiMap<String, String> headers) {

        if (response.isText()) {
            response.response(code, mimeType, responseString, headers);
        } else {
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

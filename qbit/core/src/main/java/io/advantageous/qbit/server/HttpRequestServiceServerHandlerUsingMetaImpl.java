package io.advantageous.qbit.server;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.primitive.CharBuf;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.HttpStatus;
import io.advantageous.qbit.http.HttpStatusCodeException;
import io.advantageous.qbit.http.request.*;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.meta.RequestMetaData;
import io.advantageous.qbit.meta.ServiceMethodMeta;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.provider.StandardMetaDataProvider;
import io.advantageous.qbit.meta.transformer.StandardRequestTransformer;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceMethodNotFoundException;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static io.advantageous.boon.core.Sets.set;
import static io.advantageous.boon.core.Str.startsWithItemInCollection;
import static io.advantageous.boon.primitive.Arry.array;

public class HttpRequestServiceServerHandlerUsingMetaImpl implements HttpRequestServiceServerHandler {

    private static final Set<String> ignorePackages = set("sun.", "com.sun.",
            "javax.java", "java.", "oracle.", "com.oracle.", "org.junit",
            "com.intellij", "io.advantageous.boon");


    private final int timeoutInSeconds;
    private final AtomicLong lastTimeoutCheckTime = new AtomicLong();
    private final int numberOfOutstandingRequests;
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final int flushInterval;
    private final JsonMapper jsonMapper;
    private final Map<String, Request<Object>> outstandingRequestMap = new ConcurrentHashMap<>(100_000);
    private final Logger logger = LoggerFactory.getLogger(HttpRequestServiceServerHandlerUsingMetaImpl.class);

    private final boolean devMode = GlobalConstants.DEV_MODE;
    private final Lock lock = new ReentrantLock();
    private final Map<RequestMethod, StandardMetaDataProvider> metaDataProviderMap = new ConcurrentHashMap<>();
    private final Consumer<Throwable> errorHandler;
    private long lastFlushTime;
    private ContextMetaBuilder contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
    private StandardRequestTransformer standardRequestTransformer;

    public HttpRequestServiceServerHandlerUsingMetaImpl(final int timeoutInSeconds,
                                                        final ServiceBundle serviceBundle,
                                                        final JsonMapper jsonMapper,
                                                        final int numberOfOutstandingRequests,
                                                        final int flushInterval,
                                                        final Consumer<Throwable> errorHandler) {
        this.timeoutInSeconds = timeoutInSeconds;
        lastTimeoutCheckTime.set(Timer.timer().now() + (timeoutInSeconds * 1000));
        this.numberOfOutstandingRequests = numberOfOutstandingRequests;
        this.jsonMapper = jsonMapper;
        this.errorHandler = errorHandler;

        this.methodCallSendQueue = serviceBundle.methodSendQueue();
        this.flushInterval = flushInterval;

        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();


    }

    public static StackTraceElement[] getFilteredStackTrace(StackTraceElement[] stackTrace) {


        if (stackTrace == null || stackTrace.length == 0) {
            return new StackTraceElement[0];
        }
        List<StackTraceElement> list = new ArrayList<>();
        Set<String> seenThisBefore = new HashSet<>();

        for (StackTraceElement st : stackTrace) {
            if (startsWithItemInCollection(st.getClassName(), ignorePackages)) {

                continue;
            }

            String key = Str.sputs(st.getClassName(), st.getFileName(), st.getMethodName(), st.getLineNumber());
            if (seenThisBefore.contains(key)) {
                continue;
            } else {
                seenThisBefore.add(key);
            }

            list.add(st);
        }

        return array(StackTraceElement.class, list);

    }

    public static void stackTraceToJson(CharBuf buffer, StackTraceElement[] stackTrace) {

        if (stackTrace.length == 0) {
            buffer.addLine("[]");
            return;
        }


        buffer.multiply(' ', 16).addLine('[');

        for (int index = 0; index < stackTrace.length; index++) {
            StackTraceElement element = stackTrace[index];

            if (element.getClassName().contains("Exceptions")) {
                continue;
            }
            buffer.indent(17).add("[  ").asJsonString(element.getMethodName())
                    .add(',');


            buffer.indent(3).asJsonString(element.getClassName());


            if (element.getLineNumber() > 0) {
                buffer.add(",");
                buffer.indent(3).asJsonString("" + element.getLineNumber())
                        .addLine("   ],");
            } else {
                buffer.addLine(" ],");
            }

        }
        buffer.removeLastChar(); //trailing \n
        buffer.removeLastChar(); //trailing ,

        buffer.addLine().multiply(' ', 15).add(']');
    }

    /**
     * MOST IMPORTANT METHOD FOR DEBUGGING WHY SOMETHING IS NOT CALLED.
     */
    @Override
    public void handleRestCall(final HttpRequest request) {

        final List<String> errorList = new ArrayList<>(0);
        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorList);

        if (methodCall != null && errorList.size() == 0) {
            if (!addRequestToCheckForTimeouts(request)) {
                handleOverflow(request);
                return;
            }
            sendMethodToServiceBundle(methodCall);
        } else {
            if (!request.isHandled()) {
                handleErrorConverting(request, errorList, methodCall);
            }
            return;
        }

        final RequestMetaData requestMetaData = metaDataProviderMap
                .get(RequestMethod.valueOf(request.getMethod())).get(request.address());

        final ServiceMethodMeta serviceMethod = requestMetaData.getMethod();

        if (serviceMethod.getMethodAccess().returnType() == void.class
                && !serviceMethod.hasCallBack()) {

            request.handled();

            final int responseCode = serviceMethod.getResponseCode();
            writeResponse(request.getReceiver(), responseCode == -1 ? HttpStatus.ACCEPTED : responseCode,
                    serviceMethod.getContentType(), "\"success\"",
                    requestMetaData.getRequest().getResponseHeaders());

        }


    }

    /**
     * 2nd MOST IMPORTANT METHOD FOR DEBUGGING WHY SOMETHING IS NOT CALLED.
     */
    @Override
    public void handleResponseFromServiceToHttpResponse(final Response<Object> response, final HttpRequest originatingRequest) {

        final String key = Str.add("" + originatingRequest.id(), "|", originatingRequest.returnAddress());
        this.outstandingRequestMap.remove(key);


        if (response.wasErrors()) {
            handleError(response, originatingRequest);
        } else {
            if (response.body() instanceof HttpResponse) {
                writeHttpResponse(originatingRequest.getReceiver(), ((HttpResponse) response.body()));
            } else {
                final RequestMetaData requestMetaData = metaDataProviderMap
                        .get(RequestMethod.valueOf(originatingRequest.getMethod())).get(originatingRequest.address());

                final ServiceMethodMeta serviceMethodMeta = requestMetaData.getMethod();
                final int responseCode = serviceMethodMeta.getResponseCode();


                MultiMap<String, String> headers = response.headers();

                if (requestMetaData.getRequest().hasResponseHeaders()) {
                    if (response.headers() == MultiMap.EMPTY) {
                        headers = new MultiMapImpl<>();
                    } else {
                        headers = response.headers();
                    }
                    headers.putAllCopyLists(requestMetaData.getRequest().getResponseHeaders());
                }

                writeResponse(originatingRequest.getReceiver(),
                        responseCode == -1 ? HttpStatus.OK : responseCode,
                        serviceMethodMeta.getContentType(),
                        jsonMapper.toJson(response.body()),
                        headers);

            }
        }


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

        standardRequestTransformer = new StandardRequestTransformer(metaDataProviderMap, Optional.ofNullable(errorHandler));

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
                request.getReceiver().response(HttpStatus.BAD_REQUEST, "application/json", jsonMapper.toJson(errorList));
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
    public void addRestSupportFor(String alias, Class<?> cls, String baseURI) {

        contextMetaBuilder.setRootURI(baseURI).addService(alias, cls);
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

    private void handleError(Response<Object> response, HttpRequest httpRequest) {
        final Object obj = response.body();

        if (obj instanceof ServiceMethodNotFoundException) {
            writeResponse(httpRequest.getReceiver(), HttpStatus.NOT_FOUND, "application/json",
                    jsonMapper.toJson(response.body()), response.headers());

        } else if (obj instanceof HttpStatusCodeException) {
            final HttpStatusCodeException httpStatusCodeException = ((HttpStatusCodeException) obj);
            writeResponse(httpRequest.getReceiver(), httpStatusCodeException.code(), "application/json",
                    jsonMapper.toJson(httpStatusCodeException.getMessage()), response.headers());

        } else if (obj instanceof Throwable) {

            writeResponse(httpRequest.getReceiver(), HttpStatus.ERROR, "application/json", asJson(((Throwable) obj)), response.headers());

        } else {
            writeResponse(httpRequest.getReceiver(), HttpStatus.ERROR, "application/json",
                    jsonMapper.toJson(response.body()), response.headers());
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

    public String asJson(final Throwable ex) {
        final CharBuf buffer = CharBuf.create(255);

        buffer.add('{');

        buffer.addLine().indent(5).addJsonFieldName("message")
                .asJsonString(ex.getMessage()).addLine(',');


        buffer.addLine().indent(5).addJsonFieldName("exception")
                .asJsonString(ex.getClass().getSimpleName()).addLine(',');


        if (devMode) {


            if (ex.getCause() != null) {
                buffer.addLine().indent(5).addJsonFieldName("causeMessage")
                        .asJsonString(ex.getCause().getMessage()).addLine(',');


                if (ex.getCause().getCause() != null) {
                    buffer.addLine().indent(5).addJsonFieldName("cause2Message")
                            .asJsonString(ex.getCause().getCause().getMessage()).addLine(',');

                    if (ex.getCause().getCause().getCause() != null) {
                        buffer.addLine().indent(5).addJsonFieldName("cause3Message")
                                .asJsonString(ex.getCause().getCause().getCause().getMessage()).addLine(',');

                        if (ex.getCause().getCause().getCause().getCause() != null) {
                            buffer.addLine().indent(5).addJsonFieldName("cause4Message")
                                    .asJsonString(ex.getCause().getCause().getCause().getCause().getMessage()).addLine(',');

                        }

                    }

                }

            }


            final StackTraceElement[] stackTrace = getFilteredStackTrace(ex.getStackTrace());

            if (stackTrace != null && stackTrace.length > 0) {

                buffer.addLine().indent(5).addJsonFieldName("stackTrace").addLine();

                stackTraceToJson(buffer, stackTrace);

                buffer.add(',');
            }

            buffer.addLine().indent(5).addJsonFieldName("fullStackTrace").addLine();

            final StackTraceElement[] fullStackTrace = ex.getStackTrace();
            stackTraceToJson(buffer, fullStackTrace);
        }

        buffer.add('}');
        return buffer.toString();


    }

}

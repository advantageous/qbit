package io.advantageous.qbit.proxy;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpBinaryReceiver;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ProxyServiceImpl implements ProxyService {

    
    private final Reactor reactor;
    private final Timer timer;
    private final HttpClientBuilder httpClientBuilder;
    private final Optional<HttpRequestBuilder> pingBuilder;
    private final long timeOutIntervalMS;
    private final Logger logger = LoggerFactory.getLogger(ProxyServiceImpl.class);
    private final Consumer<HttpRequestBuilder> beforeSend;
    private final Consumer<Exception> errorHandler;
    private final Predicate<HttpRequest> httpClientRequestPredicate;
    private final AtomicInteger errorCount = new AtomicInteger();
    private final AtomicInteger pingCount = new AtomicInteger();
    private final boolean trackTimeOuts;


    private HttpClient backendServiceHttpClient;
    private long time;
    private final List<HttpRequestHolder> httpRequestHolderList;

    private class HttpRequestHolder {
        final HttpRequest request;
        final long startTime;

        private HttpRequestHolder(HttpRequest request, long startTime) {
            this.request = request;
            this.startTime = startTime;
        }
    }


    public ProxyServiceImpl(final Reactor reactor,
                            final Timer timer,
                            final HttpClientBuilder httpClientBuilder,
                            final Consumer<HttpRequestBuilder> beforeSend,
                            final Consumer<Exception> errorHandler,
                            final Predicate<HttpRequest> httpClientRequestPredicate,
                            final Duration checkClientDuration,
                            final Optional<HttpRequestBuilder> pingBuilder,
                            final boolean trackTimeOuts,
                            final Duration timeOutInterval) {
        this.reactor = reactor;
        this.timer = timer;
        this.httpClientBuilder = httpClientBuilder;
        this.backendServiceHttpClient = this.httpClientBuilder.buildAndStart();
        this.beforeSend = beforeSend;
        this.errorHandler = errorHandler;
        this.httpClientRequestPredicate = httpClientRequestPredicate;
        this.trackTimeOuts = trackTimeOuts;
        this.reactor.addRepeatingTask(checkClientDuration, this::checkClient);
        this.pingBuilder = pingBuilder;

        if (trackTimeOuts) {
            this.httpRequestHolderList = new ArrayList<>();
            this.timeOutIntervalMS = timeOutInterval.toMillis();
            this.reactor.addRepeatingTask(this.timeOutIntervalMS/2, TimeUnit.MILLISECONDS, this::trackTimeouts);

        } else {
            this.httpRequestHolderList = null;
            this.timeOutIntervalMS = -1;
        }
    }

    private void trackTimeouts() {
        new ArrayList<>(httpRequestHolderList).forEach(httpRequestHolder -> {

            long duration = time - httpRequestHolder.startTime;
            if (duration > timeOutIntervalMS) {
                httpRequestHolder.request.handled();
                httpRequestHolder.request.getReceiver().timeoutWithMessage(String.format("\"TIMEOUT %s %s %s\"",
                        httpRequestHolder.request.address(),
                        httpRequestHolder.request.getRemoteAddress(),
                        httpRequestHolder.startTime
                ));
                httpRequestHolderList.remove(httpRequestHolder); //Not very fast if you a lot of outstanding requests
            }
        });
    }

    private void checkClient() {

        /** If the errorCount is greater than 0, make sure we are still connected. */
        if (errorCount.get() > 0) {
            if (backendServiceHttpClient.isClosed()) {
                backendServiceHttpClient = httpClientBuilder.buildAndStart();
            }
        }

        /** If the ping builder is present, use it to ping the service. */
        if (pingBuilder.isPresent()) {
            pingBuilder.get().setBinaryReceiver((code, contentType, body) -> {
                if (code >=200 && code < 299) {
                    pingCount.incrementAndGet();
                }else {
                    errorCount.incrementAndGet();
                }

            }).setErrorHandler(e -> {
                logger.error("Error doing ping operation", e);
                errorCount.incrementAndGet();
            });
        }

    }

    /** Request coming from the client side.
     *
     * @param clientRequest clientRequest
     */
    @Override
    public void handleRequest(final HttpRequest clientRequest) {

        if (trackTimeOuts) {
            httpRequestHolderList.add(new HttpRequestHolder(clientRequest, time));
        }

        if (httpClientRequestPredicate.test(clientRequest)) {
            createBackEndRequestPopulateAndForward(clientRequest);
        }
    }

    private void createBackEndRequestPopulateAndForward(final HttpRequest clientRequest) {
        try {
    /* forward request to backend client. */
            final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder()
                    .copyRequest(clientRequest).setBinaryReceiver(new HttpBinaryReceiver() {
                        @Override
                        public void response(final int code,
                                             final String contentType,
                                             final byte[] body,
                                             final MultiMap<String, String> headers) {
                            handleBackendClientResponses(clientRequest, code, contentType, body, headers);
                        }

                        @Override
                        public void response(int code, String contentType, byte[] body) {
                            response(code, contentType, body, MultiMap.empty());
                        }
                    }).setErrorHandler(e -> {
                        handleHttpClientErrorsForBackend(clientRequest, e);
                    });

            beforeSend.accept(httpRequestBuilder);

            backendServiceHttpClient.sendHttpRequest(httpRequestBuilder.build());
        }catch (Exception ex) {
            errorCount.incrementAndGet();
            logger.error("Unable to forward request", ex);

        }
    }

    private void handleHttpClientErrorsForBackend(final HttpRequest clientRequest, final Exception e) {
                /* Notify error handler that we got an error. */
        errorHandler.accept(e);

                /* Increment our error count. */
        errorCount.incrementAndGet();

                /* Create the error message. */
        final String errorMessage = String.format("Unable to make request %s %s %s",
                clientRequest.address(),
                clientRequest.body(), e.getMessage());


                        /* Log the error. */
        logger.error(errorMessage, e);

        if (!clientRequest.isHandled()) {
            clientRequest.handled();
            /* Notify the client that there was an error. */
            clientRequest.getReceiver().error(String.format("\"%s\"", errorMessage));
        }

    }


    private void handleBackendClientResponses(final HttpRequest clientRequest,
                                              final int code,
                                              final String contentType,
                                              final byte[] body,
                                              final MultiMap<String, String> headers) {
        if (!clientRequest.isHandled()) {
            clientRequest.handled();
            clientRequest.getReceiver().response(code, contentType, body, headers);
        }
    }

    @QueueCallback({QueueCallbackType.EMPTY,
            QueueCallbackType.IDLE,
            QueueCallbackType.LIMIT})
    public void process() {
        reactor.process();
        time = timer.time();
    }

}

package io.advantageous.qbit.proxy;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.client.HttpClientClosedConnectionException;
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


/**
 * Used to proxy HTTP calls to a backend.
 */
public class ProxyServiceImpl implements ProxyService {


    /**
     * Reactor used to manage the periodic jobs.
     */
    private final Reactor reactor;

    /**
     * Timer used to get the current time in a cost effective manner.
     */
    private final Timer timer;


    /**
     * HttpClientBuilder used to construct httpClients to talk to backend services.
     */
    private final HttpClientBuilder httpClientBuilder;


    /**
     * Used to construct a ping request to the backend if present.
     * The ping request will be sent to backend every `checkClientDuration`.
     */
    private final Optional<HttpRequestBuilder> pingBuilder;


    /**
     * Sets the backend timeout. Requests that take longer than this are aborted.
     */
    private final long timeOutIntervalMS;

    /**
     * Logging.
     */
    private final Logger logger = LoggerFactory.getLogger(ProxyServiceImpl.class);


    /**
     * Used to intercept calls to do things like populate additional headers.
     * This happens after the incoming request is copied into the HttpRequestBuilder.
     */
    private final Consumer<HttpRequestBuilder> beforeSend;


    /**
     * Used if you want to do additional error handling.
     */
    private final Consumer<Exception> errorHandler;


    /**
     * Used to determine if this request should be forwarded to the back end.
     * By default there is a predicate that always returns true.
     */
    private final Predicate<HttpRequest> httpClientRequestPredicate;

    /**
     * Keep track of errors.
     */
    private final AtomicInteger errorCount = new AtomicInteger();

    /**
     * Keep track of pings that were received.
     */
    private final AtomicInteger pingCount = new AtomicInteger();


    /**
     * Used to determine if we want to track timeouts to backend services.
     */
    private final boolean trackTimeOuts;
    /**
     * Keeps a list of outstanding requests if timeout tracking is turned on.
     */
    private final List<HttpRequestHolder> httpRequestHolderList;
    /**
     * Used to forward requests to a backend service.
     */
    private HttpClient backendServiceHttpClient;
    /**
     * Keeps the current time.
     */
    private long time;
    /**
     * Keeps the current time.
     */
    private long lastHttpClientStart;

    /**
     * Construct.
     *
     * @param reactor                    reactor
     * @param timer                      timer
     * @param httpClientBuilder          client builder to build client to backend.
     * @param beforeSend                 used if you want to populate the request builder before request is sent to the backend
     * @param errorHandler               used to pass a custom error handler
     * @param httpClientRequestPredicate httpClientRequestPredicate is used to see if this request should be forwarded to the backend.
     * @param checkClientDuration        checkClientDuration periodic check health of backend.
     * @param pingBuilder                if present used to build a ping request to backend to check client connectivity.
     * @param trackTimeOuts              if true track timeouts.
     * @param timeOutInterval            if tracking timeouts, what is considered a timeout.
     */
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

        /* If we are tracking timeouts than setup a repeating job to track timeouts. */
        if (trackTimeOuts) {
            this.httpRequestHolderList = new ArrayList<>();
            this.timeOutIntervalMS = timeOutInterval.toMillis();
            this.reactor.addRepeatingTask(this.timeOutIntervalMS / 2, TimeUnit.MILLISECONDS, this::trackTimeouts);

        } else {
            this.httpRequestHolderList = null;
            this.timeOutIntervalMS = -1;
        }
    }

    /**
     * Tracks timeouts periodically if timeout tracking is enabled.
     */
    private void trackTimeouts() {
        new ArrayList<>(httpRequestHolderList).forEach(httpRequestHolder -> {

            /* If it is handled then remove it from the list. */
            if (httpRequestHolder.request.isHandled()) {
                httpRequestHolderList.remove(httpRequestHolder);
                return;
            }

            /* Get the duration that this request has been around. */
            final long duration = time - httpRequestHolder.startTime;

            /* See if the duration is greater than the timeout time. */
            if (duration > timeOutIntervalMS) {

                /* If we timed out, mark the request as handled, and then notify the client that the
                backend timed out.
                 */
                httpRequestHolder.request.handled();

                /* Tell client that the backend timed out. */
                httpRequestHolder.request.getReceiver().timeoutWithMessage(String.format("\"TIMEOUT %s %s %s\"",
                        httpRequestHolder.request.address(),
                        httpRequestHolder.request.getRemoteAddress(),
                        httpRequestHolder.startTime
                ));

                /* If we timed out then remove this from the list. */
                httpRequestHolderList.remove(httpRequestHolder); //Not very fast if you a lot of outstanding requests
            }
        });
    }

    /**
     * Checks client health periodically to see if we are connected. Tries to reconnect if not connected.
     */
    private void checkClient() {

        try {

            /** If the errorCount is greater than 0, make sure we are still connected. */
            if (errorCount.get() > 0) {
                errorCount.set(0);
                if (backendServiceHttpClient == null || backendServiceHttpClient.isClosed()) {

                    if (backendServiceHttpClient != null) {
                        try {
                            backendServiceHttpClient.stop();
                        } catch (Exception ex) {
                            logger.debug("Was unable to stop the client connection", ex);
                        }
                    }
                    backendServiceHttpClient = httpClientBuilder.buildAndStart();
                    lastHttpClientStart = time;
                }
            }

            /** If the ping builder is present, use it to ping the service. */
            if (pingBuilder.isPresent()) {

                if (backendServiceHttpClient != null) {
                    pingBuilder.get().setBinaryReceiver((code, contentType, body) -> {
                        if (code >= 200 && code < 299) {
                            pingCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }

                    }).setErrorHandler(e -> {
                        logger.error("Error doing ping operation", e);
                        errorCount.incrementAndGet();
                    });

                    final HttpRequest httpRequest = pingBuilder.get().build();

                    backendServiceHttpClient.sendHttpRequest(httpRequest);
                }
            }

        } catch (Exception ex) {
            errorHandler.accept(ex);
            logger.error("Unable to check connection");
        }

    }

    /**
     * Request coming from the client side.
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

    /**
     * Creates a backend request from the client request and then forwards it.
     *
     * @param clientRequest clientRequest
     */
    private void createBackEndRequestPopulateAndForward(final HttpRequest clientRequest) {
        try {

            if (backendServiceHttpClient == null) {
                handleHttpClientErrorsForBackend(clientRequest, new HttpClientClosedConnectionException("Not connected"));
                long timeSinceLastStart = time - lastHttpClientStart;
                if (timeSinceLastStart > 10_000) {
                    checkClient();
                }
                return;
            }
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
                    }).setErrorHandler(e -> handleHttpClientErrorsForBackend(clientRequest, e));

            /** Give user of the lib a chance to populate headers and such. */
            beforeSend.accept(httpRequestBuilder);

            backendServiceHttpClient.sendHttpRequest(httpRequestBuilder.build());
        } catch (HttpClientClosedConnectionException httpClientClosedConnectionException) {

            errorCount.incrementAndGet();
            errorHandler.accept(httpClientClosedConnectionException);
            logger.error("Unable to forward request", httpClientClosedConnectionException);
            handleHttpClientErrorsForBackend(clientRequest, httpClientClosedConnectionException);
            backendServiceHttpClient = null;

            long timeSinceLastStart = time - lastHttpClientStart;
            if (timeSinceLastStart > 10_000) {
                checkClient();
            }

        } catch (Exception ex) {
            errorCount.incrementAndGet();
            errorHandler.accept(ex);
            logger.error("Unable to forward request", ex);
            handleHttpClientErrorsForBackend(clientRequest, ex);

            long timeSinceLastStart = time - lastHttpClientStart;
            if (timeSinceLastStart > 10_000) {
                checkClient();
            }

        }
    }

    /**
     * Handle errors.
     *
     * @param clientRequest clientRequest
     * @param e             exception
     */
    private void handleHttpClientErrorsForBackend(final HttpRequest clientRequest, final Exception e) {
                /* Notify error handler that we got an error. */
        errorHandler.accept(e);

                /* Increment our error count. */
        errorCount.incrementAndGet();

                /* Create the error message. */
        final String errorMessage = String.format("Unable to make request %s ",
                clientRequest.address());


                        /* Log the error. */
        logger.error(errorMessage, e);

        /* Don't send the error to the client if we already handled this, i.e., timedout already. */
        if (!clientRequest.isHandled()) {
            clientRequest.handled();
            /* Notify the client that there was an error. */
            clientRequest.getReceiver().error(String.format("\"%s\"", errorMessage));
        }

    }

    /**
     * Handle a response from the backend service
     *
     * @param clientRequest clientRequest (original client request)
     * @param code          response code from the backend.
     * @param contentType   contentType from the backend.
     * @param body          body from the backend.
     * @param headers       headers from the backend.
     */
    private void handleBackendClientResponses(final HttpRequest clientRequest,
                                              final int code,
                                              final String contentType,
                                              final byte[] body,
                                              final MultiMap<String, String> headers) {

        /** If it is handled like it timed out already or some other error then don't do anything. */
        if (!clientRequest.isHandled()) {

            /* If it was handled, let everyone know so we don't get a timeout. */
            clientRequest.handled();

            /* Send the response out the front end. */
            clientRequest.getReceiver().response(code, contentType, body, headers);
        }
    }

    /**
     * Manage periodic jobs.
     */
    @QueueCallback({QueueCallbackType.EMPTY,
            QueueCallbackType.IDLE,
            QueueCallbackType.LIMIT})
    public void process() {
        reactor.process();
        time = timer.time();
    }

    /**
     * Holds request information.
     */
    private class HttpRequestHolder {
        final HttpRequest request;
        final long startTime;

        private HttpRequestHolder(HttpRequest request, long startTime) {
            this.request = request;
            this.startTime = startTime;
        }
    }

}

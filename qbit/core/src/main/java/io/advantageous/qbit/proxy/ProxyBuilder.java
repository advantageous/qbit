package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Used to construct a proxy service to proxy call to a backend.
 */
public class ProxyBuilder {

    /**
     * Reactor used to manage the periodic jobs.
     */
    private Reactor reactor;

    /**
     * Timer used to get the current time in a cost effective manner.
     */
    private Timer timer;

    /**
     * HttpClientBuilder used to construct httpClients to talk to backend services.
     */
    private HttpClientBuilder httpClientBuilder;

    /**
     * Used to intercept calls to do things like populate additional headers.
     * This happens after the incoming request is copied into the HttpRequestBuilder.
     */
    private Consumer<HttpRequestBuilder> beforeSend;

    /**
     * Used if you want to do additional error handling.
     */
    private Consumer<Exception> errorHandler;

    /**
     * Used to determine if this request should be forwarded to the back end.
     * By default there is a predicate that always returns true.
     */
    private Predicate<HttpRequest> httpClientRequestPredicate;

    /**
     * How often we should check to see if the backend connection is healthy.
     */
    private Duration checkClientDuration = Duration.MINUTES.units(10);

    /**
     * Used to construct a ping request to the backend.
     * The ping request will be sent to backend every `checkClientDuration`.
     */
    private HttpRequestBuilder pingBuilder;

    /**
     * Used to determine if we want to track timeouts to backend services.
     */
    private boolean trackTimeOuts;


    /**
     * Sets the backend timeout. Requests that take longer than this are aborted.
     */
    private Duration timeOutInterval = Duration.SECONDS.units(180);

    /**
     * Used to construct a proxy service to the ProxyServiceImpl
     */
    private ServiceBuilder serviceBuilder;

    public static ProxyBuilder proxyBuilder() {
        return new ProxyBuilder();
    }

    public ServiceBuilder getServiceBuilder() {

        if (serviceBuilder == null) {
            serviceBuilder = ServiceBuilder.serviceBuilder();
            return serviceBuilder;
        }

        return serviceBuilder.copy();
    }

    public Reactor getReactor() {
        if (reactor == null) {
            reactor = ReactorBuilder.reactorBuilder().build();
        }
        return reactor;
    }

    public ProxyBuilder setReactor(Reactor reactor) {
        this.reactor = reactor;
        return this;
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public ProxyBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public HttpClientBuilder getHttpClientBuilder() {
        if (httpClientBuilder == null) {
            httpClientBuilder = HttpClientBuilder.httpClientBuilder().setPipeline(false)
                    .setKeepAlive(false).setPoolSize(100);
        }
        return httpClientBuilder;
    }

    public ProxyBuilder setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
        return this;
    }

    public Consumer<HttpRequestBuilder> getBeforeSend() {
        if (beforeSend == null) {
            beforeSend = httpRequestBuilder -> {

            };
        }
        return beforeSend;
    }

    public ProxyBuilder setBeforeSend(Consumer<HttpRequestBuilder> beforeSend) {
        this.beforeSend = beforeSend;
        return this;
    }

    public Consumer<Exception> getErrorHandler() {
        if (errorHandler == null) {
            errorHandler = e -> {
            };
        }
        return errorHandler;
    }

    public ProxyBuilder setErrorHandler(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public Predicate<HttpRequest> getHttpClientRequestPredicate() {
        if (httpClientRequestPredicate == null) {
            httpClientRequestPredicate = request -> true;
        }
        return httpClientRequestPredicate;
    }

    public ProxyBuilder setHttpClientRequestPredicate(Predicate<HttpRequest> httpClientRequestPredicate) {
        this.httpClientRequestPredicate = httpClientRequestPredicate;
        return this;
    }

    public Duration getCheckClientDuration() {
        return checkClientDuration;
    }

    public ProxyBuilder setCheckClientDuration(Duration checkClientDuration) {
        this.checkClientDuration = checkClientDuration;
        return this;
    }

    public HttpRequestBuilder getPingBuilder() {
        return pingBuilder;
    }

    public ProxyBuilder setPingBuilder(HttpRequestBuilder pingBuilder) {
        this.pingBuilder = pingBuilder;
        return this;
    }

    public boolean isTrackTimeOuts() {
        return trackTimeOuts;
    }

    public ProxyBuilder setTrackTimeOuts(boolean trackTimeOuts) {
        this.trackTimeOuts = trackTimeOuts;
        return this;
    }

    public Duration getTimeOutInterval() {
        return timeOutInterval;
    }

    public ProxyBuilder setTimeOutInterval(Duration timeOutInterval) {
        this.timeOutInterval = timeOutInterval;
        return this;
    }

    /**
     * Build the impl.
     *
     * @return returns an instance of the impl.
     */
    public ProxyService build() {
        return new ProxyServiceImpl(getReactor(), getTimer(), getHttpClientBuilder(), getBeforeSend(),
                getErrorHandler(), getHttpClientRequestPredicate(), getCheckClientDuration(),
                pingBuilder == null ? Optional.<HttpRequestBuilder>empty() : Optional.of(pingBuilder),
                isTrackTimeOuts(), getTimeOutInterval());
    }

    /**
     * Builds a proxy queue service to the impl.
     *
     * @return proxy queue service interface to impl.
     */
    public ProxyService buildProxy() {
        return getServiceBuilder().setServiceObject(build()).buildAndStart().createProxyWithAutoFlush(ProxyService.class,
                Duration.HUNDRED_MILLIS);
    }
}

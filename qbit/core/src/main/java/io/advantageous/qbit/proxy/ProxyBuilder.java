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

public class ProxyBuilder {

    private Reactor reactor;
    private Timer timer;
    private HttpClientBuilder httpClientBuilder;
    private Consumer<HttpRequestBuilder> beforeSend;
    private Consumer<Exception> errorHandler;
    private Predicate<HttpRequest> httpClientRequestPredicate;
    private Duration checkClientDuration = Duration.MINUTES.units(10);
    private HttpRequestBuilder pingBuilder;
    private boolean trackTimeOuts;
    private Duration timeOutInterval = Duration.SECONDS.units(180);
    private ServiceBuilder serviceBuilder;

    public ServiceBuilder getServiceBuilder() {

        if (serviceBuilder==null) {
            serviceBuilder = ServiceBuilder.serviceBuilder();
        }
        return serviceBuilder;
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
        if (httpClientBuilder==null) {
            httpClientBuilder = HttpClientBuilder.httpClientBuilder();
        }
        return httpClientBuilder;
    }

    public ProxyBuilder setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
        return this;
    }

    public Consumer<HttpRequestBuilder> getBeforeSend() {
        if (beforeSend==null) {
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
        if (errorHandler==null) {
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
        if (httpClientRequestPredicate==null) {
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

    public ProxyService build() {
        return new ProxyServiceImpl(getReactor(), getTimer(), getHttpClientBuilder(), getBeforeSend(),
                getErrorHandler(), getHttpClientRequestPredicate(), getCheckClientDuration(),
                pingBuilder==null? Optional.<HttpRequestBuilder>empty() : Optional.of(pingBuilder),
                isTrackTimeOuts(), getTimeOutInterval());
    }

    public ProxyService buildProxy() {
        return getServiceBuilder().setServiceObject(build()).buildAndStart().createProxyWithAutoFlush(ProxyService.class,
                Duration.HUNDRED_MILLIS);
    }

    public static ProxyBuilder proxyBuilder() {
        return new ProxyBuilder();
    }
}

package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpRequest;

import java.util.Optional;

/**
 * Holds the current HttpRequest for the current http request.
 */
public class HttpRequestContext {

    /** Current http request. */
    private final static ThreadLocal<HttpRequest> httpRequestThreadLocal = new ThreadLocal<>();

    /** Grab the current http request.
     *
     * @return Optional http request.
     */
    public Optional<HttpRequest> getHttpRequest(){
        return Optional.ofNullable(httpRequestThreadLocal.get());
    }


    /**
     * Used from this package to populate the HttpRequest for this thread.
     * @param httpRequest httpRequest
     */
    static void setHttpRequest(HttpRequest httpRequest) {
        httpRequestThreadLocal.set(httpRequest);
    }

    /**
     * Clear the HttpRequest.
     */
    static void clearHttpRequest() {
        httpRequestThreadLocal.set(null);
    }


}

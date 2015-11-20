package io.advantageous.qbit.service;

import io.advantageous.qbit.message.Request;

import java.util.Optional;

/**
 * Holds the current request for the method call.
 */
public class RequestContext {

    /** Current request. */
    private final static ThreadLocal<Request> requestThreadLocal = new ThreadLocal<>();


    /** Grab the current  request.
     *
     * @return Optional  request.
     */
    public Optional<Request> getRequest() {
        final Request request = requestThreadLocal.get();
        return Optional.ofNullable(request);

    }


    /**
     * Used from this package to populate request for this thread.
     * @param request request
     */
    static void setRequest(final Request request) {
        requestThreadLocal.set(request);
    }

    /**
     * Clear the request.
     */
    static void clear() {
        requestThreadLocal.set(null);
    }


}

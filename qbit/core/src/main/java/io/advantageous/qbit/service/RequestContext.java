package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;

import java.util.Optional;

/**
 * Holds the current request for the method call.
 */
public class RequestContext {

    /**
     * Current request.
     */
    private final static ThreadLocal<Request<Object>> requestThreadLocal = new ThreadLocal<>();

    /**
     * Clear the request.
     */
    static void clear() {
        requestThreadLocal.set(null);
    }

    /**
     * Grab the current  request.
     *
     * @return Optional  request.
     */
    public Optional<Request<Object>> getRequest() {
        final Request request = requestThreadLocal.get();
        return Optional.ofNullable(request);

    }

    /**
     * Used from this package to populate request for this thread.
     *
     * @param request request
     */
    static void setRequest(final Request<Object> request) {
        requestThreadLocal.set(request);
    }

    /**
     * Grab the current  request.
     *
     * @return Optional  request.
     */
    public Optional<MethodCall<Object>> getMethodCall() {
        final Request<Object> request = requestThreadLocal.get();
        if (request instanceof MethodCall) {
            return Optional.of(((MethodCall<Object>) request));
        }
        return Optional.empty();

    }


}

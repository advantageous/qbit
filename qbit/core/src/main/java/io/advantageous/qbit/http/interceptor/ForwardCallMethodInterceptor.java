package io.advantageous.qbit.http.interceptor;

import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.service.RequestContext;

import java.util.Optional;

/**
 * This is used by proxies to find the parent request and forward it
 * to the service that the parent calls.
 */
public class ForwardCallMethodInterceptor implements BeforeMethodSent {

    /**
     * Holds the request context, which holds the active request.
     */
    private final RequestContext requestContext;

    /**
     * @param requestContext request context
     */
    public ForwardCallMethodInterceptor(final RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Intercept the call before it gets sent to the service queue.
     *
     * @param methodBuilder methodBuilder
     */
    @Override
    public void beforeMethodSent(final MethodCallBuilder methodBuilder) {

        if (methodBuilder.getOriginatingRequest() == null) {
            final Optional<Request<Object>> request = requestContext.getRequest();
            if (request.isPresent()) {
                methodBuilder.setOriginatingRequest(request.get());
            }
        }
    }
}

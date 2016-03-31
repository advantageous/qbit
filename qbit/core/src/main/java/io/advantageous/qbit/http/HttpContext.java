package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.service.RequestContext;

import java.util.Optional;

/**
 * Holds current information about the HttpRequest.
 */
public class HttpContext extends RequestContext {


    /**
     * Grab the current http request.
     *
     * @return Optional http request.
     */
    public Optional<HttpRequest> getHttpRequest() {
        final Optional<Request<Object>> request = this.getRequest();
        if (request.isPresent()) {
            return findHttpRequest(request.get());
        } else {
            return Optional.empty();
        }
    }

    private Optional<HttpRequest> findHttpRequest(Request<Object> request) {

        if (request.originatingRequest() instanceof HttpRequest) {
            return Optional.of(((HttpRequest) request.originatingRequest()));
        } else if (request.originatingRequest() != null) {
            return findHttpRequest(request.originatingRequest());
        } else {
            return Optional.empty();
        }
    }


}

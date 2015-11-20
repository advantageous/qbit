package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.service.RequestContext;

import java.util.Optional;

public class HttpContext extends RequestContext {


    /** Grab the current http request.
     *
     * @return Optional http request.
     */
    public Optional<HttpRequest> getHttpRequest() {

        final Optional<Request> requestOptional = this.getRequest();

        if (requestOptional.isPresent()) {
            final Request request = requestOptional.get();
            if (request.originatingRequest() instanceof HttpRequest) {
                return Optional.of((HttpRequest)request.originatingRequest());
            }
        }

        return Optional.empty();

    }


}

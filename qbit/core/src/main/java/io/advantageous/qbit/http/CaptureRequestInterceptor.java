package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.service.AfterMethodCall;
import io.advantageous.qbit.service.BeforeMethodCall;

/**
 * Captures the HttpRequest if any present and puts it in the HttpRequestContext.
 */
public class CaptureRequestInterceptor implements BeforeMethodCall, AfterMethodCall {


    /** Captures the current method call and if originating as an HttpRequest,
     * then we pass the HttpRequest into the the HttpRequestContext.
     * @param methodCall methodCall
     * @return always true which means continue.
     */
    @Override
    public boolean before(final MethodCall methodCall) {

        if (methodCall.originatingRequest() instanceof HttpRequest) {
            final HttpRequest httpRequest = ((HttpRequest) methodCall.originatingRequest());
            HttpRequestContext.setHttpRequest(httpRequest);
        }
        return true;
    }


    /**
     * Clear the request out of the context
     * @param methodCall methodCall
     * @param response response
     * @return always true
     */
    @Override
    public boolean after(final MethodCall methodCall, final Response response) {
        HttpRequestContext.clearHttpRequest();
        return true;
    }

}

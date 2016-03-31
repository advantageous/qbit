package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

/**
 * Captures the Request if any present and puts it in the RequestContext.
 */
public class CaptureRequestInterceptor implements BeforeMethodCall, AfterMethodCall {


    /**
     * Captures the current method call and if originating as an HttpRequest,
     * then we pass the HttpRequest into the the RequestContext.
     *
     * @param methodCall methodCall
     * @return always true which means continue.
     */
    @Override
    public boolean before(final MethodCall methodCall) {

        RequestContext.setRequest(methodCall);
        return true;
    }


    /**
     * Clear the request out of the context
     *
     * @param methodCall methodCall
     * @param response   response
     * @return always true
     */
    @Override
    public boolean after(final MethodCall methodCall, final Response response) {
        RequestContext.clear();
        return true;
    }

}

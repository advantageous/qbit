package io.advantageous.qbit.http.request.decorator;

import io.advantageous.qbit.util.MultiMap;

public interface HttpResponseDecorator {


    /**
     * @param responseHolder  responseHolder
     * @param requestPath     request path
     * @param requestMethod   request method GET, PUT, POST
     * @param code            response code
     * @param contentType     content type, e.g., application/json
     * @param payload         the actual payload
     * @param responseHeaders response headers
     * @param requestHeaders  request headers
     * @param requestParams   request params
     * @return true if this handler handles the request.
     */
    default boolean decorateTextResponse(HttpTextResponseHolder responseHolder,
                                         String requestPath, String requestMethod,
                                         int code, String contentType, String payload,
                                         final MultiMap<String, String> responseHeaders,
                                         final MultiMap<String, String> requestHeaders,
                                         final MultiMap<String, String> requestParams) {
        return false;
    }


    /**
     * @param responseHolder  responseHolder
     * @param requestPath     request path
     * @param requestMethod   request method GET, PUT, POST
     * @param code            response code
     * @param contentType     content type, e.g., application/json
     * @param payload         the actual payload
     * @param responseHeaders response headers
     * @param requestHeaders  request headers
     * @param requestParams   request params
     * @return true if this handler handles the request.
     */
    default boolean decorateBinaryResponse(HttpBinaryResponseHolder responseHolder,
                                           String requestPath, String requestMethod,
                                           int code, String contentType, byte[] payload,
                                           final MultiMap<String, String> responseHeaders,
                                           final MultiMap<String, String> requestHeaders,
                                           final MultiMap<String, String> requestParams) {
        return false;
    }
}

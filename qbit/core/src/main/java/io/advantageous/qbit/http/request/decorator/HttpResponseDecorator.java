package io.advantageous.qbit.http.request.decorator;

import io.advantageous.qbit.util.MultiMap;

public interface HttpResponseDecorator {


    /**
     * Returns true if we should continue.
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
     * Returns true if we should continue.
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

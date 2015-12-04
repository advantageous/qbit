package io.advantageous.qbit.http.request;

import io.advantageous.qbit.util.MultiMap;

public interface HttpResponseDecorator {


    boolean decorateTextResponse(HttpTextResponse[] responseHolder,
                                 String requestPath, int code, String contentType, String payload,
                                 final MultiMap<String, String> responseHeaders,
                                 final MultiMap<String, String> requestHeaders,
                                 final MultiMap<String, String> requestParams);


    boolean decorateBinaryResponse(HttpBinaryResponse[] responseHolder,
                                   String requestPath, int code, String contentType, byte[] payload,
                                   final MultiMap<String, String> responseHeaders,
                                   final MultiMap<String, String> requestHeaders,
                                   final MultiMap<String, String> requestParams);
}

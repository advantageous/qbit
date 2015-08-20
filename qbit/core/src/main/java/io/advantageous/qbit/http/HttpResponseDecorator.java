package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpBinaryResponse;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.util.MultiMap;

public interface HttpResponseDecorator {


    boolean decorateTextResponse(int code, String contentType, String payload, MultiMap<String, String> headers,
                                 HttpTextResponse[] responseHolder);


    boolean decorateBinaryResponse(int code, String contentType, byte[] payload, MultiMap<String, String> headers,
                                   HttpBinaryResponse[] responseHolder);
}

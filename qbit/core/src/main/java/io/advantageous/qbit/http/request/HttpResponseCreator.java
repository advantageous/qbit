package io.advantageous.qbit.http.request;

import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CopyOnWriteArrayList;

public interface HttpResponseCreator {

    HttpResponse<?> createResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                   final String requestPath,
                                   final String requestMethod,
                                   final int code,
                                   final String contentType,
                                   final Object body,
                                   final MultiMap<String, String> responseHeaders,
                                   final MultiMap<String, String> requestHeaders,
                                   final MultiMap<String, String> requestParams);

}

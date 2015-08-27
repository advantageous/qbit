package io.advantageous.qbit.http.request.impl;

import io.advantageous.qbit.http.request.*;
import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CopyOnWriteArrayList;

public class HttpResponseCreatorDefault implements HttpResponseCreator {


    public HttpResponse<?> createResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                               final String requestPath,
                                               final int code,
                                               final String contentType,
                                               final Object payload,
                                               final MultiMap<String, String> responseHeaders,
                                               final MultiMap<String, String> requestHeaders,
                                               final MultiMap<String, String> requestParams) {
        if (decorators.size()==0) {
            return null;
        }
        if (payload instanceof byte[]) {
            return createBinaryResponse(decorators, requestPath, code, contentType, (byte[]) payload,
                    responseHeaders, requestHeaders, requestParams);
        } else {
            return createTextResponse(decorators, requestPath, code, contentType, payload.toString(),
                    responseHeaders, requestHeaders, requestParams);

        }
    }

    private HttpTextResponse createTextResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                                final String requestPath,
                                                final int code,
                                                final String contentType,
                                                final String payload,
                                                final MultiMap<String, String> responseHeaders,
                                                final MultiMap<String, String> requestHeaders,
                                                final MultiMap<String, String> requestParams) {

        HttpTextResponse httpTextResponse = null;
        if (decorators.size()>=0) {
            HttpTextResponse[] holder = new HttpTextResponse[1];
            for (HttpResponseDecorator decorator : decorators) {
                if (decorator.decorateTextResponse(holder, requestPath, code, contentType,
                        payload, responseHeaders, requestHeaders, requestParams )) {
                    httpTextResponse = holder[0];
                    break;
                }
            }
        }
        return httpTextResponse;
    }

    private HttpBinaryResponse createBinaryResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                                    final String requestPath, int code, String contentType,
                                                    byte[] payload,
                                                    final MultiMap<String, String> responseHeaders,
                                                    final MultiMap<String, String> requestHeaders,
                                                    final MultiMap<String, String> requestParams) {

        HttpBinaryResponse httpResponse = null;
        if (decorators.size()>=0) {
            HttpBinaryResponse[] holder = new HttpBinaryResponse[1];

            for (HttpResponseDecorator decorator : decorators) {
                if (decorator.decorateBinaryResponse(
                        holder, requestPath, code, contentType,
                        payload, responseHeaders, requestHeaders, requestParams )) {
                    httpResponse = holder[0];
                    break;
                }
            }
        }
        return httpResponse;

    }
}

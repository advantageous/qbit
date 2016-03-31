package io.advantageous.qbit.http.request.impl;

import io.advantageous.qbit.http.request.HttpBinaryResponse;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.request.decorator.HttpBinaryResponseHolder;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.request.decorator.HttpTextResponseHolder;
import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CopyOnWriteArrayList;

public class HttpResponseCreatorDefault implements HttpResponseCreator {


    public HttpResponse<?> createResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                          final String requestPath,
                                          final String requestMethod,
                                          final int code,
                                          final String contentType,
                                          final Object payload,
                                          final MultiMap<String, String> responseHeaders,
                                          final MultiMap<String, String> requestHeaders,
                                          final MultiMap<String, String> requestParams) {
        if (decorators.size() == 0) {
            return null;
        }
        if (payload instanceof byte[]) {
            return createBinaryResponse(decorators, requestPath, requestMethod, code, contentType, (byte[]) payload,
                    responseHeaders, requestHeaders, requestParams);
        } else {
            return createTextResponse(decorators, requestPath, requestMethod, code, contentType, payload.toString(),
                    responseHeaders, requestHeaders, requestParams);

        }
    }

    private HttpTextResponse createTextResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                                final String requestPath,
                                                final String requestMethod,
                                                final int code,
                                                final String contentType,
                                                final String payload,
                                                final MultiMap<String, String> responseHeaders,
                                                final MultiMap<String, String> requestHeaders,
                                                final MultiMap<String, String> requestParams) {

        HttpTextResponse httpTextResponse = null;
        if (decorators.size() >= 0) {
            HttpTextResponseHolder holder = new HttpTextResponseHolder();
            for (HttpResponseDecorator decorator : decorators) {
                if (decorator.decorateTextResponse(holder, requestPath, requestMethod, code, contentType,
                        payload, responseHeaders, requestHeaders, requestParams)) {
                    httpTextResponse = holder.getHttpTextResponse();
                    break;
                }
            }
        }
        return httpTextResponse;
    }

    private HttpBinaryResponse createBinaryResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                                    final String requestPath,
                                                    final String requestMethod,
                                                    final int code,
                                                    final String contentType,
                                                    final byte[] payload,
                                                    final MultiMap<String, String> responseHeaders,
                                                    final MultiMap<String, String> requestHeaders,
                                                    final MultiMap<String, String> requestParams) {

        HttpBinaryResponse httpResponse = null;
        if (decorators.size() >= 0) {
            HttpBinaryResponseHolder holder = new HttpBinaryResponseHolder();

            for (HttpResponseDecorator decorator : decorators) {
                if (decorator.decorateBinaryResponse(
                        holder, requestPath, requestMethod, code, contentType,
                        payload, responseHeaders, requestHeaders, requestParams)) {
                    httpResponse = holder.getHttpBinaryResponse();
                    break;
                }
            }
        }
        return httpResponse;

    }
}

package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpBinaryResponse;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CopyOnWriteArrayList;

public class HttpResponseCreatorDefault implements HttpResponseCreator {


    public HttpResponse<?> createResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                               final String requestPath,
                                               final int code,
                                               final String contentType,
                                               final Object payload,
                                               final MultiMap<String, String> responseHeaders,
                                               final MultiMap<String, String> headers,
                                               final MultiMap<String, String> params) {

        if (decorators.size()==0) {
            return null;
        }

        if (payload instanceof byte[]) {
            return createBinaryResponse(decorators, requestPath, code, contentType, (byte[]) payload, headers);

        } else {

            return createTextResponse(decorators, requestPath, code, contentType, payload.toString(), headers);

        }
    }

    private HttpTextResponse createTextResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                               final String requestPath,
                                               final int code,
                                               final String contentType,
                                               final String payload,
                                               final MultiMap<String, String> headers) {

        HttpTextResponse httpTextResponse = null;
        if (decorators.size()>=0) {

            HttpTextResponse[] holder = new HttpTextResponse[1];

            for (HttpResponseDecorator decorator : decorators) {

                if (decorator.decorateTextResponse(requestPath, code, contentType, payload, headers, holder)) {
                    httpTextResponse = holder[0];
                    break;
                }
            }
        }

        return httpTextResponse;
    }

    private HttpBinaryResponse createBinaryResponse(final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                                   final String requestPath, int code, String contentType, byte[] payload,
                                    MultiMap<String, String> headers) {


        HttpBinaryResponse httpResponse = null;
        if (decorators.size()>=0) {

            HttpBinaryResponse[] holder = new HttpBinaryResponse[1];

            for (HttpResponseDecorator decorator : decorators) {

                if (decorator.decorateBinaryResponse(requestPath, code, contentType, payload, headers, holder)) {
                    httpResponse = holder[0];
                    break;
                }
            }
        }

        return httpResponse;

    }
}

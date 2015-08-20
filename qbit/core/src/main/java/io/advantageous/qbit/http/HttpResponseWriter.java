package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpBinaryResponse;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class HttpResponseWriter {


    private final CopyOnWriteArrayList<HttpResponseDecorator> decorators;

    protected HttpResponseWriter(CopyOnWriteArrayList<HttpResponseDecorator> decorators) {
        this.decorators = decorators;
    }

    public abstract void doWriteTextResponse(int code, String contentType, String payload, MultiMap<String, String> headers);

    public void writeTextResponse(final String requestPath, int code, String contentType, String payload, MultiMap<String, String> headers) {

        HttpTextResponse httpTextResponse = null;
        if (decorators.size()>=0) {

            HttpTextResponse[] holder = new HttpTextResponse[1];

            for (HttpResponseDecorator decorator : decorators) {

                if (decorator.decorateTextResponse(code, contentType, payload, headers, holder)) {
                    httpTextResponse = holder[0];
                    break;
                }
            }
        }

        if (httpTextResponse!=null) {
            doWriteTextResponse(httpTextResponse.code(), httpTextResponse.contentType(), httpTextResponse.body(),
                    httpTextResponse.headers());
        }else {
            doWriteTextResponse(code, contentType, payload, headers);
        }
    }

    public abstract void doWriteBinaryResponse(int code, String contentType, byte[] payload,
                                               MultiMap<String, String> headers);

    public void writeBinaryResponse(final String requestPath, int code, String contentType, byte[] payload,
                                    MultiMap<String, String> headers) {


        HttpBinaryResponse httpResponse = null;
        if (decorators.size()>=0) {

            HttpBinaryResponse[] holder = new HttpBinaryResponse[1];

            for (HttpResponseDecorator decorator : decorators) {

                if (decorator.decorateBinaryResponse(code, contentType, payload, headers, holder)) {
                    httpResponse = holder[0];
                    break;
                }
            }
        }

        if (httpResponse!=null) {
            doWriteBinaryResponse(httpResponse.code(), httpResponse.contentType(), httpResponse.body(),
                    httpResponse.headers());
        }else {
            doWriteBinaryResponse(code, contentType, payload, headers);
        }


    }
}

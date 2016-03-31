package io.advantageous.qbit.vertx.http.server;

import io.advantageous.qbit.http.HttpStatus;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.HttpResponseDecorator;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.util.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class VertxHttpResponseReceiver implements HttpResponseReceiver<Object> {

    private final HttpServerResponse response;
    private final CopyOnWriteArrayList<HttpResponseDecorator> decorators;
    private final HttpResponseCreator httpResponseCreator;
    private final String requestPath;
    private final MultiMap<String, String> requestHeaders;
    private final MultiMap<String, String> requestParams;

    public VertxHttpResponseReceiver(final String requestPath,
                                     final MultiMap<String, String> headers,
                                     final MultiMap<String, String> params,
                                     final HttpServerResponse response,
                                     final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                     final HttpResponseCreator httpResponseCreator) {
        this.response = response;
        this.decorators = decorators;
        this.httpResponseCreator = httpResponseCreator;
        this.requestPath = requestPath;
        this.requestHeaders = headers;
        this.requestParams = params;

    }

    private static Buffer createBuffer(Object body, HttpServerResponse response) {
        Buffer buffer = null;

        if (body instanceof byte[]) {
            byte[] bBody = ((byte[]) body);
            response.putHeader("Content-Length", String.valueOf(bBody.length));
            buffer = new Buffer(bBody);
        } else if (body instanceof String) {
            String sBody = ((String) body);
            byte[] bBody = sBody.getBytes(StandardCharsets.UTF_8);
            response.putHeader("Content-Length", String.valueOf(bBody.length));
            buffer = new Buffer(bBody);
        }
        return buffer;
    }

    @Override
    public void response(int code, String contentType, Object body) {

        response(code, contentType, body, MultiMap.empty());
    }

    @Override
    public void response(final int code, final String contentType, final Object body,
                         final MultiMap<String, String> responseHeaders) {


        final HttpResponse<?> response = decorators.size() > 0 ? httpResponseCreator.createResponse(
                decorators, requestPath, code, contentType, body,
                responseHeaders, this.requestHeaders,
                this.requestParams) : null;


        /** Response was decorated. */
        if (response == null) {
            doResponse(code, contentType, body, responseHeaders);
        } else {
            /** Response was not decorated. */
            doResponse(response.code(), response.contentType(), response.body(), response.headers());
        }
    }

    private void doResponse(int code, String contentType, Object body, MultiMap<String, String> headers) {
        if (!headers.isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : headers) {
                this.response.putHeader(entry.getKey(), entry.getValue());
            }
        }

        this.response.putHeader("Content-Type", contentType);
        this.response.setStatusCode(code);
        this.response.setStatusMessage(HttpStatus.message(code));


        Buffer buffer = createBuffer(body, this.response);
        this.response.end(buffer);
    }


}

package io.advantageous.qbit.vertx.http.server;

import io.advantageous.qbit.http.HttpStatus;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.util.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

public class VertxHttpResponseReceiver implements HttpResponseReceiver<Object> {

    private final HttpServerResponse response;

    public VertxHttpResponseReceiver(HttpServerResponse response) {
        this.response = response;
    }

    @Override
    public void response(int code, String contentType, Object body) {

        response(code, contentType, body, MultiMap.empty());
    }

    @Override
    public void response(final int code, final String contentType, final Object body,
                         final MultiMap<String, String> headers) {


        if (!headers.isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : headers) {
                response.putHeader(entry.getKey(), entry.getValue());
            }
        }

        response.putHeader("Content-Type", contentType);
        response.setStatusCode(code);
        response.setStatusMessage(HttpStatus.message(code));


        Buffer buffer = createBuffer(body, response);
        response.end(buffer);
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


}

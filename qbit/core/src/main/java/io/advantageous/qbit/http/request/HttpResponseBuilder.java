package io.advantageous.qbit.http.request;

import io.advantageous.qbit.http.request.impl.HttpBinaryResponseImpl;
import io.advantageous.qbit.http.request.impl.HttpTextResponseImpl;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.nio.charset.StandardCharsets;

/**
 * Builds an HTTP Response.
 */
public class HttpResponseBuilder {


    /**
     * Response code.
     */
    private int code;

    /**
     * Content Type, e.g., "application/json"
     */
    private String contentType;


    /**
     * Response body.
     */
    private Object body;

    /**
     * Response headers.
     */
    private MultiMap<String, String> headers;

    /**
     * Builder creator
     *
     * @return httpResponseBuilder
     */
    public static HttpResponseBuilder httpResponseBuilder() {
        return new HttpResponseBuilder();
    }

    public int getCode() {
        return code;
    }

    public HttpResponseBuilder setCode(final int code) {
        this.code = code;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public HttpResponseBuilder setContentType(final String contentType) {
        this.contentType = contentType;
        return this;
    }


    /**
     * Sets a JSON body.
     */
    public HttpResponseBuilder setJsonBody(final String json) {
        this.setContentType("application/json");
        this.setBody(json);
        return this;
    }


    /**
     * Sets a JSON body with OK (200) http code.
     */
    public HttpResponseBuilder setJsonBodyCodeOk(final String json) {
        this.setCode(200);
        this.setContentType("application/json");
        this.setBody(json);
        return this;
    }

    /**
     * Sets a JSON body with (500) http code.
     */
    public HttpResponseBuilder setJsonBodyError(final String json) {
        this.setCode(500);
        this.setContentType("application/json");
        this.setBody(json);
        return this;
    }

    public Object getBody() {
        return body;
    }

    public HttpResponseBuilder setBody(final Object body) {
        this.body = body;
        return this;
    }

    public MultiMap<String, String> getHeaders() {
        if (headers == null) {
            headers = new MultiMapImpl<>();
        }
        return headers;
    }

    public HttpResponseBuilder setHeaders(final MultiMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Add a header
     *
     * @param name  name
     * @param value value
     * @return HttpResponseBuilder
     */
    public HttpResponseBuilder addHeader(final String name, final String value) {
        getHeaders().put(name, value);
        return this;
    }

    /**
     * Build a response object.
     *
     * @return HttpResponse
     */
    public HttpResponse<?> build() {
        if (getBody() instanceof byte[]) {
            return new HttpBinaryResponseImpl(getCode(), getContentType(), (byte[]) getBody(), headers);
        } else {
            return new HttpTextResponseImpl(getCode(), getContentType(), getBody().toString(), headers);
        }
    }


    /**
     * Build a text response object.
     *
     * @return HttpResponse
     */
    public HttpTextResponse buildTextResponse() {
        if (getBody() instanceof byte[]) {
            return new HttpTextResponseImpl(getCode(), getContentType(),
                    new String((byte[]) getBody(), StandardCharsets.UTF_8), headers);
        } else {
            return new HttpTextResponseImpl(getCode(), getContentType(), getBody().toString(), headers);
        }
    }


    /**
     * Build a binary response object.
     *
     * @return HttpResponse
     */
    public HttpBinaryResponse buildBinaryResponse() {
        if (getBody() instanceof byte[]) {
            return new HttpBinaryResponseImpl(getCode(), getContentType(), (byte[]) getBody(), headers);
        } else {
            return new HttpBinaryResponseImpl(getCode(), getContentType(),
                    getBody().toString().getBytes(StandardCharsets.UTF_8), headers);
        }
    }
}

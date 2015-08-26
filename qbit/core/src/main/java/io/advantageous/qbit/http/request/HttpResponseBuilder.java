package io.advantageous.qbit.http.request;

import io.advantageous.qbit.http.request.impl.HttpBinaryResponseImpl;
import io.advantageous.qbit.http.request.impl.HttpTextResponseImpl;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

public class HttpResponseBuilder {


    private int code;
    private String contentType;
    private Object body;
    private MultiMap<String, String> headers;

    public HttpResponseBuilder httpResponseBuilder() {
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

    public HttpResponseBuilder addHeader(final String name, final String value) {
        getHeaders().put(name, value);
        return this;
    }

    public HttpResponseBuilder setHeaders(final MultiMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpResponse<?> build() {
        if (getBody() instanceof  byte[]) {
            return new HttpBinaryResponseImpl(getCode(), getContentType(), (byte[]) getBody(), headers);
        } else {
            return new HttpTextResponseImpl(getCode(), getContentType(), getBody().toString(), headers);
        }
    }
}

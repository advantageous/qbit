package io.advantageous.qbit.http.request.impl;

import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.util.MultiMap;

public class HttpTextResponseImpl implements HttpTextResponse {

    private final int code;
    private final String contentType;
    private final String body;
    private final MultiMap<String, String> headers;

    public HttpTextResponseImpl(int code, String contentType, String body, MultiMap<String, String> headers) {
        this.code = code;
        this.contentType = contentType;
        this.body = body;
        this.headers = headers;
    }

    @Override
    public MultiMap<String, String> headers() {
        return headers;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpTextResponseImpl{" +
                "code=" + code +
                ", contentType='" + contentType + '\'' +
                ", body=" + body +
                ", headers=" + headers +
                '}';
    }
}

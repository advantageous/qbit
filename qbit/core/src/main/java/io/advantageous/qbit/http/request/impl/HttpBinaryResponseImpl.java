package io.advantageous.qbit.http.request.impl;

import io.advantageous.qbit.http.request.HttpBinaryResponse;
import io.advantageous.qbit.util.MultiMap;

import java.util.Arrays;

public class HttpBinaryResponseImpl implements HttpBinaryResponse {

    private final int code;
    private final String contentType;
    private final byte[] body;
    private final MultiMap<String, String> headers;

    public HttpBinaryResponseImpl(int code, String contentType, byte[] body, MultiMap<String, String> headers) {
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
    public byte[] body() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpBinaryResponseImpl{" +
                "code=" + code +
                ", contentType='" + contentType + '\'' +
                ", body=" + Arrays.toString(body) +
                ", headers=" + headers +
                '}';
    }
}

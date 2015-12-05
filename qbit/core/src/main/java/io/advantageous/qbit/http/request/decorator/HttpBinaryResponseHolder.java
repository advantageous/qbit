package io.advantageous.qbit.http.request.decorator;

import io.advantageous.qbit.http.request.HttpBinaryResponse;

public class HttpBinaryResponseHolder {

    private HttpBinaryResponse httpBinaryResponse;

    public HttpBinaryResponse getHttpBinaryResponse() {
        return httpBinaryResponse;
    }

    public HttpBinaryResponseHolder setHttpBinaryResponse(HttpBinaryResponse httpBinaryResponse) {
        this.httpBinaryResponse = httpBinaryResponse;
        return this;
    }
}

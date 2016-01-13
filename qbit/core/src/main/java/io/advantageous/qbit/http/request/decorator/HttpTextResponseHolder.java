package io.advantageous.qbit.http.request.decorator;

import io.advantageous.qbit.http.request.HttpTextResponse;

public class HttpTextResponseHolder {

    private HttpTextResponse httpTextResponse;


    public HttpTextResponse getHttpTextResponse() {
        return httpTextResponse;
    }

    public HttpTextResponseHolder setHttpTextResponse(HttpTextResponse httpTextResponse) {
        this.httpTextResponse = httpTextResponse;
        return this;
    }


    public HttpTextResponse get() {
        return httpTextResponse;
    }

    public HttpTextResponseHolder set(HttpTextResponse httpTextResponse) {
        this.httpTextResponse = httpTextResponse;
        return this;
    }
}

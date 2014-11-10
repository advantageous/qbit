package io.advantageous.qbit.http;

import io.advantageous.qbit.util.MultiMap;

import java.nio.charset.StandardCharsets;

/**
 * Created by rhightower on 10/24/14.
 * @author rhightower
 */
public class HttpRequestBuilder {


    public static final byte[] EMPTY_STRING = "".getBytes(StandardCharsets.UTF_8);
    private  String uri;

    private  String contentType;
    private  String remoteAddress;
    private  MultiMap<String, String> params;
    private  String body;
    private  String method;
    private  HttpResponse response;
    private MultiMap<String, String> headers;


    public String getUri() {
        return uri;
    }

    public HttpRequestBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public HttpRequestBuilder setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public MultiMap<String, String> getParams() {
        return params;
    }

    public HttpRequestBuilder setParams(MultiMap<String, String> params) {
        this.params = params;
        return this;
    }

    public String getBody() {
        return body;
    }

    public HttpRequestBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public HttpRequestBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public HttpRequestBuilder setResponse(HttpResponse response) {
        this.response = response;
        return this;
    }

    public HttpRequest build() {
        return new HttpRequest(uri, method, params, headers,
                body != null ? body.getBytes(StandardCharsets.UTF_8) : EMPTY_STRING,
                remoteAddress, contentType, response);
    }

    public String getContentType() {
        return contentType;
    }

    public HttpRequestBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public MultiMap<String, String> getHeaders() {
        return headers;
    }

    public HttpRequestBuilder setHeaders(MultiMap<String, String> headers) {
        this.headers = headers;
        return this;
    }
}

package io.advantageous.qbit.http;

import io.advantageous.qbit.util.MultiMap;


/**
 * Created by rhightower on 10/21/14.
 * @author rhightower
 */
public class HttpRequest {

    private final String uri;
    private final String remoteAddress;
    private final MultiMap<String, String> params;

    private final MultiMap<String, String> headers;
    private final String body;
    private final String method;
    private final HttpResponse response;


    public HttpRequest(final String uri, final String method, final MultiMap<String, String> params,
                       final MultiMap<String, String> headers,
                       final String body, final String remoteAddress, final HttpResponse response) {
        this.params = params;
        this.body = body;
        this.method = method;
        this.uri = uri;
        this.response = response;
        this.remoteAddress = remoteAddress;
        this.headers = headers;
    }

    public MultiMap<String, String> getParams() {
        return params;
    }

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }


    public HttpResponse getResponse() {
        return response;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "uri='" + uri + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", params=" + params +
                ", body='" + body + '\'' +
                ", method='" + method + '\'' +
                ", response=" + response +
                '}';
    }
}

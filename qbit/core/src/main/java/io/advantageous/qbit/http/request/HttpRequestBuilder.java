/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.http.request;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.primitive.ByteBuf;
import io.advantageous.qbit.http.HttpContentTypes;
import io.advantageous.qbit.util.GzipUtils;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is a builder for creating HTTP request objects.
 * <p>
 * created by rhightower on 10/24/14.
 *
 * @author rhightower
 */
public class HttpRequestBuilder {


    public static final byte[] EMPTY_STRING = "".getBytes(StandardCharsets.UTF_8);
    private final static ThreadLocal<RequestIdGenerator> idGen = new ThreadLocal<RequestIdGenerator>() {
        @Override
        protected RequestIdGenerator initialValue() {
            return new RequestIdGenerator();
        }
    };
    private String uri = "/";
    private long id;
    private long timestamp;
    private String contentType;
    private String remoteAddress;
    private MultiMap<String, String> params;
    private MultiMap<String, String> headers;
    private String method = "GET";
    private Consumer<Exception> errorHandler;
    private Supplier<Object> body = () -> EMPTY_STRING;

    private Supplier<MultiMap<String, String>> formParamsSupplier = MultiMap::empty;

    private HttpResponseReceiver receiver = (code, mimeType, body1) -> {
    };
    private Map<String, Object> data;
    private int contentLength;

    public static HttpRequestBuilder httpRequestBuilder() {
        return new HttpRequestBuilder();
    }

    public Supplier<MultiMap<String, String>> getFormParamsSupplier() {
        return formParamsSupplier;
    }

    public HttpRequestBuilder setFormParamsSupplier(Supplier<MultiMap<String, String>> formParamsSupplier) {
        this.formParamsSupplier = formParamsSupplier;
        return this;
    }

    public Consumer<Exception> getErrorHandler() {
        return errorHandler;
    }

    public HttpRequestBuilder setErrorHandler(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public HttpRequestBuilder setMethodPost() {
        this.method = "POST";
        return this;
    }

    public HttpRequestBuilder setMethodOptions() {
        this.method = "OPTIONS";
        return this;
    }

    public HttpRequestBuilder setMethodGet() {
        this.method = "GET";
        return this;
    }

    public HttpRequestBuilder setMethodPut() {
        this.method = "PUT";
        return this;
    }

    public HttpRequestBuilder setMethodDelete() {
        this.method = "DELETE";
        return this;
    }

    public long getId() {
        return id;
    }

    public HttpRequestBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public HttpRequestBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

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

        return params == null ? MultiMap.EMPTY : params;
    }

    public HttpRequestBuilder setParams(MultiMap<String, String> params) {
        this.params = params;
        return this;
    }


    public HttpRequestBuilder setBody(String body) {
        setBodySupplier(() -> body);
        return this;
    }

    public String getMethod() {
        return method;
    }

    public HttpRequestBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpResponseReceiver getReceiver() {
        return receiver;
    }

    public <T> HttpRequestBuilder receiver(HttpResponseReceiver<T> receiver) {
        this.receiver = receiver;
        return this;
    }

    public HttpRequestBuilder setTextReceiver(HttpTextReceiver response) {
        this.receiver = response;
        return this;
    }

    public HttpRequestBuilder setResponse(HttpResponseReceiver response) {
        this.receiver = response;
        return this;
    }


    public HttpRequestBuilder setBinaryReceiver(HttpBinaryReceiver response) {
        this.receiver = response;
        return this;
    }

    public HttpRequest buildClientRequest() {

        String newURI = uri;

        if (params != null && params.size() > 1) {
            String paramString = paramString();
            switch (method) {
                case "GET":
                case "OPTION":
                case "HEAD":
                case "DELETE":
                    newURI = Str.add(uri, "?", paramString);
                    break;
                case "POST":
                case "PUT":
                    setBodyBytes(paramString.getBytes(StandardCharsets.UTF_8));
                    setContentType(HttpContentTypes.FORM);
                    break;
            }
        }

        HttpResponseReceiver httpResponse = buildHttpResponseReceiver();


        if (id == 0) {

            this.id = idGen.get().inc();
        }

        if (timestamp == 0) {
            timestamp = io.advantageous.qbit.util.Timer.timer().now();
        }

        if (contentType != null) {
            this.addHeader("Content-Type", contentType);
        }
        return new HttpRequest(this.getId(), newURI, this.getMethod(), this.getData(), this.getParams(),
                this.getHeaders(),
                this.getBodySupplier(),
                this.getRemoteAddress(), this.getContentType(), httpResponse,
                getFormParamsSupplier(), this.getTimestamp(), this.getContentLength());
    }


    public HttpRequest build() {

        String newURI = uri;

        HttpResponseReceiver httpResponse = buildHttpResponseReceiver();


        if (id == 0) {

            this.id = idGen.get().inc();
        }

        if (timestamp == 0) {
            timestamp = io.advantageous.qbit.util.Timer.timer().now();
        }

        if (contentType != null) {
            this.addHeader("Content-Type", contentType);
        }
        final HttpRequest request = new HttpRequest(this.getId(), newURI, this.getMethod(), this.getData(), this.getParams(),
                this.getHeaders(),
                this.getBodySupplier(),
                this.getRemoteAddress(), this.getContentType(), httpResponse,
                this.getFormParamsSupplier(),
                this.getTimestamp(), this.getContentLength());

        return request;
    }


    private HttpResponseReceiver buildHttpResponseReceiver() {
        HttpResponseReceiver httpResponse = this.getReceiver();

        if (errorHandler != null) {

            final HttpResponseReceiver innerHttpResponse = this.getReceiver();
            final Consumer<Exception> innerErrorHandler = this.getErrorHandler();
            httpResponse = new HttpResponseReceiver() {
                @Override
                public void response(int code, String contentType, Object body) {
                    //noinspection unchecked
                    innerHttpResponse.response(code, contentType, body);
                }

                @Override
                public boolean isText() {
                    return innerHttpResponse.isText();
                }

                @Override
                public void response(int code, String contentType, Object body, MultiMap headers) {
                    //noinspection unchecked
                    innerHttpResponse.response(code, contentType, body, headers);
                }

                @Override
                public Consumer<Exception> errorHandler() {
                    return innerErrorHandler;
                }

            };
        }
        return httpResponse;
    }

    public String getContentType() {
        return contentType;
    }

    public HttpRequestBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public MultiMap<String, String> getHeaders() {

        return headers == null ? MultiMap.EMPTY : headers;
    }

    public HttpRequestBuilder setHeaders(final Map<String, String> headerMap) {

        headerMap.entrySet().forEach(entry -> addHeader(entry.getKey(), entry.getValue()));
        return this;
    }

    public HttpRequestBuilder setHeaders(final MultiMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestBuilder setJsonContentType() {

        contentType = "application/json; charset=\"UTF-8\"";
        return this;
    }

    public HttpRequestBuilder setJsonBodyForPost(final String body) {
        setJsonContentType();
        this.setBody(body);
        this.setMethod("POST");
        return this;
    }

    public HttpRequestBuilder setJsonBodyForPut(final String body) {
        setJsonContentType();
        this.setBody(body);
        this.setMethod("PUT");
        return this;
    }

    public HttpRequestBuilder addHeader(final String name, final String value) {
        if (headers == null) {
            headers = new MultiMapImpl<>();
        }
        headers.put(name, value);
        return this;
    }

    public HttpRequestBuilder addParam(final String name, final String value) {
        if (params == null) {
            params = new MultiMapImpl<>();
        }
        params.put(name, value);
        return this;
    }

    public String paramString() {
        String paramString = "";


        if (params != null) {

            ByteBuf buf = ByteBuf.create(244);

            final Set<String> keys = params.keySet();

            int index = 0;
            for (String key : keys) {

                final Iterable<String> paramsAtKey = params.getAll(key);

                for (Object value : paramsAtKey) {
                    if (index > 0) {
                        buf.addByte('&');
                    }


                    buf.addUrlEncoded(key);
                    buf.addByte('=');

                    if (!(value instanceof byte[])) {
                        buf.addUrlEncoded(value.toString());
                    } else {
                        buf.addUrlEncodedByteArray((byte[]) value);
                    }
                    index++;
                }
            }

            paramString = buf.toString();
        }


        params = MultiMap.empty();
        return paramString;

    }


    public HttpRequestBuilder setFormPutAndCreateFormBody() {
        if (params.size() == 0) {
            throw new IllegalStateException("Form must have params, you must add the params before you call this method");
        }

        String paramString = paramString();
        setContentType(HttpContentTypes.FORM);
        setMethod("PUT");
        setBodyBytes(paramString.getBytes(StandardCharsets.UTF_8));

        return this;
    }

    public HttpRequestBuilder setFormPostAndCreateFormBody() {
        if (getParams().size() == 0) {
            throw new IllegalStateException("Form must have params, you must add the params before you call this method");
        }
        String paramString = paramString();

        setContentType(HttpContentTypes.FORM);
        setMethod("POST");

        setBodyBytes(paramString.getBytes(StandardCharsets.UTF_8));


        return this;
    }


    public HttpRequestBuilder setBodyBytes(byte[] bodyBytes) {

        this.body = () -> bodyBytes;
        return this;
    }

    public HttpRequestBuilder setJsonBodyForPostGzip(String jsonBodyForPostGzip) {
        try {
            this.setBodyBytes(GzipUtils.encode(jsonBodyForPostGzip));
            this.addHeader("Accept-Encoding", "gzip, deflate");
            this.addHeader("Content-Encoding", "gzip");
            this.setJsonContentType();
            this.setMethodPost();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return this;
    }


    public HttpRequestBuilder setJsonBodyForPutGzip(String jsonBodyForPostGzip) {
        try {
            this.setBodyBytes(GzipUtils.encode(jsonBodyForPostGzip));
            this.addHeader("Accept-Encoding", "gzip, deflate");
            this.addHeader("Content-Encoding", "gzip");
            this.setJsonContentType();
            this.setMethodPut();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return this;
    }

    public HttpRequestBuilder initFormIfNeeded() {

        if (getParams().size() == 0) {
            return this;
        }
        if (getMethod().equals("POST") || getMethod().equals("PUT")) {
            setContentType(HttpContentTypes.FORM);
            String paramString = paramString();
            setBodyBytes(paramString.getBytes(StandardCharsets.UTF_8));
        }


        return this;
    }

    /**
     * Copies the request's body, headers, uri, request params, content type, etc into this builder
     *
     * @param request request to copy
     * @return this
     */
    public HttpRequestBuilder copyRequest(final HttpRequest request) {


        this.setContentType(request.getContentType());

        this.setMethod(request.getMethod());

        if (request.getHeaders().size() > 0) {
            if (this.headers == null) {
                this.setHeaders(new MultiMapImpl<>());
            }
            final MultiMap<String, String> headers = this.getHeaders();
            request.getHeaders().forEach(entry -> {
                entry.getValue().forEach(value -> headers.add(entry.getKey(), value));
            });
        }

        if (request.getParams().size() > 0) {


            if (this.params == null) {
                this.setParams(new MultiMapImpl<>());
            }

            final MultiMap<String, String> params = this.getParams();
            request.getParams().forEach(entry -> {
                entry.getValue().forEach(value -> params.add(entry.getKey(), value));
            });
        }

        final byte[] body = request.getBody();
        this.setBodyBytes(body);
        this.setParams(request.getParams());
        this.setUri(request.getUri());


        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public HttpRequestBuilder setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public int getContentLength() {
        return contentLength;
    }

    public HttpRequestBuilder setContentLength(final int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public Supplier<Object> getBodySupplier() {
        return body;
    }

    public HttpRequestBuilder setBodySupplier(final Supplier<Object> body) {
        this.body = body;
        return this;
    }

    private static class RequestIdGenerator {
        private long value;

        private long inc() {
            return value++;
        }
    }
}

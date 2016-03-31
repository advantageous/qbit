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

import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.util.MultiMap;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;


/**
 * This represents and HTTP request.
 * <p>
 * created by rhightower on 10/21/14.
 *
 * @author rhightower
 */
public class HttpRequest implements Request<Object> {

    private final Map<String, Object> data;
    private final String uri;
    private final String remoteAddress;
    private final MultiMap<String, String> params;
    private final MultiMap<String, String> headers;
    private final String contentType;
    private final String method;
    private final HttpResponseReceiver receiver;
    private final long messageId;
    private final long timestamp;
    private final Supplier<Object> bodySupplier;
    private final Supplier<MultiMap<String, String>> formParamsSupplier;
    private final int contentLength;
    private MultiMap<String, String> formParams;
    private volatile boolean handled;
    private Object body;

    public HttpRequest(final long id,
                       final String uri,
                       final String method,
                       final Map<String, Object> data,
                       final MultiMap<String, String> params,
                       final MultiMap<String, String> headers,
                       final Supplier<Object> bodySupplier,
                       final String remoteAddress,
                       final String contentType,
                       final HttpResponseReceiver response,
                       final Supplier<MultiMap<String, String>> formParamsSupplier,
                       final long timestamp,
                       final int contentLength) {

        this.data = data;
        this.uri = uri;
        this.messageId = id;
        this.params = params;
        this.bodySupplier = bodySupplier;
        this.method = method;
        this.contentType = contentType;
        this.receiver = response;
        this.remoteAddress = remoteAddress;
        this.headers = headers;
        this.timestamp = timestamp;
        this.formParamsSupplier = formParamsSupplier;
        this.contentLength = contentLength;
    }

    @Override
    public String address() {
        return uri;
    }

    @Override
    public String returnAddress() {
        return remoteAddress;
    }

    @Override
    public MultiMap<String, String> params() {
        return params;
    }

    @Override
    public MultiMap<String, String> headers() {
        return headers;
    }


    public MultiMap<String, String> formParams() {
        if (formParams == null) {

            formParams = formParamsSupplier.get();

        }

        return formParams;

    }

    public MultiMap<String, String> getFormParams() {
        return formParams();
    }

    @Override
    public boolean hasParams() {
        return false;
    }

    @Override
    public boolean hasHeaders() {
        return false;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public synchronized boolean isHandled() {
        return handled;
    }

    @Override
    public synchronized void handled() {
        handled = true;
    }

    @Override
    public long id() {
        return messageId;
    }

    @Override
    public Object body() {
        if (body == null) {
            body = bodySupplier.get();
        }
        return body;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public MultiMap<String, String> getParams() {
        return params;
    }

    public byte[] getBody() {
        final Object body = body();
        if (body == null) {
            return null;
        }
        if (body instanceof byte[]) {
            return ((byte[]) body);
        } else {
            return body.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    public String getBodyAsString() {
        final Object body = body();
        if (body == null) {
            return null;
        }
        if (body instanceof byte[]) {
            return new String(((byte[]) body), StandardCharsets.UTF_8);
        } else {
            return body.toString();
        }
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Object> data() {
        return data;
    }

    public String getUri() {
        return uri;
    }


    public HttpResponseReceiver<Object> getReceiver() {
        //noinspection unchecked
        return receiver;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public MultiMap<String, String> getHeaders() {
        return headers;
    }


    public long getMessageId() {
        return messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpRequest request = (HttpRequest) o;

        if (messageId != request.messageId) return false;
        if (timestamp != request.timestamp) return false;
        if (!method.equals(request.method)) return false;
        return uri.equals(request.uri);

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    public String getContentType() {

        if (contentType == null) {
            return headers != MultiMap.EMPTY ? headers.get("Content-Type") : "";
        } else {
            return contentType;
        }

    }


    public boolean isJson() {
        return "application/json".equals(contentType);
    }

    public int getContentLength() {
        return contentLength;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "uri='" + uri + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", params=" + params +
                ", headers=" + headers +
                //", body=" + getBodyAsString() +
                ", contentType='" + contentType + '\'' +
                ", method='" + method + '\'' +
                ", receiver=" + receiver +
                ", messageId=" + messageId +
                ", timestamp=" + timestamp +
                ", handled=" + handled +
                '}';
    }
}

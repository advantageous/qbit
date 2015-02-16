/*******************************************************************************

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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.http.request;

import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.boon.Str;
import org.boon.primitive.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This is a builder for creating HTTP request objects.
 * <p>
 * Created by rhightower on 10/24/14.
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
    private String uri;
    private long id;
    private long timestamp;
    private String contentType;
    private String remoteAddress;
    private MultiMap<String, String> params;
    private MultiMap<String, String> headers;
    private String body;
    private String method = "GET";
    private Consumer<Exception> errorHandler;

    private HttpResponseReceiver response = (code, mimeType, body1) -> {
    };

    public static HttpRequestBuilder httpRequestBuilder() {
        return new HttpRequestBuilder();
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

    public HttpResponseReceiver getResponse() {
        return response;
    }

    public HttpRequestBuilder response(HttpResponseReceiver response) {
        this.response = response;
        return this;
    }

    public HttpRequestBuilder setTextResponse(HttpTextResponse response) {
        this.response = response;
        return this;
    }

    public HttpRequestBuilder setBinaryResponse(HttpBinaryResponse response) {
        this.response = response;
        return this;
    }

    public HttpRequest build() {

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
                    body = paramString;
                    contentType = "application/x-www-form-urlencoded";
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
        return new HttpRequest(this.getId(), newURI, this.getMethod(), this.getParams(),
                this.getHeaders(),
                this.getBody() != null ? this.getBody().getBytes(StandardCharsets.UTF_8) : EMPTY_STRING,
                this.getRemoteAddress(), this.getContentType(), httpResponse, this.getTimestamp());
    }

    private HttpResponseReceiver buildHttpResponseReceiver() {
        HttpResponseReceiver httpResponse = this.getResponse();

        if (errorHandler != null) {

            final HttpResponseReceiver innerHttpResponse = this.getResponse();
            final Consumer<Exception> innerErrorHandler = this.getErrorHandler();
            httpResponse = new HttpResponseReceiver() {
                @Override
                public void response(int code, String contentType, Object body) {
                    innerHttpResponse.response(code, contentType, body);
                }

                @Override
                public boolean isText() {
                    return innerHttpResponse.isText();
                }

                @Override
                public void response(int code, String contentType, Object body, MultiMap headers) {
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

    public HttpRequestBuilder setHeaders(MultiMap<String, String> headers) {
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

    private String paramString() {
        String paramString = null;


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


        return paramString;

    }

    private static class RequestIdGenerator {
        private long value;

        private long inc() {
            return value++;
        }
    }
}

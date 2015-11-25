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

package io.advantageous.qbit.vertx.http.server;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.StringScanner;
import io.advantageous.qbit.http.HttpContentTypes;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.HttpResponseDecorator;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.http.websocket.impl.WebSocketImpl;
import io.advantageous.qbit.network.impl.NetSocketBase;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;


public class VertxServerUtils {
    private final Logger logger = LoggerFactory.getLogger(VertxServerUtils.class);

    private AtomicLong requestId = new AtomicLong();
    private volatile long time;


    public void setTime(long time) {
        this.time = time;
    }

    public HttpRequest createRequest(final HttpServerRequest request, final Buffer buffer,
                                     final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
                                     final HttpResponseCreator httpResponseCreator) {

        final MultiMap<String, String> headers = request.headers().size() == 0 ? MultiMap.empty() :
                new MultiMapWrapper(request.headers());

        final String contentType = request.headers().get("Content-Type");



        final byte[] body = HttpContentTypes.isFormContentType(contentType) ||
                buffer == null ?
                new byte[0] : buffer.getBytes();

        final MultiMap<String, String> params = buildParams(request, contentType);

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();

        final String requestPath = request.path();

        httpRequestBuilder.setId(requestId.incrementAndGet())
                .setUri(requestPath).setMethod(request.method().toString())
                .setParams(params).setBodyBytes(body)
                .setRemoteAddress(request.remoteAddress().toString())
                .setResponse(createResponse(requestPath, headers, params, request.response(), decorators, httpResponseCreator))
                .setTimestamp(time == 0L ? Timer.timer().now() : time)
                .setHeaders(headers);


        return httpRequestBuilder.build();
    }

    private MultiMap<String, String> buildParams(final HttpServerRequest request,
                                                 final String contentType) {

        if (HttpContentTypes.isFormContentType(contentType)) {

            if (request.params().size()==0) {
                return new MultiMapWrapper(request.formAttributes());
            } else {
                MultiMap<String, String> newParams = new MultiMapImpl<>();

                request.formAttributes().forEach(entry ->
                        newParams.add(entry.getKey(), entry.getValue()));

                request.params().forEach(entry ->
                        newParams.add(entry.getKey(), entry.getValue()));
                return newParams;

            }
        } else {
            return request.params().size() == 0 ? MultiMap.empty()
                    : new MultiMapWrapper(request.params());

        }

    }

    private HttpResponseReceiver createResponse(
            final String requestPath,
            MultiMap<String, String> headers, MultiMap<String, String> params, final HttpServerResponse response,
            final CopyOnWriteArrayList<HttpResponseDecorator> decorators,
            final HttpResponseCreator httpResponseCreator) {

        return new VertxHttpResponseReceiver(requestPath, headers, params, response, decorators, httpResponseCreator);

    }

    public WebSocket createWebSocket(final ServerWebSocket vertxServerWebSocket) {


        final MultiMap<String, String> params = paramMap(vertxServerWebSocket);

        final MultiMap<String, String> headers =
                vertxServerWebSocket.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(
                        vertxServerWebSocket.headers());


        /* Create a websocket that uses vertxServerWebSocket to forwardEvent messages. */
        final WebSocket webSocket = webSocketBuilder().setUri(vertxServerWebSocket.uri())
                .setRemoteAddress(vertxServerWebSocket.remoteAddress().toString())
                .setWebSocketSender(new WebSocketSender() {
                    @Override
                    public void sendText(String message) {
                        vertxServerWebSocket.writeFinalTextFrame(message);
                    }

                    @Override
                    public void sendBytes(byte[] message) {
                        vertxServerWebSocket.writeFinalBinaryFrame(Buffer.buffer(message));
                    }

                    @Override
                    public void close() {
                        vertxServerWebSocket.close();
                    }
                })
                .setHeaders(headers)
                .setParams(params)
                .build();

        /* Handle open. */
        webSocket.onOpen();

        /* Handle close. */
        vertxServerWebSocket.closeHandler(event -> webSocket.onClose());

        final Buffer[] bufferRef = new Buffer[1];


        /* Handle message. */
        vertxServerWebSocket.handler(buffer -> {
            bufferRef[0] = buffer;
        });

        /* Handle frame. */
        vertxServerWebSocket.frameHandler(event -> {
            if (event.isFinal()) {
                if (event.isBinary()) {
                    ((NetSocketBase) webSocket).setBinary();
                    webSocket.onBinaryMessage(bufferRef[0].getBytes());
                } else {
                    final String message = bufferRef[0].toString("UTF-8");
                    webSocket.onTextMessage(message);
                }
            }
        });


        /* Handle error. */
        vertxServerWebSocket.exceptionHandler(event -> {
            if (event instanceof Exception) {
                webSocket.onError((Exception) event);
            } else {
                webSocket.onError(new Exception(event));
            }
        });

        return webSocket;
    }

    private MultiMap<String, String> paramMap(ServerWebSocket vertxServerWebSocket) {
        String query = vertxServerWebSocket.query();
        MultiMap<String, String> paramMap = MultiMap.empty();

        if (!Str.isEmpty(query)) {
            final String[] params = StringScanner.split(query, '&');

            if (params.length > 0) {
                paramMap = new MultiMapImpl<>();

                for (String param : params) {
                    final String[] keyValue = StringScanner.split(param, '=');

                    if (keyValue.length == 2) {

                        String key = keyValue[0];
                        String value = keyValue[1];
                        try {
                            key = URLDecoder.decode(key, "UTF-8");
                            value = URLDecoder.decode(value, "UTF-8");
                            paramMap.add(key, value);
                        } catch (UnsupportedEncodingException e) {
                            logger.warn(sputs("Unable to url decode key or value in param", key, value), e);
                        }
                    }
                }
            }
        }
        return paramMap;
    }
}

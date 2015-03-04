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

import io.advantageous.boon.Str;
import io.advantageous.boon.StringScanner;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;


/**
 * Created by rhightower on 2/15/15.
 */
public class VertxServerUtils {
    private final Logger logger = LoggerFactory.getLogger(VertxServerUtils.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();


    volatile long requestId;
    volatile long time;

    private static Buffer createBuffer(Object body) {
        Buffer buffer = null;

        if (body instanceof byte[]) {

            byte[] bBody = ((byte[]) body);
            buffer = new Buffer(bBody);

        } else if (body instanceof String) {

            String sBody = ((String) body);
            buffer = new Buffer(sBody, "UTF-8");
        }
        return buffer;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public HttpRequest createRequest(final HttpServerRequest request, final Buffer buffer) {


        final MultiMap<String, String> params = request.params().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.params());
        final MultiMap<String, String> headers = request.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.headers());
        final byte[] body = buffer == null ? "".getBytes(StandardCharsets.UTF_8) : buffer.getBytes();

        final String contentType = request.headers().get("Content-Type");//TODO should this be accept?

        return new HttpRequest(requestId++, request.path(), request.method(), params, headers, body,
                request.remoteAddress().toString(),
                contentType, createResponse(request.response()), time);
    }

    private HttpResponseReceiver createResponse(final HttpServerResponse response) {
        return (code, mimeType, body) -> {

            //TODO put the rest of the headers here
            response.setStatusCode(code).putHeader("Content-Type", mimeType);
            //response.setStatusCode(code).putHeader("Keep-Alive", "timeout=600");
            Buffer buffer = createBuffer(body);
            response.end(buffer);

        };
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
                        vertxServerWebSocket.writeTextFrame(message);
                    }

                    @Override
                    public void sendBytes(byte[] message) {
                        vertxServerWebSocket.writeBinaryFrame(new Buffer(message));
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


        /* Handle message. */
        vertxServerWebSocket.dataHandler(buffer -> {
            final String message = buffer.toString("UTF-8");
            webSocket.onTextMessage(message);
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
                            continue;
                        }
                    }

                }

            }
        }
        return paramMap;
    }


}

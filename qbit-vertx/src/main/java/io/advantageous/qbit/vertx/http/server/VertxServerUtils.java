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

package io.advantageous.qbit.vertx.http.server;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.MultiMapWrapper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;

import java.nio.charset.StandardCharsets;

import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;

/**
 * Created by rhightower on 2/15/15.
 */
public class VertxServerUtils {


    volatile long requestId;
    volatile long time;
    volatile long webSocketId;

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

    public WebSocketMessage createWebSocketMessage(final ServerWebSocket serverWebSocket, final Buffer buffer) {


        return createWebSocketMessage(serverWebSocket.uri(), serverWebSocket.remoteAddress().toString(),

                new WebSocketSender() {
                    @Override
                    public void sendText(String message) {
                        serverWebSocket.writeTextFrame(message);
                    }

                    @Override
                    public void sendBytes(byte[] message) {
                        serverWebSocket.writeBinaryFrame(new Buffer(message));

                    }
                }, buffer != null ? buffer.toString("UTF-8") : "");
    }

    public WebSocketMessage createWebSocketMessage(final String address, final String returnAddress, final WebSocketSender webSocketSender, final String message) {


        return new WebSocketMessage(webSocketId, time, address, message, returnAddress, webSocketSender);
    }

    public HttpRequest createRequest(final HttpServerRequest request, final Buffer buffer) {


        final MultiMap<String, String> params = request.params().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.params());
        final MultiMap<String, String> headers = request.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(request.headers());
        final byte[] body = buffer == null ? "".getBytes(StandardCharsets.UTF_8) : buffer.getBytes();

        final String contentType = request.headers().get("Content-Type");

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

        /* Create a websocket that uses vertxServerWebSocket to send messages. */
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
                .build();


        /* Handle open. */
        webSocket.onOpen();

        /* Handle close. */
        vertxServerWebSocket.closeHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                webSocket.onClose();

            }
        });

//        vertxServerWebSocket.endHandler(event -> {
//            webSocket.onClose();
//        });

        /* Handle message. */
        vertxServerWebSocket.dataHandler(buffer -> {
            final String message = buffer.toString("UTF-8");
            webSocket.onTextMessage(message);
        });

        /* Handle error. */
        vertxServerWebSocket.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {
                if (event instanceof Exception) {
                    webSocket.onError((Exception) event);
                } else {
                    webSocket.onError(new Exception(event));
                }
            }
        });

        return webSocket;
    }


}

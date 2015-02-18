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

package io.advantageous.qbit.http.server;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.server.Server;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents an HTTP server.
 * Has the ability to register callbacks.
 * <p>
 * Created by rhightower on 10/22/14.
 *
 * @author rhightower
 */
public interface HttpServer extends Server {

    default void setWebSocketOnOpenConsumer(Consumer<WebSocket> onOpenConsumer) {
        throw new RuntimeException("Not supported");
    }

    void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer);

    void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer);

    void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer);

    default void setShouldContinueHttpRequest(Predicate<HttpRequest> predicate) {
        throw new RuntimeException("Not supported");
    }


    default void setShouldContinueWebSocket(Predicate<WebSocket> predicate) {
        throw new RuntimeException("Not supported");
    }

    void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer);

    void setWebSocketIdleConsume(Consumer<Void> idleConsumer);

    default HttpServer startServer() {
        start();
        return this;
    }


}

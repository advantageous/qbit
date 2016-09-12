/**
 * ****************************************************************************
 * <p>
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 * http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 * http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 * http://rick-hightower.blogspot.com/2015/01/quick-startClient-qbit-programming.html
 * http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 * http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 * <p>
 * ****************************************************************************
 */

package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.service.Startable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents an HTTP transport.
 *
 * @author rhightower
 *         on 2/19/15.
 */
public interface HttpTransport extends Startable {


    default void setWebSocketOnOpenConsumer(Consumer<WebSocket> onOpenConsumer) {
        throw new RuntimeException("Not supported");
    }


    default void setOnStart(Runnable runnable) {
    }


    default void setOnError(Consumer<Throwable> exceptionConsumer) {
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

    default void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {
       
    }

    default HttpTransport startTransport() {
        start();
        return this;
    }

}

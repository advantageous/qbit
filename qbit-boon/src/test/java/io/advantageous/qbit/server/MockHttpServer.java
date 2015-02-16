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

package io.advantageous.qbit.server;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpResponseReceiver;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.util.MultiMap;
import org.boon.Boon;

import java.util.function.Consumer;

/**
 * Created by rhightower on 10/24/14.
 *
 * @author rhightower
 */
public class MockHttpServer implements HttpServer {

    Consumer<WebSocketMessage> webSocketMessageConsumer;

    Consumer<HttpRequest> httpRequestConsumer;
    volatile long messageId = 0;
    private Consumer<Void> idleHttpRequestConsumer;
    private Consumer<Void> idleWebSocketConsumer;


    public void postRequestObject(final String uri, final Object body, final HttpResponseReceiver response) {

        final String json = Boon.toJson(body);
        final HttpRequest request = new HttpRequestBuilder().setUri(uri).setBody(json).setMethod("POST").response(response).setRemoteAddress("localhost:9999").build();
        this.httpRequestConsumer.accept(request);


    }


//    public void zendWebSocketMessage(final String uri, final Object args, WebSocketSender socketSender) {
//
//
//        final MethodCall<Object> methodCall =
//                QBit.factory().createMethodCallToBeEncodedAndSent(messageId++, uri, "client1",
//                        null, null, System.currentTimeMillis(), args, null);
//        final String message = QBit.factory().createEncoder().encodeAsString(methodCall);
//        final WebSocketMessage webSocketMessage = new WebSocketMessageBuilder()
//                .setMessage(message).setSender(socketSender).build();
//        this.webSocketMessageConsumer.accept(webSocketMessage);
//        this.idleWebSocketConsumer.accept(null);
//
//    }


    public void sendHttpGet(final String uri, final MultiMap<String, String> params, final HttpTextResponse response) {

        final HttpRequest request = new HttpRequestBuilder().setUri(uri).setParams(params).setMethod("GET").setTextResponse(response).setRemoteAddress("localhost:9999").build();
        this.httpRequestConsumer.accept(request);
        this.idleHttpRequestConsumer.accept(null);


    }

    @Override
    public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.webSocketMessageConsumer = webSocketMessageConsumer;


    }

    @Override
    public void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

    }

    @Override
    public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;

    }

    @Override
    public void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer) {
        this.idleHttpRequestConsumer = idleConsumer;

    }

    @Override
    public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {
        this.idleWebSocketConsumer = idleConsumer;

    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

}

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

package io.advantageous.qbit.http.client;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.advantageous.qbit.http.request.HttpRequestBuilder.httpRequestBuilder;

/**
 * This is an interface that allows users to send HTTP requests to a server.
 * <p>
 * Created by rhightower on 10/28/14.
 *
 * @author rhightower
 */
public interface HttpClient {

    void sendHttpRequest(HttpRequest request);

    default void sendGetRequest(String uri) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .build();

        sendHttpRequest(httpRequest);
    }

    default HttpResponse get(String uri) {
        return sendGetRequestAndWait(uri, 30, TimeUnit.SECONDS);
    }

    default HttpResponse sendGetRequestAndWait(String uri) {
        return sendGetRequestAndWait(uri, 30, TimeUnit.SECONDS);
    }

    default HttpResponse sendGetRequestAndWait(String uri, long wait, TimeUnit timeUnit) {


        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicReference<HttpResponse> httpResponseAtomicReference = new AtomicReference<>();

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setTextResponse(new HttpTextResponse() {
                    @Override
                    public void response(int code, String contentType, String body) {
                        response(code, contentType, body, MultiMap.EMPTY);
                    }

                    @Override
                    public void response(
                            final int code,
                            final String contentType,
                            final String body,
                            final MultiMap<String, String> headers) {

                        httpResponseAtomicReference.set(
                                new HttpResponse() {
                                    @Override
                                    public MultiMap<String, String> headers() {
                                        return headers;
                                    }

                                    @Override
                                    public int code() {
                                        return code;
                                    }

                                    @Override
                                    public String contentType() {
                                        return contentType;
                                    }

                                    @Override
                                    public String body() {
                                        return body;
                                    }
                                }
                        );
                        countDownLatch.countDown();
                    }
                })
                .build();

        sendHttpRequest(httpRequest);

        try {
            countDownLatch.await(wait, timeUnit);
        } catch (InterruptedException e) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.interrupted();
            }
        }
        return httpResponseAtomicReference.get();

    }


    default void sendGetRequest1Param(String uri, String key, Object value) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).addParam(key, value == null ? "" : value.toString())
                .build();

        sendHttpRequest(httpRequest);
    }


    default void sendGetRequest2Params(String uri,
                                       String key, Object value,
                                       String key1, Object value1) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .build();

        sendHttpRequest(httpRequest);
    }


    default void sendGetRequest3Params(String uri,
                                       String key, Object value,
                                       String key1, Object value1,
                                       String key2, Object value2) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value1 == null ? "" : value2.toString())
                .build();

        sendHttpRequest(httpRequest);
    }

    default void sendGetRequest4Params(String uri,
                                       String key, Object value,
                                       String key1, Object value1,
                                       String key2, Object value2,
                                       String key3, Object value3) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value1 == null ? "" : value2.toString())
                .addParam(key2, value3 == null ? "" : value3.toString())
                .build();

        sendHttpRequest(httpRequest);
    }


    default void sendPost(final String uri,
                          final String contentType,
                          final String body) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setBody(body).setContentType(contentType).setMethodPost()
                .build();

        sendHttpRequest(httpRequest);
    }


    default void sendJsonPost(final String uri,
                              final String body) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPost(body).setMethodPost()
                .build();

        sendHttpRequest(httpRequest);
    }


    default WebSocket createWebSocket(String uri) {
        throw new RuntimeException("New way to send messages");
    }

    void periodicFlushCallback(Consumer<Void> periodicFlushCallback);


    HttpClient start();

    void flush();

    void stop();

}

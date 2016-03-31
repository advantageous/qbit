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

package io.advantageous.qbit.http.client;

import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.qbit.http.request.*;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.service.ServiceFlushable;
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.advantageous.qbit.http.request.HttpRequestBuilder.httpRequestBuilder;

/**
 * This is an interface that allows users to send HTTP requests to a server.
 * <p>
 * created by rhightower on 10/28/14.
 *
 * @author rhightower
 */
public interface HttpClient extends ServiceFlushable, Stoppable, Startable {

    int HTTP_CLIENT_DEFAULT_TIMEOUT = Sys.sysProp(
            "io.advantageous.qbit.http.client.HttpClient.timeout", 180);

    static void _createHttpTextReceiver(final HttpRequest httpRequest,
                                        final CountDownLatch countDownLatch,
                                        final CountDownLatch countDownLatch2,
                                        final AtomicReference<HttpTextResponse> httpResponseAtomicReference) {

        final HttpTextReceiver httpTextReceiver = new HttpTextReceiver() {
            @Override
            public void response(int code, String contentType, String body) {
                response(code, contentType, body, MultiMap.empty());
            }

            @Override
            public void response(
                    final int code,
                    final String contentType,
                    final String body,
                    final MultiMap<String, String> headers) {

                httpResponseAtomicReference.set(
                        new HttpTextResponse() {
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

                            public String toString() {
                                return ("HttpTextResponse(" + "code:" + code +
                                        "contentType:" + contentType +
                                        "\nbody:\n" +
                                        body + "\n)"
                                );
                            }

                        }
                );
                countDownLatch.countDown();
                countDownLatch2.countDown();
            }
        };

        BeanUtils.idx(httpRequest, "receiver", httpTextReceiver);
    }

    default boolean isClosed() {
        return false;
    }

    default void checkClosed() {
        if (isClosed()) {
            throw new HttpClientClosedConnectionException();
        }
    }

    default void sendHttpRequest(HttpRequest request) {
    }

    default void sendGetRequest(String uri) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .build();
        sendHttpRequest(httpRequest);
    }

    default void getAsync(final String uri, final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setTextReceiver(httpTextReceiver)
                .build();
        sendHttpRequest(httpRequest);
    }

    default void getAsyncWith1Param(final String uri,
                                    String paramName0, Object value0,
                                    final HttpTextReceiver httpTextReceiver) {
        sendAsyncRequestWith1Param(uri, "GET", paramName0, value0, httpTextReceiver);
    }

    default void getAsyncWith2Params(final String uri,
                                     String paramName0, Object value0,
                                     String paramName1, Object value1,
                                     final HttpTextReceiver httpTextReceiver) {

        sendAsyncRequestWith2Params(uri, "GET",
                paramName0, value0,
                paramName1, value1,
                httpTextReceiver);
    }

    default void getAsyncWith3Params(final String uri,
                                     String paramName0, Object value0,
                                     String paramName1, Object value1,
                                     String paramName2, Object value2,
                                     final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith3Params(uri, "GET",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                httpTextReceiver);
    }

    default void getAsyncWith4Params(final String uri,
                                     String paramName0, Object value0,
                                     String paramName1, Object value1,
                                     String paramName2, Object value2,
                                     String paramName3, Object value3,
                                     final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith4Params(uri, "GET",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                paramName3, value3,
                httpTextReceiver);
    }

    default void getAsyncWith5Params(final String uri,
                                     String paramName0, Object value0,
                                     String paramName1, Object value1,
                                     String paramName2, Object value2,
                                     String paramName3, Object value3,
                                     String paramName4, Object value4,
                                     final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith5Params(uri, "GET",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                paramName3, value3,
                paramName4, value4,
                httpTextReceiver);
    }

    default void postAsync(final String uri, final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setTextReceiver(httpTextReceiver)
                .setMethod("POST")
                .build();
        sendHttpRequest(httpRequest);
    }

    default void postFormAsyncWith1Param(final String uri,
                                         String paramName0, Object value0,
                                         final HttpTextReceiver httpTextReceiver) {
        sendAsyncRequestWith1Param(uri, "POST", paramName0, value0, httpTextReceiver);
    }

    default void postFormAsyncWith2Params(final String uri,
                                          String paramName0, Object value0,
                                          String paramName1, Object value1,
                                          final HttpTextReceiver httpTextReceiver) {

        sendAsyncRequestWith2Params(uri, "POST",
                paramName0, value0,
                paramName1, value1,
                httpTextReceiver);
    }

    default void postFormAsyncWith3Params(final String uri,
                                          String paramName0, Object value0,
                                          String paramName1, Object value1,
                                          String paramName2, Object value2,
                                          final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith3Params(uri, "POST",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                httpTextReceiver);
    }

    default void postFormAsyncWith4Params(final String uri,
                                          String paramName0, Object value0,
                                          String paramName1, Object value1,
                                          String paramName2, Object value2,
                                          String paramName3, Object value3,
                                          final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith4Params(uri, "POST",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                paramName3, value3,
                httpTextReceiver);
    }

    default void postFormAsyncWith5Params(final String uri,
                                          String paramName0, Object value0,
                                          String paramName1, Object value1,
                                          String paramName2, Object value2,
                                          String paramName3, Object value3,
                                          String paramName4, Object value4,
                                          final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith5Params(uri, "POST",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                paramName3, value3,
                paramName4, value4,
                httpTextReceiver);
    }

    default void putAsync(final String uri, final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setTextReceiver(httpTextReceiver)
                .setMethod("PUT")
                .build();
        sendHttpRequest(httpRequest);
    }

    default void putFormAsyncWith1Param(final String uri,
                                        String paramName0, Object value0,
                                        final HttpTextReceiver httpTextReceiver) {
        sendAsyncRequestWith1Param(uri, "PUT", paramName0, value0, httpTextReceiver);
    }

    default void putFormAsyncWith2Params(final String uri,
                                         String paramName0, Object value0,
                                         String paramName1, Object value1,
                                         final HttpTextReceiver httpTextReceiver) {

        sendAsyncRequestWith2Params(uri, "PUT",
                paramName0, value0,
                paramName1, value1,
                httpTextReceiver);
    }

    default void putFormAsyncWith3Params(final String uri,
                                         String paramName0, Object value0,
                                         String paramName1, Object value1,
                                         String paramName2, Object value2,
                                         final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith3Params(uri, "PUT",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                httpTextReceiver);
    }

    default void putFormAsyncWith4Params(final String uri,
                                         String paramName0, Object value0,
                                         String paramName1, Object value1,
                                         String paramName2, Object value2,
                                         String paramName3, Object value3,
                                         final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith4Params(uri, "PUT",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                paramName3, value3,
                httpTextReceiver);
    }

    default void putFormAsyncWith5Params(final String uri,
                                         String paramName0, Object value0,
                                         String paramName1, Object value1,
                                         String paramName2, Object value2,
                                         String paramName3, Object value3,
                                         String paramName4, Object value4,
                                         final HttpTextReceiver httpTextReceiver) {


        sendAsyncRequestWith5Params(uri, "PUT",
                paramName0, value0,
                paramName1, value1,
                paramName2, value2,
                paramName3, value3,
                paramName4, value4,
                httpTextReceiver);
    }

    default void sendAsyncRequestWith1Param(final String uri, String method,
                                            String paramName0, Object value0,
                                            final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod(method)
                .setUri(uri).setTextReceiver(httpTextReceiver)
                .addParam(paramName0, value0 == null ? "" : value0.toString())
                .initFormIfNeeded()
                .build();

        sendHttpRequest(httpRequest);

    }

    default void sendAsyncRequestWith2Params(final String uri, String method,
                                             String paramName0, Object value0,
                                             String paramName1, Object value1,
                                             final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod(method)
                .setUri(uri).setTextReceiver(httpTextReceiver)
                .addParam(paramName0, value0 == null ? "" : value0.toString())
                .addParam(paramName1, value1 == null ? "" : value1.toString())
                .initFormIfNeeded()
                .build();
        sendHttpRequest(httpRequest);
    }

    default void sendAsyncRequestWith3Params(final String uri, String method,
                                             String paramName0, Object value0,
                                             String paramName1, Object value1,
                                             String paramName2, Object value2,
                                             final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod(method)
                .setUri(uri).setTextReceiver(httpTextReceiver)
                .addParam(paramName0, value0 == null ? "" : value0.toString())
                .addParam(paramName1, value1 == null ? "" : value1.toString())
                .addParam(paramName2, value2 == null ? "" : value2.toString())
                .initFormIfNeeded()
                .build();
        sendHttpRequest(httpRequest);
    }

    default void sendAsyncRequestWith4Params(final String uri, String method,
                                             String paramName0, Object value0,
                                             String paramName1, Object value1,
                                             String paramName2, Object value2,
                                             String paramName3, Object value3,
                                             final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod(method)
                .setUri(uri).setTextReceiver(httpTextReceiver)
                .addParam(paramName0, value0 == null ? "" : value0.toString())
                .addParam(paramName1, value1 == null ? "" : value1.toString())
                .addParam(paramName2, value2 == null ? "" : value2.toString())
                .addParam(paramName3, value3 == null ? "" : value3.toString())
                .initFormIfNeeded()
                .build();
        sendHttpRequest(httpRequest);
    }

    default void sendAsyncRequestWith5Params(final String uri, String method,
                                             String paramName0, Object value0,
                                             String paramName1, Object value1,
                                             String paramName2, Object value2,
                                             String paramName3, Object value3,
                                             String paramName4, Object value4,
                                             final HttpTextReceiver httpTextReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod(method)
                .setUri(uri)
                .setTextReceiver(httpTextReceiver)
                .addParam(paramName0, value0 == null ? "" : value0.toString())
                .addParam(paramName1, value1 == null ? "" : value1.toString())
                .addParam(paramName2, value2 == null ? "" : value2.toString())
                .addParam(paramName3, value3 == null ? "" : value3.toString())
                .addParam(paramName4, value4 == null ? "" : value4.toString())
                .initFormIfNeeded()
                .build();
        sendHttpRequest(httpRequest);
    }

    default void getBinaryAsync(final String uri, final HttpBinaryReceiver binaryReceiver) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setBinaryReceiver(binaryReceiver)
                .build();
        sendHttpRequest(httpRequest);
    }

    default HttpTextResponse sendRequestAndWait(final HttpRequest httpRequest) {
        return sendRequestAndWait(httpRequest, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse sendRequestAndWait(final HttpRequest httpRequest,
                                                final long wait,
                                                final TimeUnit timeUnit) {


        final CountDownLatch countDownLatchConnect = new CountDownLatch(1);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicReference<HttpTextResponse> httpResponseAtomicReference = new AtomicReference<>();


        _createHttpTextReceiver(httpRequest, countDownLatch, countDownLatchConnect, httpResponseAtomicReference);

        sendHttpRequest(httpRequest);


        try {

            countDownLatchConnect.await(500, TimeUnit.MILLISECONDS);
            checkClosed();
            countDownLatch.await(wait, timeUnit);

        } catch (InterruptedException e) {

            if (Thread.currentThread().isInterrupted()) {
                Thread.interrupted();
            }
        }


        final HttpTextResponse httpResponse = httpResponseAtomicReference.get();
        if (httpResponse == null) {
            if (countDownLatch.getCount() != 0) {
                throw new HttpClientTimeoutException("Timeout start time " + new Date(httpRequest.getTimestamp()) +
                        " now " + new Date(Timer.clockTime()));
            } else {
                throw new HttpClientTimeoutException("Timeout: no response " + new Date(httpRequest.getTimestamp()) +
                        " now " + new Date(Timer.clockTime()));
            }
        }
        return httpResponse;

    }

    default HttpTextResponse get(String uri) {
        return getWithTimeout(uri, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse getWithTimeout(String uri, long time, TimeUnit unit) {
        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).build();
        return sendRequestAndWait(httpRequest, time, unit);
    }

    default HttpTextResponse post(String uri) {
        return postWithTimeout(uri, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse postWithTimeout(String uri, long time, TimeUnit unit) {
        final HttpRequest httpRequest = httpRequestBuilder().setMethod("POST")
                .setUri(uri).build();
        return sendRequestAndWait(httpRequest, time, unit);
    }

    default HttpTextResponse put(String uri) {
        return postWithTimeout(uri, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse putWithTimeout(String uri, long time, TimeUnit unit) {
        final HttpRequest httpRequest = httpRequestBuilder().setMethod("PUT")
                .setUri(uri).build();
        return sendRequestAndWait(httpRequest, time, unit);
    }

    default HttpTextResponse getWith1ParamWithTimeout(String uri, String key, Object value,
                                                      final long time,
                                                      final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).addParam(key, value == null ? "" : value.toString())
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse getWith1Param(String uri, String key, Object value) {

        return getWith1ParamWithTimeout(uri, key, value, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse postWith1ParamWithTimeout(String uri, String key, Object value,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder().setMethod("POST")
                .setUri(uri).addParam(key, value == null ? "" : value.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse postWith1Param(String uri, String key, Object value) {

        return postWith1ParamWithTimeout(uri, key, value, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse putWith1ParamWithTimeout(String uri, String key, Object value,
                                                      final long time,
                                                      final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder().setMethod("PUT")
                .setUri(uri).addParam(key, value == null ? "" : value.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse putWith1Param(String uri, String key, Object value) {

        return putWith1ParamWithTimeout(uri, key, value, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse getWith2ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse getWith2Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1
    ) {

        return getWith2ParamsWithTimeout(uri, key, value, key1, value1, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse postWith2ParamsWithTimeout(String uri,
                                                        String key, Object value,
                                                        String key1, Object value1,
                                                        final long time,
                                                        final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod("POST")
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse postWith2Params(String uri,
                                             String key, Object value,
                                             String key1, Object value1
    ) {

        return postWith2ParamsWithTimeout(uri, key, value, key1, value1, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse putWith2ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod("PUT")
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse putWith2Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1
    ) {

        return putWith2ParamsWithTimeout(uri, key, value, key1, value1, HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse getWith3ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       String key2, Object value2,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse getWith3Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1,
                                            String key2, Object value2) {

        return getWith3ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                HTTP_CLIENT_DEFAULT_TIMEOUT,
                TimeUnit.SECONDS);
    }

    default HttpTextResponse postWith3ParamsWithTimeout(String uri,
                                                        String key, Object value,
                                                        String key1, Object value1,
                                                        String key2, Object value2,
                                                        final long time,
                                                        final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod("POST")
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse postWith3Params(String uri,
                                             String key, Object value,
                                             String key1, Object value1,
                                             String key2, Object value2) {

        return postWith3ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                HTTP_CLIENT_DEFAULT_TIMEOUT,
                TimeUnit.SECONDS);
    }

    default HttpTextResponse putWith3ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       String key2, Object value2,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod("PUT")
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse putWith3Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1,
                                            String key2, Object value2) {

        return putWith3ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                HTTP_CLIENT_DEFAULT_TIMEOUT,
                TimeUnit.SECONDS);
    }

    default HttpTextResponse getWith4ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       String key2, Object value2,
                                                       String key3, Object value3,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .addParam(key3, value3 == null ? "" : value3.toString())
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse getWith4Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1,
                                            String key2, Object value2,
                                            String key3, Object value3) {

        return getWith4ParamsWithTimeout(uri,
                key, value,
                key1, value1, key2, value2,
                key3, value3,
                HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse postWith4ParamsWithTimeout(String uri,
                                                        String key, Object value,
                                                        String key1, Object value1,
                                                        String key2, Object value2,
                                                        String key3, Object value3,
                                                        final long time,
                                                        final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod("POST")
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .addParam(key3, value3 == null ? "" : value3.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse postWith4Params(String uri,
                                             String key, Object value,
                                             String key1, Object value1,
                                             String key2, Object value2,
                                             String key3, Object value3) {

        return postWith4ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                key3, value3,
                HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse putWith4ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       String key2, Object value2,
                                                       String key3, Object value3,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setMethod("PUT")
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .addParam(key3, value3 == null ? "" : value3.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse putWith4Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1,
                                            String key2, Object value2,
                                            String key3, Object value3) {

        return putWith4ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                key3, value3,
                HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse getWith5ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       String key2, Object value2,
                                                       String key3, Object value3,
                                                       String key4, Object value4,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .addParam(key3, value3 == null ? "" : value3.toString())
                .addParam(key4, value4 == null ? "" : value4.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse getWith5Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1,
                                            String key2, Object value2,
                                            String key3, Object value3,
                                            String key4, Object value4) {

        return getWith5ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                key3, value3,
                key4, value4,
                HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse postWith5ParamsWithTimeout(String uri,
                                                        String key, Object value,
                                                        String key1, Object value1,
                                                        String key2, Object value2,
                                                        String key3, Object value3,
                                                        String key4, Object value4,
                                                        final long time,
                                                        final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .setMethod("POST")
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .addParam(key3, value3 == null ? "" : value3.toString())
                .addParam(key4, value4 == null ? "" : value4.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse postWith5Params(String uri,
                                             String key, Object value,
                                             String key1, Object value1,
                                             String key2, Object value2,
                                             String key3, Object value3,
                                             String key4, Object value4) {

        return postWith5ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                key3, value3,
                key4, value4,
                HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    default HttpTextResponse putWith5ParamsWithTimeout(String uri,
                                                       String key, Object value,
                                                       String key1, Object value1,
                                                       String key2, Object value2,
                                                       String key3, Object value3,
                                                       String key4, Object value4,
                                                       final long time,
                                                       final TimeUnit timeUnit) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri)
                .setMethod("PUT")
                .addParam(key, value == null ? "" : value.toString())
                .addParam(key1, value1 == null ? "" : value1.toString())
                .addParam(key2, value2 == null ? "" : value2.toString())
                .addParam(key3, value3 == null ? "" : value3.toString())
                .addParam(key4, value4 == null ? "" : value4.toString())
                .initFormIfNeeded()
                .build();

        return sendRequestAndWait(httpRequest, time, timeUnit);
    }

    default HttpTextResponse putWith5Params(String uri,
                                            String key, Object value,
                                            String key1, Object value1,
                                            String key2, Object value2,
                                            String key3, Object value3,
                                            String key4, Object value4) {

        return putWith5ParamsWithTimeout(uri,
                key, value,
                key1, value1,
                key2, value2,
                key3, value3,
                key4, value4,
                HTTP_CLIENT_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
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

    default HttpTextResponse postJson(final String uri,
                                      final String body) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPost(body).setMethodPost()
                .build();

        return sendRequestAndWait(httpRequest);
    }

    default HttpTextResponse postJsonGzip(final String uri,
                                          final String body) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPostGzip(body)
                .build();

        return sendRequestAndWait(httpRequest);
    }

    default HttpTextResponse putJsonGzip(final String uri,
                                         final String body) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPutGzip(body)
                .build();

        return sendRequestAndWait(httpRequest);
    }

    default HttpTextResponse putJson(final String uri,
                                     final String body) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPost(body).setMethodPost()
                .build();

        return sendRequestAndWait(httpRequest);
    }

    default void sendJsonPut(final String uri,
                             final String body) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPost(body).setMethodPut()
                .build();

        sendHttpRequest(httpRequest);
    }

    default void sendJsonPostAsync(final String uri,
                                   final String body,
                                   final HttpTextReceiver receiver) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPost(body).setMethodPost()
                .setTextReceiver(receiver)
                .build();

        sendHttpRequest(httpRequest);
    }

    default <T> void sendJsonGzipPostAsync(final String uri,
                                           final String body,
                                           final HttpResponseReceiver<T> receiver) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPostGzip(body)
                .receiver(receiver)
                .build();

        sendHttpRequest(httpRequest);
    }

    default <T> void sendJsonGzipPutAsync(final String uri,
                                          final String body,
                                          final HttpResponseReceiver<T> receiver) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPutGzip(body)
                .receiver(receiver)
                .build();

        sendHttpRequest(httpRequest);
    }

    default void sendJsonPutAsync(final String uri,
                                  final String body,
                                  final HttpTextReceiver receiver) {

        final HttpRequest httpRequest = httpRequestBuilder()
                .setUri(uri).setJsonBodyForPost(body).setMethodPut()
                .setTextReceiver(receiver)
                .build();

        sendHttpRequest(httpRequest);
    }

    default WebSocket createWebSocket(String uri) {
        throw new RuntimeException("New way to send messages");
    }

    default void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {
    }

    default int getPort() {
        return -1;
    }

    default String getHost() {
        return "localhost";
    }

    default HttpClient startClient() {
        return this;
    }

    default void flush() {
    }

    default void stop() {
    }


}

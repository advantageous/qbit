package io.advantageous.qbit.http;

import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.advantageous.qbit.http.HttpRequestBuilder.httpRequestBuilder;

/**
 * This is an interface that allows users to send HTTP requests to a server.
 *
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
                    public void response(int code, String mimeType, String body) {
                        response(code, mimeType, body, MultiMap.EMPTY);
                    }

                    @Override
                    public void response(
                            final int code, final String mimeType,
                            final String body, final MultiMap<String, String> headers) {

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
                                        return mimeType;
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

    void sendWebSocketMessage(WebSocketMessage webSocketMessage);

    void periodicFlushCallback(Consumer<Void> periodicFlushCallback);


    HttpClient start();

    void flush();

    void stop();

}

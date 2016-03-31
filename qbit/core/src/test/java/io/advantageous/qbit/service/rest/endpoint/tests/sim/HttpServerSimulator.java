package io.advantageous.qbit.service.rest.endpoint.tests.sim;

import io.advantageous.boon.core.Sys;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.request.*;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.request.impl.HttpResponseCreatorDefault;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.util.MultiMap;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HttpServerSimulator implements HttpServer {

    private final HttpResponseCreator httpResponseCreator = new HttpResponseCreatorDefault();
    private Consumer<HttpRequest> httpRequestConsumer;
    private Consumer<Void> idleConsumer;
    private Predicate<HttpRequest> predicate;
    private CopyOnWriteArrayList<HttpResponseDecorator> decorators = new CopyOnWriteArrayList<>();

    public void addDecorator(HttpResponseDecorator decorator) {
        decorators.add(decorator);
    }


    public final HttpTextResponse sendRequestRaw(final HttpRequest request) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder().copyRequest(request);
        httpRequestBuilder.setUri(request.getUri());
        final AtomicReference<HttpTextResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }

    public final HttpTextResponse sendRequest(final HttpRequest request) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder().copyRequest(request);
        httpRequestBuilder.setUri("/services" + request.getUri());
        final AtomicReference<HttpTextResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }

    public final HttpTextResponse get(String uri) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setUri("/services" + uri);
        final AtomicReference<HttpTextResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }


    public final HttpTextResponse postBody(String uri, Object object) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setUri("/services" + uri);
        httpRequestBuilder.setJsonBodyForPost(JsonFactory.toJson(object));
        httpRequestBuilder.setContentType(null);
        final AtomicReference<HttpTextResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }


    public final HttpTextResponse postBodyWithHeaders(String uri, Object object, Map<String, String> headers) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();

        httpRequestBuilder.setHeaders(headers);
        httpRequestBuilder.setMethodPost();
        httpRequestBuilder.setUri("/services" + uri);
        httpRequestBuilder.setBody(JsonFactory.toJson(object));
        final AtomicReference<HttpTextResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }

    public final HttpTextResponse postBodyPlain(String uri, String object) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setMethodPost();
        httpRequestBuilder.setUri("/services" + uri);
        httpRequestBuilder.setBody(object);
        final AtomicReference<HttpTextResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }


    private void callService(final HttpRequest request) {

        if (predicate != null) {
            if (predicate.test(request)) {
                httpRequestConsumer.accept(request);
                Sys.sleep(100);
                idleConsumer.accept(null);
            }
        } else {
            httpRequestConsumer.accept(request);
            Sys.sleep(100);
            idleConsumer.accept(null);
        }
    }

    private AtomicReference<HttpTextResponse> getHttpResponseAtomicReference(final HttpRequestBuilder httpRequestBuilder) {
        final AtomicReference<HttpTextResponse> response = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        httpRequestBuilder.setTextReceiver(


                new HttpTextReceiver() {


                    public void response(int code, String contentType, String body, MultiMap<String, String> headers) {
                        response.set(HttpServerSimulator.this.createResponse(httpRequestBuilder, code, contentType, body, headers));
                        latch.countDown();
                    }

                    @Override
                    public void response(int code, String contentType, String body) {

                        response.set(HttpServerSimulator.this.createResponse(httpRequestBuilder, code, contentType, body, null));
                        latch.countDown();
                    }

                }

        );

        Sys.sleep(100);

        callService(httpRequestBuilder.build());


        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }

    private HttpTextResponse createResponse(HttpRequestBuilder httpRequestBuilder,
                                            int code,
                                            String contentType,
                                            String body,
                                            MultiMap<String, String> headers) {


        HttpTextResponse httpTextResponse = (HttpTextResponse) httpResponseCreator.createResponse(decorators,
                httpRequestBuilder.getUri(), httpRequestBuilder.getMethod(), code, contentType,
                body, headers, httpRequestBuilder.getHeaders(), httpRequestBuilder.getParams());

        if (httpTextResponse == null) {
            httpTextResponse = (HttpTextResponse) HttpResponseBuilder.httpResponseBuilder().setBody(body)
                    .setCode(code).setContentType(contentType).setHeaders(headers).build();
        }

        return httpTextResponse;
    }

    @Override
    public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

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

        this.idleConsumer = idleConsumer;
    }

    @Override
    public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {

    }

    @Override
    public void start() {

    }


    @Override
    public void setShouldContinueHttpRequest(Predicate<HttpRequest> predicate) {
        this.predicate = predicate;
    }

    public void setResponseDecorators(CopyOnWriteArrayList<HttpResponseDecorator> responseDecorators) {
        this.decorators = responseDecorators;
    }
}

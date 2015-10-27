package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpBinaryReceiver;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;

import java.util.function.Consumer;

public class ProxyServiceImpl implements ProxyService {

    
    private final Reactor reactor;
    private final Timer timer;
    private final HttpClientBuilder httpClientBuilder;
    private final HttpClient backendServiceHttpClient;

    private final Consumer<HttpRequestBuilder> beforeSend;

    public ProxyServiceImpl(Reactor reactor, Timer timer, HttpClientBuilder httpClientBuilder,
                            HttpClient httpClient, Consumer<HttpRequestBuilder> beforeSend) {
        this.reactor = reactor;
        this.timer = timer;
        this.httpClientBuilder = httpClientBuilder;
        this.backendServiceHttpClient = httpClient;
        this.beforeSend = beforeSend;
    }

    /** Request coming from the client side.
     *
     * @param clientRequest clientRequest
     */
    @Override
    public void handleRequest(final HttpRequest clientRequest) {

        //forward request to client

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder()
                .copyRequest(clientRequest).setBinaryReceiver(new HttpBinaryReceiver() {

            @Override
            public void response(int code, String contentType, byte[] body, MultiMap<String, String> headers) {

                clientRequest.getReceiver().response(code, contentType, body, headers);

            }

            @Override
            public void response(int code, String contentType, byte[] body) {
                response(code, contentType, body, MultiMap.empty());
            }
        });

        beforeSend.accept(httpRequestBuilder);

        backendServiceHttpClient.sendHttpRequest(httpRequestBuilder.build());


    }

}

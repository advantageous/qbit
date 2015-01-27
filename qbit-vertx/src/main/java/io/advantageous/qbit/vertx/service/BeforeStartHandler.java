package io.advantageous.qbit.vertx.service;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.vertx.BufferUtils;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import java.util.function.Consumer;

/**
* Created by rhightower on 1/26/15.
*/
public class BeforeStartHandler implements Consumer<HttpServer> {


    private Vertx vertx = null;

    @Override
    public void accept(final HttpServer httpServer) {

        httpServer.setHttpRequestConsumer(new Consumer<HttpRequest>() {
            @Override
            public void accept(final HttpRequest request) {
                Buffer buffer = new Buffer();

                BufferUtils.writeString(buffer, request.getUri());
                BufferUtils.writeString(buffer, request.getMethod());
                BufferUtils.writeString(buffer, request.getRemoteAddress());

                final MultiMap<String, String> params = request.getParams();

                BufferUtils.writeMap(buffer, params);

                BufferUtils.writeString(buffer, request.getBodyAsString());


            }
        });

    }

}

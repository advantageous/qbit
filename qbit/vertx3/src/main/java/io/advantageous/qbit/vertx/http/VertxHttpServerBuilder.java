package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.vertx.http.server.HttpServerVertx;
import io.vertx.core.Vertx;


/**
 * Allows one to build an HTTP server.
 *
 * It also allows one to pass a shared Vertx object if running inside of the Vertx world.
 *
 * @author rhightower
 */
public class VertxHttpServerBuilder extends HttpServerBuilder {


    private Vertx vertx;

    public Vertx getVertx() {
        if (vertx == null) {
            vertx = Vertx.vertx();
        }
        return vertx;
    }

    public VertxHttpServerBuilder setVertx(final Vertx vertx) {
        this.vertx = vertx;
        return this;
    }



    public HttpServer build() {

        final HttpServer httpServer =
             new HttpServerVertx(this.getVertx(), getEndpointName(), getConfig(),
                getSystemManager(), getServiceDiscovery(), getHealthServiceAsync(),
                getServiceDiscoveryTtl(), getServiceDiscoveryTtlTimeUnit(), getResponseDecorators(),
                getHttpResponseCreator());

        if (this.getRequestContinuePredicate()!=null) {
            httpServer.setShouldContinueHttpRequest(this.getRequestContinuePredicate());
        }

        if (getSystemManager() != null) {
            getSystemManager().registerServer(httpServer);
        }
        return httpServer;
    }
}

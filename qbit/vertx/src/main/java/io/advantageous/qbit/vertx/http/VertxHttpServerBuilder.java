package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.vertx.http.server.HttpServerVertx;
import io.advantageous.qbit.vertx.http.server.SimpleVertxHttpServerWrapper;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;


/**
 * Allows one to build an HTTP server.
 *
 * It also allows one to pass a shared Vertx object if running inside of the Vertx world.
 * It also allows one to pass a shared HttpServer if you want to use more than just QBit routing.
 * If you are using routing or you want to limit this HttpServer to one route then you can
 * pass routes.
 *
 * This class allows you to mix and match Vertx routes and QBit REST routing.
 *
 * ##Usage
 *
 * #### Using this server with a single route
 * ```java
 *     vertxHttpServerBuilder = VertxHttpServerBuilder.vertxHttpServerBuilder()
 *                     .setVertx(vertx).setHttpServer(httpServer).setRoute(route);
 *
 *     HttpServer httpServer = vertxHttpServerBuilder.build();
 *
 * ```
 *
 * #### Using this server but using Vertx Routes by passing a router.
 * ```java
 *
 *
 *     Router router = Router.router(vertx); //Vertx router
 *     Route route1 = router.route("/some/path/").handler(routingContext -> {
 *     HttpServerResponse response = routingContext.response();
 *          // enable chunked responses because we will be adding data as
 *          // we execute over other handlers. This is only required once and
 *          // only if several handlers do output.
 *          response.setChunked(true);
 *          response.write("route1\n");
 *
 *          // Call the next matching route after a 5 second delay
 *         routingContext.vertx().setTimer(5000, tid -> routingContext.next());
 *     });
 *
 *     //Now install our QBit Server to handle REST calls.
 *     vertxHttpServerBuilder = VertxHttpServerBuilder.vertxHttpServerBuilder()
 *                     .setVertx(vertx).setHttpServer(httpServer).setRouter(router);
 *
 *     HttpServer httpServer = vertxHttpServerBuilder.build();
 *     httpServer.start();
 *
 * ```
 *
 *  Note that you can pass `HttpServerBuilder` or a `HttpServer` to `EndpointServerBuilder`
 *  to use that builder instead or `HttpServer` instead of the default.
 *
 *  #### EndpointServerBuilder integration
 *  ```java
 *
 *      //Like before
 *      vertxHttpServerBuilder = VertxHttpServerBuilder.vertxHttpServerBuilder()
 *                   .setVertx(vertx).setHttpServer(httpServer).setRouter(router);
 *      //Now just inject it into the vertxHttpServerBuilder before you call build
 *      HttpServer httpServer = vertxHttpServerBuilder.build();
 *      endpointServerBuilder.setHttpServer(httpServer);
 *  ```
 *
 *  If you are using QBit REST with Vertx, that is one integration point.
 *
 *  Also note that you can pass `HttpServerBuilder` or a `HttpServer` to `ManagedServiceBuilder`
 *  to use that builder instead or `HttpServer` instead of the default.
 *
 *  #### ManagedServiceBuilder integration
 *  ```java
 *     //Like before
 *     vertxHttpServerBuilder = VertxHttpServerBuilder.vertxHttpServerBuilder()
 *            .setVertx(vertx).setHttpServer(httpServer).setRouter(router);
 *
 *     //Now just inject it into the vertxHttpServerBuilder before you call build
 *     HttpServer httpServer = vertxHttpServerBuilder.build();
 *     managedServiceBuilder.setHttpServer(httpServer);
 *  ```
 *
 *  If you wanted to use QBit REST and QBit Swagger support with Vertx then you
 *  would want to use `ManagedServiceBuilder` with this class.
 *
 *  Read Vertx guide on routing for more details
 *  [Vertx Http Ext Manual](http://vertx.io/docs/vertx-web/java/#_routing_by_exact_path)
 *
 * @author rhightower
 */
public class VertxHttpServerBuilder extends HttpServerBuilder {


    /**
     * Vertx.
     */
    private Vertx vertx;

    /**
     * Router, if present http server will register with the router.
     */
    private Router router;

    /**
     * Vertx HttpServer if you want to use routes then you must inject this into the builder.
     */
    private io.vertx.core.http.HttpServer vertxHttpServer;


    private boolean startedVertx;

    /**
     * Route, if present http server will register with the route.
     * Route takes priority over router so if they are both set, only the route is used.
     */
    private Route route;

    public static VertxHttpServerBuilder vertxHttpServerBuilder() {
        return new VertxHttpServerBuilder();
    }

    public Router getRouter() {
        return router;
    }

    public VertxHttpServerBuilder setRouter(final Router router) {
        this.router = router;
        return this;
    }


    public Route getRoute() {
        return route;
    }

    public VertxHttpServerBuilder setRoute(final Route route) {
        this.route = route;
        return this;
    }

    public Vertx getVertx() {
        if (vertx == null) {
            startedVertx = true;
            vertx = Vertx.vertx();
        }
        return vertx;
    }

    public VertxHttpServerBuilder setVertx(final Vertx vertx) {
        this.vertx = vertx;
        return this;
    }


    public io.vertx.core.http.HttpServer getHttpServer() {
        return vertxHttpServer;
    }

    public VertxHttpServerBuilder setHttpServer(final io.vertx.core.http.HttpServer httpServer) {
        this.vertxHttpServer = httpServer;
        return this;
    }

    public HttpServer build() {


        HttpServer httpServer;


        /* If the vertxHttpServer then we can just create one and register direct with it. */
        if (vertxHttpServer == null) {

            if (this.getRoute() != null) {
                throw new IllegalArgumentException("You can't pass a route if you don't pass an httpServer");
            }

            if (this.getRouter() != null) {
                throw new IllegalArgumentException("You can't pass a router if you don't pass an httpServer");
            }

            httpServer = new HttpServerVertx(startedVertx, this.getVertx(), getEndpointName(), getConfig(),
                    getSystemManager(), getServiceDiscovery(), getHealthServiceAsync(),
                    getServiceDiscoveryTtl(), getServiceDiscoveryTtlTimeUnit(), getResponseDecorators(),
                    getHttpResponseCreator(), getRequestBodyContinuePredicate());
        } else {

            /* They wanted routes. */
            if (vertx == null) {
                throw new IllegalArgumentException("You can't pass a httpServer and not pass a vertx object");
            }
            httpServer = new SimpleVertxHttpServerWrapper(getHttpServer(), getRouter(), getRoute(),
                    super.getConfig().getFlushInterval(), this.getEndpointName(),
                    this.getVertx(), this.getSystemManager(),
                    this.getServiceDiscovery(), this.getHealthServiceAsync(), getServiceDiscoveryTtl(),
                    getServiceDiscoveryTtlTimeUnit(), getResponseDecorators(), getHttpResponseCreator(),
                    getRequestBodyContinuePredicate());
        }

        if (this.getRequestContinuePredicate() != null) {
            httpServer.setShouldContinueHttpRequest(this.getRequestContinuePredicate());
        }

        if (getSystemManager() != null) {
            getSystemManager().registerServer(httpServer);
        }
        return httpServer;
    }
}

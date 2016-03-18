package io.advantageous.qbit.example.vertx;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.stats.StatCollection;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.vertx.http.VertxHttpServerBuilder;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class HelloWorldVerticle extends AbstractVerticle {
    private final int port;

    private QBitSystemManager systemManager;

    public HelloWorldVerticle(int port) {
        this.port = port;
    }

    public static void main(final String... args) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final HelloWorldVerticle helloWorldVerticle = new HelloWorldVerticle(8080);
        final AtomicReference<AsyncResult> asyncResultRef = new AtomicReference<>();
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(5));
        vertx.deployVerticle(helloWorldVerticle, result -> {
            if (result.succeeded()) {
                System.out.println("Deployment id is: " + result.result());
                asyncResultRef.set(result);
            } else {
                System.out.println("Deployment failed!");
                result.cause().printStackTrace();
            }
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        if (asyncResultRef.get().succeeded()) {

            System.out.println("Server started");
        }
    }

    public void start() {

        try {


                /* Route one call to a vertx handler. */
            final Router router = Router.router(vertx); //Vertx router
            router.route("/svr/rout1/").handler(routingContext -> {
                HttpServerResponse response = routingContext.response();
                response.setStatusCode(202);
                response.end("route1");
            });

                /* Route everything under /hello to QBit http server. */
            final Route qbitRoute = router.route().path("/hello/*");


                /* Vertx HTTP Server. */
            final io.vertx.core.http.HttpServer vertxHttpServer =
                    this.getVertx().createHttpServer();

                /*
                 * Use the VertxHttpServerBuilder which is a special builder for Vertx/Qbit integration.
                 */
            final HttpServer httpServer = VertxHttpServerBuilder.vertxHttpServerBuilder()
                    .setRoute(qbitRoute)
                    .setHttpServer(vertxHttpServer)
                    .setVertx(getVertx())
                    .build();


            /** Use a managed service builder. */
            final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder();



            final EndpointServerBuilder endpointServerBuilder = managedServiceBuilder
                    .getEndpointServerBuilder().setUri("/")
                    .addService(new HelloWorldService())
                    .setHttpServer(httpServer);


                /* Route everything under /__health to QBit health system. */
            final Route healthRoute = router.route().path("/__health");

            final HealthServiceAsync healthService = endpointServerBuilder.getHealthService();
            healthRoute.handler(event -> healthService.ok(aBoolean -> event.response().end(aBoolean.toString())));



                /* Route everything under /__stats/instance to QBit stats system. */
            final Route statsRoute = router.route().path("/__stats/instance");
            final Route globalStats = router.route().path("/__stats");
            final StatCollection statsCollection = endpointServerBuilder.getStatsCollection();
            statsRoute.handler(event -> statsCollection.collect(stats -> {

                String json = JsonFactory.toJson(stats);
                event.response().end(json);
            }));
            globalStats.handler(event -> {
                event.response().end("{\"version\":1}");
            });




                /*
                 * Create a new service endpointServer.
                 */
            final ServiceEndpointServer endpointServer = endpointServerBuilder.build();


            endpointServer.startServer();


            systemManager = managedServiceBuilder.getSystemManager();



                /*
                 * Associate the router as a request handler for the vertxHttpServer.
                 */
            vertxHttpServer.requestHandler(router::accept).listen(port);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void stop() {

        if (systemManager != null) {
            systemManager.shutDown();
        }
    }


}

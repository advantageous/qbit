package io.advantageous.qbit.example.vertx.eventbus.bridge;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.vertx.eventbus.bridge.VertxEventBusBridgeBuilder;
import io.advantageous.qbit.vertx.http.VertxHttpServerBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * Send JSON POST.
 * 
 * <code>
 *     curl -X POST -H "Content-Type: application/json" \
 *     http://localhost:8080/es/1.0/employee/ \
 *     -d '{"id":"5","firstName":"Bob","lastName":"Jingles","birthYear":1962,"socialSecurityNumber":999999999}'
 * </code>
 *
 * Get JSON
 *
 * <code>
 *      curl http://localhost:8080/es/1.0/employee/?id=5
 * </code>
 */
public class EventBusBridgeExample extends AbstractVerticle {

    @Override
    public void start() throws Exception {


        final String address = "/es/1.0";
        final EmployeeService employeeService = new EmployeeService();
        final VertxEventBusBridgeBuilder vertxEventBusBridgeBuilder = VertxEventBusBridgeBuilder
                .vertxEventBusBridgeBuilder()
                .setVertx(vertx);
        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder();
        final Router router = Router.router(vertx);


        managedServiceBuilder.setRootURI("/");
        vertxEventBusBridgeBuilder.addBridgeAddress(address, EmployeeService.class);
        /* Route everything under address to QBit http server. */
        router.route().path(address + "/*");
        /* Configure bridge at this HTTP/WebSocket URI. */
        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(
                new BridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddress(address))
                        .addOutboundPermitted(new PermittedOptions().setAddress(address))
        ));

        final io.vertx.core.http.HttpServer vertxHttpServer = vertx.createHttpServer();
        /*
         * Use the VertxHttpServerBuilder which is a special builder for Vertx/Qbit integration.
         */
        final HttpServer httpServer = VertxHttpServerBuilder.vertxHttpServerBuilder()
                .setRouter(router)
                .setHttpServer(vertxHttpServer)
                .setVertx(vertx)
                .build();


        final ServiceEndpointServer endpointServer = managedServiceBuilder.getEndpointServerBuilder()
                .setHttpServer(httpServer)
                .addService(employeeService)
                .build();
        vertxEventBusBridgeBuilder.setServiceBundle(endpointServer.serviceBundle()).build();
        endpointServer.startServer();
        vertxHttpServer.requestHandler(router::accept).listen(8080);


    }


    public static void main(String... args) throws Exception {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new EventBusBridgeExample());
    }
}

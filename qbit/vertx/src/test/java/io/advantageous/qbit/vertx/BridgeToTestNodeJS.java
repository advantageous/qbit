package io.advantageous.qbit.vertx;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.vertx.eventbus.bridge.VertxEventBusBridgeBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class BridgeToTestNodeJS extends AbstractVerticle {


    public static void main(String... args) throws Exception {


        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new BridgeToTestNodeJS());

    }

    @Override
    public void start() throws Exception {

        /* test service */
        final TestService testService = new TestService();

        /* address */
        final String address = "testservice";

        /* service builder */
        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setServiceObject(testService);
        final ServiceQueue serviceQueue = serviceBuilder.build();




        /* vertx event bus bridge to qbit. */
        final VertxEventBusBridgeBuilder vertxEventBusBridgeBuilder = VertxEventBusBridgeBuilder
                .vertxEventBusBridgeBuilder()
                .setVertx(vertx);

        vertxEventBusBridgeBuilder.addBridgeAddress(address, TestService.class);

        final Router router = Router.router(vertx);


        router.route("/health/").handler(routingContext -> routingContext.response().end("\"ok\""));


            /* Configure bridge at this HTTP/WebSocket URI. */
        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(
                new BridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddress(address))
                        .addOutboundPermitted(new PermittedOptions().setAddress(address))
        ));


        vertxEventBusBridgeBuilder.setServiceQueue(serviceQueue);
        serviceQueue.startAll(); //startall not supported yet for bridge.
        vertxEventBusBridgeBuilder.build();


        vertx.createHttpServer().requestHandler(router::accept).listen(8080);

        System.out.println("Bound to 8080");
    }

    public static class TestService {
        public void test(Callback<Boolean> callback, final String newValue) {
            System.out.println("HERE::" + newValue);
            callback.returnThis(true);
        }
    }
}

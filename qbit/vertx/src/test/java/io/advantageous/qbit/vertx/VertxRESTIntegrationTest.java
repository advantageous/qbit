package io.advantageous.qbit.vertx;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.PathVariable;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.qbit.vertx.http.VertxHttpServerBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.advantageous.qbit.server.EndpointServerBuilder.endpointServerBuilder;
import static org.junit.Assert.assertEquals;

public class VertxRESTIntegrationTest {

    private Vertx vertx;
    private TestVerticle testVerticle;
    private int port;

    @Before
    public void setup() throws Exception {


        final CountDownLatch latch = new CountDownLatch(1);
        port = PortUtils.findOpenPortStartAt(9000);
        testVerticle = new TestVerticle(port);
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(5));
        vertx.deployVerticle(testVerticle, res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
            } else {
                System.out.println("Deployment failed!");
                res.cause().printStackTrace();
            }
            latch.countDown();
        });


        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void test() {

        final HttpClient client = HttpClientBuilder.httpClientBuilder().setHost("localhost").setPort(port).buildAndStart();
        Sys.sleep(1000);
        final HttpTextResponse response = client.postJson("/svr/rout1/", "\"hi\"");
        assertEquals(202, response.code());
        assertEquals("route1", response.body());


        final HttpTextResponse response2 = client.postJson("/hello/world", "\"hi\"");
        assertEquals(200, response2.code());
        assertEquals("\"hi\"", response2.body());

        final HttpTextResponse response3 = client.get("/hello/sayHi/me");
        assertEquals(200, response3.code());
        assertEquals("\"me\"", response3.body());
    }

    @After
    public void tearDown() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        vertx.close(res -> {
            if (res.succeeded()) {
                System.out.println("Vertx is closed? " + res.result());
            } else {
                System.out.println("Vertx failed closing");
            }
            latch.countDown();
        });


        latch.await(5, TimeUnit.SECONDS);
        vertx = null;
        testVerticle = null;

    }

    @RequestMapping("/hello")
    public static class TestRestService {

        @RequestMapping(value = "/world", method = RequestMethod.POST)
        public String hello(String body) {
            return body;
        }

        @RequestMapping(value = "/sayHi/{0}", method = RequestMethod.GET)
        public String sayHi(@PathVariable String to) {
            return to;
        }
    }

    public static class TestVerticle extends AbstractVerticle {

        private final int port;

        public TestVerticle(int port) {
            this.port = port;
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


                /*
                 * Create a new service endpointServer.
                 */
                final ServiceEndpointServer endpointServer = endpointServerBuilder().setUri("/")
                        .addService(new TestRestService()).setHttpServer(httpServer).build();

                endpointServer.startServerAndWait();




                /*
                 * Associate the router as a request handler for the vertxHttpServer.
                 */
                vertxHttpServer.requestHandler(router::accept).listen(port);

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalStateException(ex);
            }
        }

        public void stop() {
        }

    }
}

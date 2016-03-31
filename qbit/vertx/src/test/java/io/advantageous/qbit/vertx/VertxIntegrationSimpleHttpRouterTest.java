package io.advantageous.qbit.vertx;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.qbit.vertx.http.VertxHttpServerBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class VertxIntegrationSimpleHttpRouterTest {


    private Vertx vertx;
    private TestVerticle testVerticle;
    private int port;

    @Before
    public void setup() throws Exception {


        final CountDownLatch latch = new CountDownLatch(2);
        port = PortUtils.findOpenPortStartAt(9000);
        testVerticle = new TestVerticle(port, latch);
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
        final HttpTextResponse response = client.postJson("/svr/rout1/", "\"hi\"");
        assertEquals(202, response.code());
        assertEquals("route1", response.body());


        final HttpTextResponse response2 = client.postJson("/hello/world", "\"hi\"");
        assertEquals(200, response2.code());
        assertEquals("\"hi\"", response2.body());

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

    public static class TestVerticle extends AbstractVerticle {

        private final int port;
        private final CountDownLatch latch;

        public TestVerticle(int port, CountDownLatch latch) {
            this.port = port;
            this.latch = latch;
        }

        public void start() {

            try {

                HttpServerOptions options = new HttpServerOptions().setMaxWebsocketFrameSize(1000000);
                options.setPort(port);

                Router router = Router.router(vertx); //Vertx router
                router.route("/svr/rout1/").handler(routingContext -> {
                    HttpServerResponse response = routingContext.response();
                    response.setStatusCode(202);
                    response.end("route1");
                });


                io.vertx.core.http.HttpServer vertxHttpServer =
                        this.getVertx().createHttpServer(options);

                HttpServer httpServer = VertxHttpServerBuilder.vertxHttpServerBuilder()
                        .setRouter(router)
                        .setHttpServer(vertxHttpServer)
                        .setVertx(getVertx())
                        .build();


                httpServer.setHttpRequestConsumer(httpRequest -> {

                    System.out.println(httpRequest.address());

                    httpRequest.getReceiver().response(200, httpRequest.getContentType(), httpRequest.body());
                });

                httpServer.start();

                vertxHttpServer.requestHandler(router::accept).listen(event -> {

                    if (event.succeeded()) {
                        latch.countDown();
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void stop() {
        }

    }

}

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class VertxIntegrationSimpleHttpTest {

    private Vertx vertx;
    private TestVerticle testVerticle;
    private int port;


    public static class TestVerticle extends AbstractVerticle {

        private final int port;

        public TestVerticle(int port) {
            this.port = port;
        }

        public void start() {

            try {

                HttpServerOptions options = new HttpServerOptions().setMaxWebsocketFrameSize(1000000);
                options.setPort(port);


                io.vertx.core.http.HttpServer vertxHttpServer =
                        this.getVertx().createHttpServer(options);

                HttpServer httpServer = VertxHttpServerBuilder.vertxHttpServerBuilder()
                        .setVertx(getVertx()).setHttpServer(vertxHttpServer).build();


                httpServer.setHttpRequestConsumer(httpRequest -> {

                    System.out.println(httpRequest.address());

                    httpRequest.getReceiver().response(200, httpRequest.getContentType(), httpRequest.body());
                });

                httpServer.start();

                vertxHttpServer.listen();
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void stop() {
        }

    }

    @Before
    public void setup() throws Exception{


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
        final HttpTextResponse response = client.postJson("/hello/world", "\"hi\"");
        assertEquals(200, response.code());
        assertEquals("\"hi\"", response.body());

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
}

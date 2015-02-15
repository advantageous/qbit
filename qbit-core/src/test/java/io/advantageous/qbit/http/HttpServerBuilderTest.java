package io.advantageous.qbit.http;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.http.websocket.WebSocketMessage;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.system.QBitSystemManager;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static org.boon.Exceptions.die;

public class HttpServerBuilderTest {


    HttpServerBuilder objectUnderTest;

    Consumer<WebSocketMessage> webSocketMessageConsumer;
    Consumer<HttpRequest> httpRequestConsumer;
    boolean ok;

    @Before
    public void setUp() throws Exception {

        objectUnderTest =new HttpServerBuilder();

        webSocketMessageConsumer = new Consumer<WebSocketMessage>() {
            @Override
            public void accept(WebSocketMessage webSocketMessage) {

            }
        };

        httpRequestConsumer = new Consumer<HttpRequest>() {
            @Override
            public void accept(HttpRequest request) {

            }
        };

        FactorySPI.setFactory(new Factory() {

            @Override
            public HttpServer createHttpServer(HttpServerOptions options, QueueBuilder requestQueueBuilder,
                                               QueueBuilder rspQB,
                                               QueueBuilder webSocketMessageQueueBuilder, QBitSystemManager systemManager) {
                return null;
            }
        });

        FactorySPI.setHttpServerFactory((options, requestQueueBuilder, resQB,
                                         webSocketMessageQueueBuilder, systemManager) -> new SimpleHttpServer());

        Sys.sleep(100);

    }

    @Test
    public void test() throws Exception {


        ok = objectUnderTest.setHost("host").getHost().equals("host") || die();
        ok = objectUnderTest.setHost("localhost").getHost().equals("localhost") || die();
        ok = objectUnderTest.setManageQueues(true).isManageQueues() || die();
        ok = !objectUnderTest.setManageQueues(false).isManageQueues() || die();

        ok = objectUnderTest.setPipeline(true).isPipeline() || die();
        ok = !objectUnderTest.setPipeline(false).isPipeline() || die();

        ok = objectUnderTest.setPollTime(7).getPollTime()==7 || die();
        ok = objectUnderTest.setPort(9090).getPort()==9090 || die();
        ok = objectUnderTest.setPort(8080).getPort()==8080 || die();

        ok = objectUnderTest.setFlushInterval(909).getFlushInterval()==909 || die();
        ok = objectUnderTest.setFlushInterval(808).getFlushInterval()==808 || die();

        ok = objectUnderTest.setRequestBatchSize(13).getRequestBatchSize()==13
                || die();

        objectUnderTest.build();

    }
}
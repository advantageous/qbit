package io.advantageous.qbit.http;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpClientFactory;
import io.advantageous.qbit.spi.HttpServerFactory;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static org.boon.Exceptions.die;
import static org.junit.Assert.*;

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
            public HttpServer createHttpServer(String host, int port, boolean manageQueues, int pollTime, int requestBatchSize, int flushInterval) {
                return FactorySPI.getHttpServerFactory().create(host, port, manageQueues, pollTime, requestBatchSize, flushInterval);
            }
        });

        FactorySPI.setHttpServerFactory(new HttpServerFactory() {

            @Override
            public HttpServer create(String host, int port,
                                     boolean manageQueues,
                                     int pollTime,
                                     int requestBatchSize,
                                     int flushInterval) {
                return new HttpServer() {
                    @Override
                    public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

                    }

                    @Override
                    public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {

                    }

                    @Override
                    public void start() {

                    }

                    @Override
                    public void stop() {

                    }
                };
            }
        });

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

        ok = objectUnderTest.setHttpRequestConsumer(httpRequestConsumer)
                .getHttpRequestConsumer()==httpRequestConsumer || die();


        ok = objectUnderTest.setWebSocketMessageConsumer(webSocketMessageConsumer)
                .getWebSocketMessageConsumer()==webSocketMessageConsumer || die();

        objectUnderTest.build();

    }
}
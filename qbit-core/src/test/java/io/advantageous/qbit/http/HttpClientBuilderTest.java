package io.advantageous.qbit.http;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.sender.Sender;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpClientFactory;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.MultiMap;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.boon.Exceptions.die;

public class HttpClientBuilderTest {

    HttpClientBuilder objectUnderTest;
    boolean ok;

    @Before
    public void setUp() throws Exception {

        objectUnderTest =new HttpClientBuilder();

        FactorySPI.setFactory(new Factory() {

            @Override
            public HttpClient createHttpClient(String host, int port, int pollTime, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush) {
                return FactorySPI.getHttpClientFactory().create(host, port, pollTime, requestBatchSize, timeOutInMilliseconds, poolSize, autoFlush);
            }
        });

        FactorySPI.setHttpClientFactory(new HttpClientFactory() {
            @Override
            public HttpClient create(String host, int port, int pollTime, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush) {
                return null;
            }
        });

        Sys.sleep(100);

    }


    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetHost() throws Exception {

        ok = objectUnderTest.setHost("host").getHost().equals("host") || die();
        ok = objectUnderTest.setHost("localhost").getHost().equals("localhost") || die();
        ok = objectUnderTest.setAutoFlush(true).isAutoFlush() || die();
        ok = !objectUnderTest.setAutoFlush(false).isAutoFlush() || die();
        ok = objectUnderTest.setPollTime(7).getPollTime()==7 || die();
        ok = objectUnderTest.setPoolSize(11).getPoolSize()==11 || die();
        ok = objectUnderTest.setPort(9090).getPort()==9090 || die();
        ok = objectUnderTest.setPort(8080).getPort()==8080 || die();
        ok = objectUnderTest.setRequestBatchSize(13).getRequestBatchSize()==13
                || die();
        ok = objectUnderTest.setTimeOutInMilliseconds(113).getTimeOutInMilliseconds()==113
                || die();

        objectUnderTest.build();

    }
}
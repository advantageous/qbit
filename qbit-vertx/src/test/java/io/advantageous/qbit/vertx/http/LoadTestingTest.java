package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 1/21/15.
 */
public class LoadTestingTest {

    public static final int WARMUP = 10_000;
    Client client;
    ServiceServer server;
    HttpClient httpClient;
    ClientServiceInterface clientProxy;
    volatile int callCount;
    volatile int returnCount;
    AtomicReference<String> pongValue;
    boolean ok;
    static int port = 5555;

    static interface ClientServiceInterface {
        String ping(Callback<String> callback, String ping);
    }

    class MockService {

        @RequestMapping(method = RequestMethod.POST)
        public String ping(String ping) {
            callCount++;
            return ping + " pong";
        }
    }


    @Test
    public void warmup() throws Exception {


        final long startTime = System.currentTimeMillis();

        final Callback<String> callback = new Callback<String>() {
            @Override
            public void accept(String s) {
                returnCount++;
            }
        };

        for (int index=0; index< WARMUP; index++) {

            clientProxy.ping(callback, "hi");

        }

        client.flush();

        while (returnCount < WARMUP -1) {
            Sys.sleep(1);
        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime = System.currentTimeMillis();

        ok = returnCount == callCount || die();

        final long duration = endTime - startTime;

        puts(duration);



    }


    @Test
    public void test10K() throws Exception {

        warmup();

        returnCount = 0;
        callCount = 0;
        Sys.sleep(100);



        final long startTime = System.currentTimeMillis();

        final Callback<String> callback = new Callback<String>() {
            @Override
            public void accept(String s) {
                returnCount++;
            }
        };

        for (int index=0; index< WARMUP; index++) {

            clientProxy.ping(callback, "hi");

        }


        client.flush();

        while (returnCount < WARMUP -1) {
            Sys.sleep(1);
        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime = System.currentTimeMillis();

        ok = returnCount == callCount || die();

        final long duration = endTime - startTime;

        puts(duration);



    }



    @Test
    public void test100K() throws Exception {

        warmup();

        returnCount = 0;
        callCount = 0;
        Sys.sleep(100);



        final long startTime = System.currentTimeMillis();

        final Callback<String> callback = new Callback<String>() {
            @Override
            public void accept(String s) {
                returnCount++;
            }
        };

        for (int index=0; index< 100_000; index++) {

            clientProxy.ping(callback, "hi");

        }


        client.flush();

        while (returnCount < 100_000 -1) {
            Sys.sleep(1);
        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime = System.currentTimeMillis();

        ok = returnCount == callCount || die();

        final long duration = endTime - startTime;

        puts(duration);



    }



    @Test
    public void test1M() throws Exception {

        warmup();

        returnCount = 0;
        callCount = 0;
        Sys.sleep(100);



        final long startTime = System.currentTimeMillis();

        final Callback<String> callback = new Callback<String>() {
            @Override
            public void accept(String s) {
                returnCount++;
            }
        };

        for (int index=0; index< 1_000_000; index++) {

            clientProxy.ping(callback, "hi");

        }


        client.flush();

        while (returnCount < 1_000_000 -1) {
            Sys.sleep(1);
        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime = System.currentTimeMillis();

        ok = returnCount == callCount || die();

        final long duration = endTime - startTime;

        puts(duration);



    }

    @Before
    public void setup() throws Exception {

        Sys.sleep(5000);
        pongValue = new AtomicReference<>();

        httpClient = new HttpClientBuilder().setPort(port).build();

        client = new ClientBuilder().setRequestBatchSize(300).setPort(port).build();
        server = new ServiceServerBuilder().setRequestBatchSize(300).setTimeoutSeconds(20)
                .setPollTime(10).setPort(port).build();

        server.initServices(new MockService());

        server.start();

        Sys.sleep(200);

        clientProxy = client.createProxy(ClientServiceInterface.class, "mockService");
        client.start();
        httpClient.start();

        callCount = 0;
        pongValue.set(null);

        Sys.sleep(200);



    }

    @After
    public void teardown() throws Exception {

        port++;


        Sys.sleep(200);
        server.stop();
        Sys.sleep(200);
        client.stop();
        httpClient.stop();
        Sys.sleep(200);
        server = null;
        client = null;
        System.gc();
        Sys.sleep(1000);

    }
}

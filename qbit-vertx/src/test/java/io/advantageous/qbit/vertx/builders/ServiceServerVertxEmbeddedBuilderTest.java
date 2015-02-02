package io.advantageous.qbit.vertx.builders;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.vertx.builders.ServiceServerVertxEmbeddedBuilder;
import org.boon.HTTP;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class ServiceServerVertxEmbeddedBuilderTest {

    ServiceServerVertxEmbeddedBuilder builder;

    Client client;

    ClientServiceInterface clientProxy;

    boolean ok;

    static interface ClientServiceInterface {
        String ping(Callback<String> callback, String ping);
    }

    static class MockService {

        int callCount;

        @RequestMapping(method = RequestMethod.POST)
        public String ping(String ping) {
            callCount++;
            return ping + " pong";
        }
    }


    public static class BeforeHandler implements Consumer<ServiceBundle> {

        @Override
        public void accept(ServiceBundle serviceBundle) {

            serviceBundle.addService(new MockService());
        }
    }

    @Before
    public void setUp() throws Exception {
        builder = new ServiceServerVertxEmbeddedBuilder();

        builder.setPort(4049);

        builder.setBeforeStartHandler(BeforeHandler.class);

        final ServiceServer server = builder.build();

        server.start();

        Sys.sleep(200);


        client = new ClientBuilder().setPort(4049).build();

        clientProxy = client.createProxy(ClientServiceInterface.class, "mockService");
        client.start();
        Sys.sleep(200);
    }

    @Test
    public void test() {
    }

    //TODO FIX
    public void fix() {

        Sys.sleep(2_000);

        final String ping = HTTP.postJSON("http://localhost:4049/services/mockservice/ping", "\"ping\"");

        ok = ping.equals("\"ping pong\"") || die(ping);


        final AtomicInteger count = new AtomicInteger();

        final Callback<String> callback = new Callback<String>() {
            @Override
            public void accept(String s) {

                count.incrementAndGet();
                puts("                     PONG", s);

            }
        };

        for (int index=0; index< 10; index++) {

            clientProxy.ping(callback, "hi");

        }


        client.flush();
        Sys.sleep(1_000);

        ok = count.get() == 10 || die();


    }
    @After
    public void tearDown() throws Exception {

        Sys.sleep(1_000);
        client.stop();

    }
}
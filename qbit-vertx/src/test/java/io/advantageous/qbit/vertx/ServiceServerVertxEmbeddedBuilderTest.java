package io.advantageous.qbit.vertx;

import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.ServiceBundle;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static org.boon.Boon.puts;
import static org.junit.Assert.*;

public class ServiceServerVertxEmbeddedBuilderTest {

    ServiceServerVertxEmbeddedBuilder  builder;

    static class BeforeHandler implements Consumer<ServiceBundle> {

        @Override
        public void accept(ServiceBundle serviceBundle) {
            puts("GOT HERE");
        }
    }

    @Before
    public void setUp() throws Exception {
        builder = new ServiceServerVertxEmbeddedBuilder();

        builder.setPort(4049);

        builder.setBeforeStartHandler(BeforeHandler.class);

        final ServiceServer server = builder.build();

        server.start();
    }

    @Test
    public void test() {


    }
    @After
    public void tearDown() throws Exception {

        Sys.sleep(20_000);

    }
}
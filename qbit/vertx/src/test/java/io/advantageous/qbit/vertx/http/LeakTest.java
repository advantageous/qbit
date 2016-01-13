package io.advantageous.qbit.vertx.http;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.client.RemoteTCPClientProxy;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;

public class LeakTest {

    public static void main(String... args) throws Exception {
        final ServiceEndpointServer endpointServer = EndpointServerBuilder
                .endpointServerBuilder().setPort(9000).build();
        endpointServer.addServiceObject("myservice", new MyServiceImpl());
        endpointServer.startServerAndWait();

        final Client client = ClientBuilder.clientBuilder().setPoolSize(1).setHost("localhost").setPort(9000).build();

        MyService myservice = client.createProxy(MyService.class, "myservice");
        client.start();

        for (int index = 0; index < 100_000; index++) {
            Sys.sleep(1000);
            myservice.foo();
            ((RemoteTCPClientProxy) myservice).silentClose();

            final Client c = ClientBuilder.clientBuilder().setHost("localhost").setPort(9000).build();
            myservice = c.createProxy(MyService.class, "myservice");
            c.start();

            System.gc();
        }

    }

    public interface MyService {
        void foo();
    }

    public static class MyServiceImpl {
        public void foo() {
            System.out.println("foo");
        }
    }
}

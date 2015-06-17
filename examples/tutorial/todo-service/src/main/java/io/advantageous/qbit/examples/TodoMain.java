package io.advantageous.qbit.examples;

import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;

public class TodoMain {

    public static void main(String... args) {

        ServiceEndpointServer server = EndpointServerBuilder
                              .endpointServerBuilder().setHost("localhost")
                .setPort(8080).build();

        server.initServices(new TodoService()).startServer();
    }

}

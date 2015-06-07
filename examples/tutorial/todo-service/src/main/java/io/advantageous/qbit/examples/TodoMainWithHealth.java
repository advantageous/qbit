package io.advantageous.qbit.examples;


import io.advantageous.qbit.admin.AdminBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;

public class TodoMainWithHealth {

    public static void main(final String... args) {


        AdminBuilder adminBuilder = AdminBuilder.adminBuilder();

        ServiceEndpointServer server = EndpointServerBuilder.endpointServerBuilder().setHost("localhost")
                .setPort(8080).build().startServer();

        server.initServices(new TodoService());
    }

}

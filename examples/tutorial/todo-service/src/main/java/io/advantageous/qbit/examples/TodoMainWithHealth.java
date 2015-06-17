package io.advantageous.qbit.examples;


import io.advantageous.qbit.admin.AdminBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthServiceAsync;

public class TodoMainWithHealth {

    public static void main(final String... args) {


        final AdminBuilder adminBuilder = AdminBuilder.adminBuilder();
        final HealthServiceAsync healthService = adminBuilder.getHealthService();
        final ServiceEndpointServer server = EndpointServerBuilder
                .endpointServerBuilder()
                .setHost("localhost")
                .setPort(8080)
                .setHealthService(healthService)
                .build();

        final ServiceEndpointServer adminServer =
                adminBuilder.build();

        server.initServices(new TodoService()).startServer();


        /** Shut them down. */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            adminBuilder.getHealthServiceBuilder().getServiceQueue().stop();
            server.stop();
            adminServer.stop();
        }));

    }

}

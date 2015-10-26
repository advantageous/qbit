package io.advantageous.qbit.example;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;

public class HelloWorld {

    @RequestMapping("/hello")
    public String hello() {
        return "hello " + System.currentTimeMillis();
    }

    public static void main(final String... args) {
        final ManagedServiceBuilder managedServiceBuilder =
                ManagedServiceBuilder.managedServiceBuilder().setRootURI("/root");

        /* Start the service. */
        managedServiceBuilder.addEndpointService(new HelloWorld())
                .getEndpointServerBuilder()
                .build().startServer();

        /* Start the admin builder which exposes health end-points and meta data. */
        managedServiceBuilder.getAdminBuilder().build().startServer();

        System.out.println("Servers started");

        managedServiceBuilder.getSystemManager().waitForShutdown();


    }


}

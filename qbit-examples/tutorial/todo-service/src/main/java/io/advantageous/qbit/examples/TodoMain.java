package io.advantageous.qbit.examples;

import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;

public class TodoMain {

    public static void main(String... args) {
        ServiceServer server = new ServiceServerBuilder().setHost("localhost").setPort(8080).build();
        server.initServices(new TodoService());
        server.start();
    }

}
package io.advantageous.qbit.sample.server;


import io.advantageous.qbit.sample.server.service.TodoService;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;

/**
 * Created by rhightower on 11/5/14.
 * @author Rick Hightower
 */
public class TodoServerMain {

    public static void main(String... args) {
        ServiceServer server = new ServiceServerBuilder().setRequestBatchSize(100).build();
        server.initServices(new TodoService());
        server.start();

    }
}

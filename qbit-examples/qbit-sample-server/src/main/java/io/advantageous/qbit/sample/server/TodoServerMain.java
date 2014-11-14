package io.advantageous.qbit.sample.server;


import io.advantageous.qbit.sample.server.service.TodoService;
import io.advantageous.qbit.server.Server;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.vertx.RegisterVertxWithQBit;

/**
 * Created by rhightower on 11/5/14.
 */
public class TodoServerMain {

    public static void main(String... args) {
        Server server = new Server();
        server.run(new TodoService());

    }
}

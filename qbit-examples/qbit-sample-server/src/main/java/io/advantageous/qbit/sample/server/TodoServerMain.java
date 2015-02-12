package io.advantageous.qbit.sample.server;


import io.advantageous.qbit.sample.server.service.TodoService;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.system.QBitSystemManager;

import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;

/**
 * Created by rhightower on 11/5/14.
 * @author Rick Hightower
 */
public class TodoServerMain {

    public static void main(String... args) {

        QBitSystemManager systemManager = new QBitSystemManager();

        ServiceServer server = serviceServerBuilder()
                .setSystemManager(systemManager).build()
                .initServices(new TodoService()).startServer();

        systemManager.waitForShutdown();


    }
}

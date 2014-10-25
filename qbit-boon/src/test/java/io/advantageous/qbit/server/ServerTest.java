package io.advantageous.qbit.server;


import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.HttpResponse;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.bundle.example.todo.Todo;
import io.advantageous.qbit.service.bundle.example.todo.TodoService;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.Sets;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.Date;

import static org.boon.Boon.puts;

public class ServerTest {



    boolean ok;

    static {

        /** Boon is the default implementation but there can be others. */
        RegisterBoonWithQBit.registerBoonWithQBit();
    }

    @Test
    public void testServer() {

        ProtocolEncoder encoder = QBit.factory().createEncoder();
        MockHttpServer httpServer = new MockHttpServer();
        ServiceBundle serviceBundle = QBit.factory().createServiceBundle("/services");



        Server server = new Server(httpServer, encoder, serviceBundle);

        server.initServices(Sets.set(new TodoService()));

        server.run();

        httpServer.postRequestObject("/services/todo-manager/todo",
                new Todo("Call Dad", "Call Dad", new Date()), (code, mimeType, body) -> {

                    puts("ADD CALL RESPONSE code ", code, " mimeType ", mimeType, " body ", body);
                });


        Sys.sleep(1000);


    }
}
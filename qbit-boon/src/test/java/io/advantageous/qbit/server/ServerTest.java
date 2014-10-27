package io.advantageous.qbit.server;


import io.advantageous.qbit.BoonJsonMapper;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.HttpResponse;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.bundle.example.todo.Todo;
import io.advantageous.qbit.service.bundle.example.todo.TodoService;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.Boon;
import org.boon.Sets;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

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

        JsonMapper mapper = new BoonJsonMapper();


        Server server = new Server(httpServer, encoder, serviceBundle, mapper);

        server.initServices(Sets.set(new TodoService()));

        server.run();

        final AtomicBoolean resultsWorked = new AtomicBoolean();

        httpServer.postRequestObject("/services/todo-manager/todo",
                new Todo("Call Dad", "Call Dad", new Date()), (code, mimeType, body) -> {


                    if (body!=null && body.equals("true")) {
                        resultsWorked.set(true);
                    }
                });


        Sys.sleep(1000);


        if (!resultsWorked.get()) {
            die("Add operation did not work");
        }

        resultsWorked.set(false);

        httpServer.sendHttpGet("/services/todo-manager/todo/",
                null, (code, mimeType, body) -> {

                    puts("ADD CALL RESPONSE code ", code, " mimeType ", mimeType, " body ", body);


                    List<Todo> todos = Boon.fromJsonArray(body, Todo.class);
                    if (todos.size()>0) {
                        Todo todo = todos.get(0);
                        if(todo.getDescription().equals("Call Dad")){
                            resultsWorked.set(true);
                        }
                    }
                });


        Sys.sleep(1000);

        if (!resultsWorked.get()) {
            die("Add operation did not work");
        }

    }


    //@Test
    public void testServerTimeout() {

        ProtocolEncoder encoder = QBit.factory().createEncoder();
        MockHttpServer httpServer = new MockHttpServer();
        ServiceBundle serviceBundle = QBit.factory().createServiceBundle("/services");

        JsonMapper mapper = new BoonJsonMapper();


        Server server = new Server(httpServer, encoder, serviceBundle, mapper);

        server.initServices(Sets.set(new TodoService()));

        server.run();

        final AtomicBoolean resultsWorked = new AtomicBoolean();

        httpServer.sendHttpGet("/services/todo-manager/timeout",
                null, (code, mimeType, body) -> {


                    if (body!=null && body.equals("true")) {
                        resultsWorked.set(true);
                    }
                });


        Sys.sleep(1000);


        if (!resultsWorked.get()) {
            die(" operation did not work");
        }

        resultsWorked.set(false);

    }
}
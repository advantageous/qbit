package io.advantageous.qbit.server;


import io.advantageous.qbit.BoonJsonMapper;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.bundle.example.todo.Todo;
import io.advantageous.qbit.service.bundle.example.todo.TodoService;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
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

        ProtocolParser parser = QBit.factory().createProtocolParser();
        MockHttpServer httpServer = new MockHttpServer();
        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").build();

        JsonMapper mapper = new BoonJsonMapper();


        ServiceServerImpl server = new ServiceServerImpl(httpServer, encoder, parser,
                serviceBundle, mapper, 30, 100, 30);

        server.initServices(Sets.set(new TodoService()));

        server.start();

        final AtomicBoolean resultsWorked = new AtomicBoolean();

        httpServer.postRequestObject("/services/todo-manager/todo",
                new Todo("Call Dad", "Call Dad", new Date()), (code, mimeType, body) -> {


                    puts("CALL CALLED", body, "\n\n");
                    if (body!=null && code==200 && body.equals("\"success\"") ) {
                        resultsWorked.set(true);
                    }
                });



        Sys.sleep(1_000);


        if (!resultsWorked.get()) {
            die("Add operation did not work");
        }

        resultsWorked.set(false);

        httpServer.sendHttpGet("/services/todo-manager/todo/list/",
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

        server.flush();


        Sys.sleep(1_000);

        if (!resultsWorked.get()) {
            die("List operation did not work");
        }

    }


    @Test
    public void testServerTimeout() {

        ProtocolEncoder encoder = QBit.factory().createEncoder();
        MockHttpServer httpServer = new MockHttpServer();


        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").build();
        JsonMapper mapper = new BoonJsonMapper();


        ServiceServerImpl server = new ServiceServerImpl(httpServer, encoder, QBit.factory().createProtocolParser(), serviceBundle, mapper, 1, 100, 30);

        server.initServices(Sets.set(new TodoService()));

        server.start();

        final AtomicBoolean resultsWorked = new AtomicBoolean();

        for (int index=0; index < 100; index++) {

            httpServer.sendHttpGet("/services/todo-manager/timeout",
                    null, (code, mimeType, body) -> {


                        if (code == 408 && body != null && body.equals("\"timed out\"")) {
                            resultsWorked.set(true);
                        }
                    });
        }


        Sys.sleep(2_000);


        if (!resultsWorked.get()) {
            die(" we did not get timeout");
        }

        resultsWorked.set(false);

    }


    @Test
    public void testNoMethodCallFound() {

        ProtocolEncoder encoder = QBit.factory().createEncoder();
        MockHttpServer httpServer = new MockHttpServer();

        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/root").build();

        JsonMapper mapper = new BoonJsonMapper();


        ServiceServerImpl server = new ServiceServerImpl(httpServer, encoder,
                QBit.factory().createProtocolParser(), serviceBundle, mapper, 1, 100, 30);

        server.initServices(new TodoService());

        server.start();

        final AtomicBoolean resultsWorked = new AtomicBoolean();


        resultsWorked.set(false);

        httpServer.sendHttpGet("/services/todo-manager/testNoMethodCallFound",
                null, (code, mimeType, body) -> {


                    if (code == 404 && body!=null && body.startsWith("\"No service method for URI")) {
                        resultsWorked.set(true);
                    }
                });


        Sys.sleep(1_000);


        if (!resultsWorked.get()) {
            die(" we did not get timeout");
        }

        resultsWorked.set(false);

    }
}
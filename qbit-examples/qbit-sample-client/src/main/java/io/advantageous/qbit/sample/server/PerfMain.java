package io.advantageous.qbit.sample.server;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.sample.server.client.TodoServiceClient;
import io.advantageous.qbit.sample.server.model.TodoItem;
import org.boon.core.Sys;

import java.util.Date;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 12/4/14.
 */
public class PerfMain {



    public static void main(String... args) {

        String host = "localhost";
        int port = 8080;
        if (args.length > 1) {

            host = args[1];
        }


        if (args.length > 2) {

            port = Integer.parseInt(args[2]);
        }



        Client client = new ClientBuilder().setPort(port).setHost(host).build();

        io.advantageous.qbit.sample.server.client.TodoServiceClient todoService = client.createProxy(TodoServiceClient.class, "todo-manager");

        client.run();


        Date date = new Date();
        for (int index=0; index < 2; index++) {
            /* Add a new item. */
            todoService.add(new TodoItem("Buy Milk" + index, "Go to the grocery store and buy some milk", date));
            client.flush();

        }

        todoService.add(new TodoItem("Buy Hot dogs", "Go to 7/11 and buy some hot dogs", new Date()));




        /* Read the items back. */
        todoService.list(todoItems -> {

            for (TodoItem item : todoItems) {
                puts (item.getDescription(), item.getName(), item.getDue());
            }
        });


        Sys.sleep(2_000);

        client.stop();


    }

}

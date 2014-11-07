package io.advantageous.qbit.sample.server;

import io.advantageous.qbit.sample.server.client.TodoServiceClient;
import io.advantageous.qbit.sample.server.model.TodoItem;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.vertx.Client;
import io.advantageous.qbit.vertx.QBitClient;
import org.boon.core.Sys;

import java.util.Date;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 11/5/14.
 */
public class TodoClientMain {

    public static void main(String... args) {

        RegisterBoonWithQBit.registerBoonWithQBit();
        Client client = new Client("localhost", 8080, "/services");
        TodoServiceClient todoService = client.createProxy(TodoServiceClient.class, "todo-manager");

        client.run();

        /* Add a new item. */
        todoService.add(new TodoItem("Buy Milk", "Go to the grocery store and buy some milk", new Date()));
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

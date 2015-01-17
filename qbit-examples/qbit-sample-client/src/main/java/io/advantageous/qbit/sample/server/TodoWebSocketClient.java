package io.advantageous.qbit.sample.server;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.sample.server.client.TodoServiceClient;
import io.advantageous.qbit.sample.server.model.TodoItem;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;

import java.util.Date;
import java.util.List;

import static org.boon.Boon.puts;

/**
 * Created by Richard on 11/17/14.
 */
public class TodoWebSocketClient {



    public static void main(String... args) {

        String host = "localhost";
        int port = 8080;
        if (args.length > 1) {

            host = args[1];
        }


        if (args.length > 2) {

            port = Integer.parseInt(args[2]);
        }



        Client client = new ClientBuilder().setPort(port).setHost(host).setPollTime(10)
                .setAutoFlush(true).setFlushInterval(50).setRequestBatchSize(50)
                .setProtocolBatchSize(50).build();

        TodoServiceClient todoService = client.createProxy(TodoServiceClient.class, "todo-manager");

        client.start();

                /* Add a new item. */
        todoService.add(new TodoItem("Buy Hot dogs", "Go to 7/11 and buy some hot dogs", new Date()));
        /* Add a new item. */
        todoService.add(new TodoItem("Buy Hot dogs", "Go to 7/11 and buy some hot dogs", new Date()));





        todoService.add(new TodoItem("a" , "b", new Date()));


        todoService.size(new Callback<Integer>() {
            @Override
            public void accept(Integer size) {
                puts(" SIZE " + size);
            }
        });


        todoService.list(new Callback<List<TodoItem>>() {

            @Override
            public void accept(List<TodoItem> todoItems) {
                puts(todoItems);
            }
        });

        client.flush();



        Sys.sleep(10_000);

        client.stop();


    }



}

package io.advantageous.qbit.examples.client;


import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import org.boon.core.Sys;

/**
 * Created by fadi on 1/9/15.
 */
public class TodoMainWebSocketClient {

    public static void main(String... args) {

        String host = "localhost";

        int port = 8080;


        Client client = new ClientBuilder().setPort(port).setHost(host).setPollTime(10)
                .setAutoFlush(true).setFlushInterval(50).setRequestBatchSize(50)
                .setProtocolBatchSize(50).build();

        TodoServiceClient todoService =
                client.createProxy(TodoServiceClient.class, "todo-service");

        client.start();


        for(int index =0 ; index < 200; index++) {
        /* Add a new items. */
            todoService.add(new TodoItem("Buy Milk"));
            todoService.add(new TodoItem("Buy Hot dogs"));
        }

        client.flush();



        Sys.sleep(1000);

        for(int index =0 ; index < 200; index++) {

            todoService.list(todoItems -> { //LAMBDA EXPRESSION Java 8

                for (TodoItem item : todoItems) {
                    System.out.println(item.getDescription() + " " + item.getName() + " " + item.getDue());
                }
            });
            Sys.sleep(1000);

        }

        client.flush();

        Sys.sleep(1000);

    }
}

package io.advantageous.qbit.examples.client;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;

/**
 * Fixed by Rick on March 16th, 2015.
 * created by fadi on 1/9/15.
 */
public class TodoClientMain {

    public static void main(String... args) {

        String host = "localhost";

        int port = 8080;


        /* Create a client object.
        * A client object connects to a ServiceEndpointServer so
        * you can invoke a service over WebSocket.
        */
        final Client client = new ClientBuilder()
                .setPort(port).setHost(host).setPollTime(10)
                .setAutoFlush(true).setFlushInterval(50)
                .setRequestBatchSize(50)
                .setProtocolBatchSize(50).build();

        client.start();

        final TodoServiceClientInterface todoService =
                client.createProxy(TodoServiceClientInterface.class, "todoService");



        todoService.add(new TodoItem("Buy Milk"));
        todoService.add(new TodoItem("Buy Hot dogs"));
        todoService.list(todoItems -> { //LAMBDA EXPRESSION Java 8

            for (TodoItem item : todoItems) {
                System.out.println("TODO ITEM " +
                        item.getDescription() + " " +
                        item.getName() + " " +
                        item.getDue());
            }
        });


        Sys.sleep(1000);


    }
}

package io.advantageous.qbit.examples;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocalServiceExample {


    public interface TodoServiceAsync {

        void list(Callback<List<TodoItem>> handler);

        void add(TodoItem todoItem);

    }

    public static void main (String... args) throws Exception {

        /* Build a local queue. */
        final ServiceQueue serviceQueue = ServiceBuilder
                                    .serviceBuilder()
                                    .setServiceObject(new TodoService())
                                    .buildAndStart();

        serviceQueue.startCallBackHandler();

        /* Service proxy */
        final TodoServiceAsync todoService =
                            serviceQueue
                                    .createProxyWithAutoFlush(TodoServiceAsync.class,
                                            50, TimeUnit.MILLISECONDS);


        todoService.add(new TodoItem("Buy Milk", "task1", new Date()));
        todoService.add(new TodoItem("Buy Hot dogs", "task2", new Date()));
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

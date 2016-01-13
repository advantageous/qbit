package io.advantageous.qbit.examples;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.reactive.Callback;

import java.util.Date;
import java.util.List;

/**
 * Created by rick on 6/16/15.
 */
public class LocalServiceBundleExample {

    public interface TodoServiceAsync {

        void list(Callback<List<TodoItem>> handler);

        void add(TodoItem todoItem);

    }

    public static void main (String... args) throws Exception {

        /* Build a local queue. */
        ServiceBundle serviceBundle = ServiceBundleBuilder
                .serviceBundleBuilder().build();

        serviceBundle.addServiceObject("todo", new TodoService());


        /* Service proxy */
        final TodoServiceAsync todoService =
                serviceBundle.createLocalProxy(TodoServiceAsync.class, "todo");

        serviceBundle.start();


        todoService.add(new TodoItem("Buy Milk", "task1", new Date()));
        todoService.add(new TodoItem("Buy Hot dogs", "task2", new Date()));

        ServiceProxyUtils.flushServiceProxy(todoService);
        todoService.list(todoItems -> { //LAMBDA EXPRESSION Java 8

            for (TodoItem item : todoItems) {
                System.out.println("TODO ITEM " +
                        item.getDescription() + " " +
                        item.getName() + " " +
                        item.getDue());
            }
        });


        ServiceProxyUtils.flushServiceProxy(todoService);

        Sys.sleep(1000);

    }
}

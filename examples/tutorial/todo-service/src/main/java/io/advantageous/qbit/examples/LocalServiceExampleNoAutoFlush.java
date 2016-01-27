package io.advantageous.qbit.examples;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.Date;
import java.util.List;

public class LocalServiceExampleNoAutoFlush {


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
                                    .createProxy(TodoServiceAsync.class);

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

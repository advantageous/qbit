package io.advantageous.qbit.sample.server;

import io.advantageous.qbit.sample.server.model.TodoItem;
import org.boon.Boon;
import org.boon.HTTP;

import java.util.Date;
import java.util.List;

/**
 * Created by rhightower on 11/6/14.
 */
public class TodoRESTClient {

    public static void main(String... args) {


        String host = "localhost";
        int port = 8080;
        if (args.length > 1) {

            host = args[1];
        }


        if (args.length > 2) {

            port = Integer.parseInt(args[2]);
        }



        TodoItem todoItem = new TodoItem("Go to work", "Get on ACE train and go to Cupertino", new Date());
        HTTP.postJSON("http://" + host + ":" + port + "/services/todo-manager/todo", Boon.toJson(todoItem));

        todoItem = new TodoItem("Call Jefe", "Call Jefe", new Date());

        HTTP.postJSON("http://" + host + ":" + port + "/services/todo-manager/todo", Boon.toJson(todoItem));

        final String todoJsonList = HTTP.getJSON("http://" + host + ":" + port + "/services/todo-manager/todo/list", null);

        final List<TodoItem> todoItems = Boon.fromJsonArray(todoJsonList, TodoItem.class);

        for (TodoItem todo : todoItems) {
            System.out.println(todo.getName() + " " + todo.getDescription() + " " + todo.getDue());
        }
    }
}

package io.advantageous.qbit.sample.server;

import io.advantageous.qbit.sample.server.model.TodoItem;
import org.boon.Boon;
import org.boon.HTTP;

import java.util.Date;
import java.util.List;

import static org.boon.Boon.puts;

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



        TodoItem todoItem = new TodoItem("Go to work",
                "Get on ACE train and go to Cupertino",
                new Date());

        final String addTodoURL =
                "http://" + host + ":" + port + "/services/todo-manager/todo";

        final String readTodoListURL
                = "http://" + host + ":" + port + "/services/todo-manager/todo/list";


        puts(readTodoListURL);

        //HTTP POST
        HTTP.postJSON(addTodoURL, Boon.toJson(todoItem));

        todoItem = new TodoItem("Call Jefe", "Call Jefe", new Date());

        //HTTP POST
        HTTP.postJSON(addTodoURL, Boon.toJson(todoItem));


        //HTTP GET
        final String todoJsonList =
                HTTP.getJSON(readTodoListURL, null);

        final List<TodoItem> todoItems =
                Boon.fromJsonArray(todoJsonList, TodoItem.class);

        for (TodoItem todo : todoItems) {
            puts(todo.getName(), todo.getDescription(), todo.getDue());
        }
    }
}

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
        TodoItem todoItem = new TodoItem("Go to work", "Get on ACE train and go to Cupertino", new Date());
        HTTP.postJSON("http://localhost:8080/services/todo-manager/todo", Boon.toJson(todoItem));

        todoItem = new TodoItem("Call Jefe", "Call Jefe", new Date());

        HTTP.postJSON("http://localhost:8080/services/todo-manager/todo", Boon.toJson(todoItem));

        final String todoJsonList = HTTP.getJSON("http://localhost:8080/services/todo-manager/todo/list", null);

        final List<TodoItem> todoItems = Boon.fromJsonArray(todoJsonList, TodoItem.class);

        for (TodoItem todo : todoItems) {
            puts(todo.getName(), todo.getDescription(), todo.getDue());
        }
    }
}

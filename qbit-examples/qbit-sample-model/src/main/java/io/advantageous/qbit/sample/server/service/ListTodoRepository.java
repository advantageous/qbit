package io.advantageous.qbit.sample.server.service;

import io.advantageous.qbit.sample.server.model.TodoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhightower on 11/5/14.
 */
public class ListTodoRepository implements TodoRepository {


    List<TodoItem> todoItemList = new ArrayList<>(10);

    public List<TodoItem> list() {
        return new ArrayList<>(todoItemList);
    }

    public void add(TodoItem item) {
        todoItemList.add(item);
    }

}

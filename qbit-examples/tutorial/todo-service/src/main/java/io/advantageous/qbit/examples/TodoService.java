package io.advantageous.qbit.examples;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhightower on 1/19/15.
 */
public class TodoService {

    List<TodoItem> items = new ArrayList<>();



    public List<TodoItem> list() {
        return items;
    }

    public void add(TodoItem todoItem) {
        items.add(todoItem);
    }

}

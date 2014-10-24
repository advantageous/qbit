package io.advantageous.qbit.example.todo;

import java.util.ArrayList;
import java.util.List;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 10/24/14.
 */
public class TodoService {


    private List<TodoItem> items = new ArrayList<>();

    public void add(TodoItem todoItem) {

        puts("add method was called", todoItem);
        items.add( todoItem );

        puts("add method AFTER called", items);
    }

    public List<TodoItem> list() {
        puts("List method was called", items);
        return new ArrayList<>(items);
    }
}

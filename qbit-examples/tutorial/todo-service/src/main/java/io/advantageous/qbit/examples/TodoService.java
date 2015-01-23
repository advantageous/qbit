package io.advantageous.qbit.examples;


import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhightower on 1/19/15.
 */

@RequestMapping("/todo-service")
public class TodoService {

    List<TodoItem> items = new ArrayList<>();


    @RequestMapping("/todo/count")
    public int size() {

        return items.size();
    }



    @RequestMapping("/todo/")
    public List<TodoItem> list() {
        return items;
    }


    @RequestMapping(value = "/todo", method = RequestMethod.POST)
    public void add(TodoItem todoItem) {
        items.add(todoItem);
    }

}

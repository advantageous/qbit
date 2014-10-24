package io.advantageous.qbit.service.bundle.example.todo;

import io.advantageous.qbit.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.qbit.annotation.RequestMethod.POST;
import static org.boon.Boon.puts;

/**
 * Created by rhightower on 10/24/14.
 * @author rhightower
 */
@RequestMapping("/todo-manager")
public class TodoService {


    private List<Todo> items = new ArrayList<>();


    @RequestMapping(value = "/todo", method = POST)
    public void add(Todo todoItem) {

        puts("add method was called", todoItem);
        items.add( todoItem );

        puts("add method AFTER called", items);
    }

    @RequestMapping("/todo/")
    public List<Todo> list() {
        puts("List method was called", items);
        return new ArrayList<>(items);
    }

}

package io.advantageous.qbit.sample.server.service;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.sample.server.model.TodoItem;

import java.util.List;

/**
 * Created by rhightower on 11/5/14.
 */

@RequestMapping("/todo-manager")
public class TodoService {


    private final TodoRepository todoRepository = new ListTodoRepository();

    int adds = 0;

    @RequestMapping("/todo/list")
    public int size() {

        return adds;
    }


    @RequestMapping("/todo/size")
    public List<TodoItem> list() {

        return todoRepository.list();
    }

    @RequestMapping(value = "/todo", method = RequestMethod.POST)
    public void add(TodoItem item) {
        adds++;

        if (adds<10_000) {
            todoRepository.add(item);
        }
    }
}

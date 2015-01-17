package io.advantageous.qbit.sample.server.service;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.sample.server.model.TodoItem;

import java.util.List;

/**
 * Created by rhightower on 11/5/14.
 */

@RequestMapping("/todo-manager")
public class TodoService  {


    private final TodoRepository todoRepository = new ListTodoRepository();


    @RequestMapping("/todo/size")
    public int size() {

        return todoRepository.size();
    }


    @RequestMapping("/todo/list")
    public List<TodoItem> list() {

        System.out.println("LIST");
        return todoRepository.list();
    }

    @RequestMapping(value = "/todo", method = RequestMethod.POST)
    public void add(TodoItem item) {
            todoRepository.add(item);
    }
}

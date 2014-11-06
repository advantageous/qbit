package io.advantageous.qbit.sample.server;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;

import java.util.List;

/**
 * Created by rhightower on 11/5/14.
 */
public class TodoService {


    private final TodoRepository todoRepository = new ListTodoRepository();

    @RequestMapping("/todo/list")
    List<TodoItem> list() {

        return todoRepository.list();
    }

    @RequestMapping(value = "/todo", method = RequestMethod.POST)
    void add(TodoItem item) {

        todoRepository.add(item);
    }
}

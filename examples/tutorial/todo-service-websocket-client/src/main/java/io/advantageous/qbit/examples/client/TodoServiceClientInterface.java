package io.advantageous.qbit.examples.client;


import io.advantageous.qbit.reactive.Callback;

import java.util.List;

public interface TodoServiceClientInterface {

    void list(Callback<List<TodoItem>> handler);

    void add(TodoItem todoItem);

}
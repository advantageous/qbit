package io.advantageous.qbit.examples.client;

import io.advantageous.qbit.service.Callback;

import java.util.List;

public interface TodoServiceClient {

    void list(Callback<List<TodoItem>> handler);

    void add(TodoItem todoItem);


}
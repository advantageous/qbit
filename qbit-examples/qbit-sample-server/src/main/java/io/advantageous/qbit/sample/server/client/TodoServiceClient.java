package io.advantageous.qbit.sample.server.client;

import io.advantageous.qbit.sample.server.model.TodoItem;
import io.advantageous.qbit.service.Callback;

import java.util.List;

/**
 * Created by rhightower on 11/5/14.
 */
public interface TodoServiceClient {

        void list(Callback<List<TodoItem>> handler);

        void add(TodoItem todoItem);


}

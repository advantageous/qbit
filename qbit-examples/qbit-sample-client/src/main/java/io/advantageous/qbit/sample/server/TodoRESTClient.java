/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.sample.server;

import io.advantageous.boon.Boon;
import io.advantageous.boon.HTTP;
import io.advantageous.qbit.sample.server.model.TodoItem;

import java.util.Date;
import java.util.List;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by rhightower on 11/6/14.
 */
public class TodoRESTClient {

    public static void main(String... args) {


        String host = "localhost";
        int port = 8080;
        if (args.length > 1) {

            host = args[1];
        }


        if (args.length > 2) {

            port = Integer.parseInt(args[2]);
        }


        TodoItem todoItem = new TodoItem("Go to work",
                "Get on ACE train and go to Cupertino",
                new Date());

        final String addTodoURL =
                "http://" + host + ":" + port + "/services/todo-manager/todo";

        final String readTodoListURL
                = "http://" + host + ":" + port + "/services/todo-manager/todo/list";


        puts(readTodoListURL);

        //HTTP POST
        HTTP.postJSON(addTodoURL, Boon.toJson(todoItem));

        todoItem = new TodoItem("Call Jefe", "Call Jefe", new Date());

        //HTTP POST
        HTTP.postJSON(addTodoURL, Boon.toJson(todoItem));


        //HTTP GET
        final String todoJsonList =
                HTTP.getJSON(readTodoListURL, null);

        final List<TodoItem> todoItems =
                Boon.fromJsonArray(todoJsonList, TodoItem.class);

        for (TodoItem todo : todoItems) {
            puts(todo.getName(), todo.getDescription(), todo.getDue());
        }
    }
}

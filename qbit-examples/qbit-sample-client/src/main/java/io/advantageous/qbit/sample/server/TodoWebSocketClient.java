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

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.sample.server.client.TodoServiceClient;
import io.advantageous.qbit.sample.server.model.TodoItem;
import io.advantageous.qbit.service.Callback;
import io.advantageous.boon.core.Sys;

import java.util.Date;
import java.util.List;

import static io.advantageous.boon.Boon.puts;


/**
 * Created by Richard on 11/17/14.
 */
public class TodoWebSocketClient {


    public static void main(String... args) {

        String host = "localhost";
        int port = 8080;
        if (args.length > 1) {

            host = args[1];
        }


        if (args.length > 2) {

            port = Integer.parseInt(args[2]);
        }


        Client client = new ClientBuilder().setPort(port).setHost(host).setPollTime(10)
                .setAutoFlush(true).setFlushInterval(50).setRequestBatchSize(50)
                .setProtocolBatchSize(50).build();

        TodoServiceClient todoService = client.createProxy(TodoServiceClient.class, "todo-manager");

        client.start();

                /* Add a new item. */
        todoService.add(new TodoItem("Buy Hot dogs", "Go to 7/11 and buy some hot dogs", new Date()));
        /* Add a new item. */
        todoService.add(new TodoItem("Buy Hot dogs", "Go to 7/11 and buy some hot dogs", new Date()));


        todoService.add(new TodoItem("a", "b", new Date()));


        todoService.size(new Callback<Integer>() {
            @Override
            public void accept(Integer size) {
                puts(" SIZE " + size);
            }
        });


        todoService.list(new Callback<List<TodoItem>>() {

            @Override
            public void accept(List<TodoItem> todoItems) {
                puts(todoItems);
            }
        });

        client.flush();


        Sys.sleep(10_000);

        client.stop();


    }


}

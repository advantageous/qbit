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

package io.advantageous.qbit.service.bundle.example.todo;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.annotation.RequestMethod.POST;

/**
 * created by rhightower on 10/24/14.
 *
 * @author rhightower
 */
@SuppressWarnings("ALL")
@RequestMapping("/todo-manager")
public class TodoService {


    private List<Todo> items = new ArrayList<>();


    @RequestMapping(value = "/timeout")
    public boolean timeout() {

        while (true) {
            Sys.sleep(1000);
        }

    }

    @RequestMapping(value = "/todo", method = POST)
    public void add(Todo todoItem) {

        puts("add serviceCall was called", todoItem);
        items.add(todoItem);

        puts("add serviceCall AFTER called", items);
    }

    @RequestMapping("/todo/list/")
    public List<Todo> list() {
        puts("List serviceCall was called", items);
        return new ArrayList<>(items);
    }

}

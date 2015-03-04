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

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.boon.core.Sys;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static io.advantageous.boon.Exceptions.die;


/**
 * Created by rhightower on 10/24/14.
 */
public class TodoServiceWithServiceBundleTest {

    boolean ok;

    static {

        /** Boon is the default implementation but there can be others. */
        RegisterBoonWithQBit.registerBoonWithQBit();
    }

    @Test
    public void testWithBundleUsingAddress() {

        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").buildAndStart();

        serviceBundle.addService(new TodoService());


        Todo todoItem = new Todo("call mom", "give mom a call", new Date());

        MethodCall<Object> addMethod = QBit.factory().createMethodCallByAddress("/services/todo-manager/add", "client1", todoItem, null);


        serviceBundle.call(addMethod);


        MethodCall<Object> listMethod = QBit.factory().createMethodCallByAddress("/services/todo-manager/list", "client1", null, null);

        serviceBundle.call(listMethod);

        serviceBundle.flush();

        Sys.sleep(100);

        ReceiveQueue<Response<Object>> responses = serviceBundle.responses().receiveQueue();

        Response<Object> response = responses.take();

        Object body = response.body();

        if (body instanceof List) {
            List<Todo> items = (List) body;

            ok = items.size() > 0 || die("items should have one todo in it");

            Todo todoItem1 = items.get(0);

            ok = todoItem.equals(todoItem1) || die("TodoItem ", todoItem, todoItem1);


        } else {
            die("Response was not a list", body);
        }


    }


    @Test
    public void testWithBundleUsingObjectName() {
        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").buildAndStart();

        serviceBundle.addService(new TodoService());


        Todo todoItem = new Todo("call mom", "give mom a call", new Date());
        MethodCall<Object> addMethodCall = QBit.factory().createMethodCallByNames("add", "/services/todo-manager", "call1:localhost", todoItem, null);

        serviceBundle.call(addMethodCall);


        MethodCall<Object> listMethodCall = QBit.factory().createMethodCallByNames("list", "/services/todo-manager", "call2:localhost", todoItem, null);

        serviceBundle.call(listMethodCall);

        serviceBundle.flush();


        Sys.sleep(100);

        ReceiveQueue<Response<Object>> responses = serviceBundle.responses().receiveQueue();

        Response<Object> response = responses.take();

        Object body = response.body();

        if (body instanceof List) {
            List<Todo> items = (List) body;

            ok = items.size() > 0 || die("items should have one todo in it");

            Todo todoItem1 = items.get(0);

            ok = todoItem.equals(todoItem1) || die("TodoItem ", todoItem, todoItem1);


        } else {
            die("Response was not a list", body);
        }


    }


    @Test
    public void testWithBundleUsingAddressRequestMappings() {
        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").buildAndStart();

        serviceBundle.addService(new TodoService());


        Todo todoItem = new Todo("call mom", "give mom a call", new Date());

        MethodCall<Object> addMethod = QBit.factory().createMethodCallByAddress("/services/todo-manager/todo", "client1", todoItem, null);


        serviceBundle.call(addMethod);


        MethodCall<Object> listMethod = QBit.factory().createMethodCallByAddress("/services/todo-manager/todo/list/", "client1", null, null);

        serviceBundle.call(listMethod);

        serviceBundle.flush();

        Sys.sleep(100);

        ReceiveQueue<Response<Object>> responses = serviceBundle.responses().receiveQueue();

        Response<Object> response = responses.take();

        Object body = response.body();

        if (body instanceof List) {
            List<Todo> items = (List) body;

            ok = items.size() > 0 || die("items should have one todo in it");

            Todo todoItem1 = items.get(0);

            ok = todoItem.equals(todoItem1) || die("TodoItem ", todoItem, todoItem1);


        } else {
            die("Response was not a list", body);
        }


    }


}

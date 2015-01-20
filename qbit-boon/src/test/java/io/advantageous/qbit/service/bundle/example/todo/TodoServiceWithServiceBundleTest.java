package io.advantageous.qbit.service.bundle.example.todo;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.boon.Exceptions.die;

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


        ServiceBundle serviceBundle = QBit.factory()
                .createServiceBundle("/services");
        serviceBundle.addService(new TodoService());


        Todo todoItem = new Todo("call mom", "give mom a call",
                new Date());

        MethodCall<Object> addMethod = QBit.factory()
                .createMethodCallByAddress("/services/todo-manager/add", "client1",
                todoItem, null);


        serviceBundle.call(addMethod);


        MethodCall<Object> listMethod = QBit.factory()
                .createMethodCallByAddress("/services/todo-manager/list", "client1",
                null, null);

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
        ServiceBundle serviceBundle = QBit.factory().createServiceBundle("/services");
        serviceBundle.addService(new TodoService());


        Todo todoItem = new Todo("call mom", "give mom a call", new Date());
        MethodCall<Object> addMethodCall = QBit.factory().createMethodCallByNames("add", "/services/todo-manager", "call1:localhost",
                todoItem, null);

        serviceBundle.call(addMethodCall);


        MethodCall<Object> listMethodCall = QBit.factory().createMethodCallByNames("list", "/services/todo-manager", "call2:localhost",
                todoItem, null);

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
        ServiceBundle serviceBundle = QBit.factory().createServiceBundle("/services");
        serviceBundle.addService(new TodoService());


        Todo todoItem = new Todo("call mom", "give mom a call", new Date());

        MethodCall<Object> addMethod = QBit.factory().createMethodCallByAddress("/services/todo-manager/todo", "client1",
                todoItem, null);


        serviceBundle.call(addMethod);


        MethodCall<Object> listMethod = QBit.factory().createMethodCallByAddress("/services/todo-manager/todo/list/", "client1",
                null, null);

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

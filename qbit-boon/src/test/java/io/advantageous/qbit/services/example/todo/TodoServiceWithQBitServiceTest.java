package io.advantageous.qbit.services.example.todo;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 10/24/14.
 * @author rhightower
 */
public class TodoServiceWithQBitServiceTest {

    boolean ok;

    static {

        /** Boon is the default implementation but there can be others. */
        RegisterBoonWithQBit.registerBoonWithQBit();
    }

    @Test
    public void testCallbackWithObjectNameAndMethodName() {


        Service service = QBit.factory().createService("/services", "/todo-service", new TodoService(), null).start();


        SendQueue<MethodCall<Object>> requests = service.requests();

        TodoItem todoItem = new TodoItem("call mom", "give mom a call", new Date());
        MethodCall<Object> addMethodCall = QBit.factory().createMethodCallByNames("add", "/todo-service", "call1:localhost",
                todoItem, null);


        requests.send(addMethodCall);

        MethodCall<Object> listMethodCall = QBit.factory().createMethodCallByNames("list", "/todo-service", "call2:localhost",
                todoItem, null);

        requests.sendAndFlush(listMethodCall);

        Sys.sleep(100);

        ReceiveQueue<Response<Object>> responses = service.responses();

        Response<Object> response = responses.take();

        Object body = response.body();

        if (body instanceof List) {
            List<TodoItem> items = (List) body;

            ok = items.size() > 0 || die("items should have one todo in it");

            TodoItem todoItem1 = items.get(0);

            ok = todoItem.equals(todoItem1) || die("TodoItem ", todoItem, todoItem1);


        } else {
            die("Response was not a list", body);
        }
    }


    @Test
    public void testCallbackWithAddress() {


        Service service = QBit.factory().createService("/services", "/todo-service", new TodoService(), null).start();

        SendQueue<MethodCall<Object>> requests = service.requests();

        TodoItem todoItem = new TodoItem("call mom", "give mom a call", new Date());
        MethodCall<Object> addMethodCall = QBit.factory().createMethodCallByAddress("/services/todo-service/add", "call1:localhost",
                todoItem, null);


        requests.send(addMethodCall);

        MethodCall<Object> listMethodCall = QBit.factory().createMethodCallByAddress("/services/todo-service/list", "call2:localhost",
                todoItem, null);

        requests.sendAndFlush(listMethodCall);

        Sys.sleep(100);

        ReceiveQueue<Response<Object>> responses = service.responses();

        Response<Object> response = responses.take();

        Object body = response.body();

        if (body instanceof List) {
            List<TodoItem> items = (List) body;

            ok = items.size() > 0 || die("items should have one todo in it");

            TodoItem todoItem1 = items.get(0);

            ok = todoItem.equals(todoItem1) || die("TodoItem ", todoItem, todoItem1);


        } else {
            die("Response was not a list", body);
        }
    }
}

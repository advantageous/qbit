package org.qbit.service;

import org.boon.Lists;
import org.boon.core.Sys;
import org.junit.Test;
import org.qbit.message.MethodCall;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.SendQueue;
import org.qbit.service.method.impl.MethodCallImpl;
import org.qbit.message.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 8/26/14.
 */
public class RegularCalls {

    public static class Adder {
        int all;
        int add(int a, int b) {
            int total;

            total = a + b;
            all += total;
            return total;
        }

        void queueIdle() {
            puts("Queue Idle");
        }


        void queueEmpty() {
            puts("Queue Empty");
        }


        void queueShutdown() {
            puts("Queue Shutdown");
        }


        void queueLimit() {
            puts("Queue Limit");
        }
    }

    boolean ok;

    @Test
    public void test() {

        Adder adder = new Adder();
        Service service = Services.regularService("test", adder, 1000, TimeUnit.MILLISECONDS, 10);
        SendQueue<MethodCall<Object>> requests = service.requests();
        ReceiveQueue<Response<Object>> responses = service.responses();

        requests.send(MethodCallImpl.method("add", Lists.list(1, 2)));

        requests.sendAndFlush(MethodCallImpl.methodWithArgs("add", 4, 5));


        Response<Object> response = responses.take();


        Object o = response.body();

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = responses.take();

        ok = response != null || die(response);

        o = response.body();

        ok = o.equals(Integer.valueOf(9)) || die(response);





        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }


        List<MethodCall<Object>> methods = new ArrayList<>();

        for (int index = 0; index < 1000; index++) {
            methods.add(MethodCallImpl.method("add", Lists.list(1, 2)));
        }

        requests.sendBatch(methods);

        Sys.sleep(3000);

        synchronized (adder) {
            ok = adder.all == 3012 || die(adder.all);
        }

        service.stop();

        Sys.sleep(100);

    }


    @Test
    public void testMany() {

        Adder adder = new Adder();


        Service service = Services.regularService("test", adder, 1000, TimeUnit.MILLISECONDS, 10);
        SendQueue<MethodCall<Object>> requests = service.requests();
        ReceiveQueue<Response<Object>> responses = service.responses();

        requests.sendMany(MethodCallImpl.method("add",
                            Lists.list(1, 2)),
                        MethodCallImpl.method("add",
                                Lists.list(4, 5)));



        Response<Object> response = responses.take();


        Object o = response.body();

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = responses.take();

        ok = response != null || die(response);

        o = response.body();

        ok = o.equals(Integer.valueOf(9)) || die(response);




        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }
    }



    @Test
    public void testBatch() {

        Adder adder = new Adder();
        Service service = Services.regularService("test", adder, 1000, TimeUnit.MILLISECONDS, 10);
        SendQueue<MethodCall<Object>> requests = service.requests();
        ReceiveQueue<Response<Object>> responses = service.responses();

        List<MethodCall<Object>> methods = Lists.list(
                MethodCallImpl.method("add", Lists.list(1, 2)),
                MethodCallImpl.method("add", Lists.list(4, 5)));


        requests.sendBatch(methods);




        Response<Object> response = responses.take();


        Object o = response.body();

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = responses.take();

        ok = response != null || die(response);

        o = response.body();

        ok = o.equals(Integer.valueOf(9)) || die(response);





        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }
    }

}

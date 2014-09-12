package org.qbit.service;

import org.boon.Lists;
import org.junit.Test;
import org.qbit.message.MethodCall;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.SendQueue;
import org.qbit.service.method.impl.MethodCallImpl;
import org.qbit.message.Response;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.boon.Boon.fromJson;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 8/11/14.
 */
public class JsonServiceCallTest {

    public static class Adder {
        int all;
        int add(int a, int b) {
            int total;

            total = a + b;
            all += total;
            return total;
        }
    }

    boolean ok;

    @Test
    public void test() {

        Adder adder = new Adder();
        Service methodQueue = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);


        methodQueue.requests().send(MethodCallImpl.method("add", "[1,2]"));

        methodQueue.requests().send(MethodCallImpl.method("add", "[4,5]"));


        Response<Object> response = methodQueue.responses().take();


        Object o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = methodQueue.responses().take();

        ok = response != null || die(response);

        o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(9)) || die(response);





        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }
    }


    @Test
    public void testMany() {

        Adder adder = new Adder();
        Service methodQueue = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);
        final ReceiveQueue<Response<Object>> responses = methodQueue.responses();
        final SendQueue<MethodCall<Object>> requests = methodQueue.requests();


        requests.sendMany(MethodCallImpl.method("add", "[1,2]"), MethodCallImpl.method("add", "[4,5]"));



        Response<Object> response = responses.take();

        Object o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = responses.take();

        ok = response != null || die(response);

        o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(9)) || die(response);




        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }
    }



    @Test
    public void testBatch() {

        Adder adder = new Adder();
        Service methodQueue = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);

        List<MethodCall<Object>> methods = Lists.list(MethodCallImpl.method("add", "[1,2]"), MethodCallImpl.method("add", "[4,5]"));
        methodQueue.requests().sendBatch(methods);



        Response<Object> response = methodQueue.responses().take();

        ok = response != null || die(response);


        Object o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = methodQueue.responses().take();

        ok = response != null || die(response);

        o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(9)) || die(response);



        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }
    }

}

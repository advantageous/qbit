package org.qbit.service;

import org.boon.Lists;
import org.boon.core.Sys;
import org.junit.Test;

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
        Service methodQueue = Services.regularService(adder, 1000, TimeUnit.MILLISECONDS, 10);


        methodQueue.requests().offer(MethodCallImpl.method("add", Lists.list(1, 2)));

        methodQueue.requests().offer(MethodCallImpl.methodWithArgs("add", 4, 5));


        Response<Object> response = methodQueue.responses().take();


        Object o = response.body();

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = methodQueue.responses().take();

        ok = response != null || die(response);

        o = response.body();

        ok = o.equals(Integer.valueOf(9)) || die(response);





        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }


        List<MethodCall> methods = new ArrayList<MethodCall>();

        for (int index = 0; index < 1000; index++) {
            methods.add(MethodCallImpl.method("add", Lists.list(1, 2)));
        }

        methodQueue.requests().offerBatch(methods);

        Sys.sleep(3000);

        synchronized (adder) {
            ok = adder.all == 3012 || die(adder.all);
        }

        methodQueue.stop();

        Sys.sleep(100);

    }


    @Test
    public void testMany() {

        Adder adder = new Adder();
        Service methodQueue = Services.regularService(adder, 1000, TimeUnit.MILLISECONDS, 100);


        methodQueue.requests().offerMany(MethodCallImpl.method("add", Lists.list(1, 2)), MethodCallImpl.method("add", Lists.list(4, 5)));



        Response<Object> response = methodQueue.responses().take();


        Object o = response.body();

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = methodQueue.responses().take();

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
        Service methodQueue = Services.regularService(adder, 1000, TimeUnit.MILLISECONDS, 100);

        List<MethodCall> methods = Lists.list(MethodCallImpl.method("add", Lists.list(1, 2)), MethodCallImpl.method("add", Lists.list(4, 5)));
        methodQueue.requests().offerBatch(methods);




        Response<Object> response = methodQueue.responses().take();


        Object o = response.body();

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = methodQueue.responses().take();

        ok = response != null || die(response);

        o = response.body();

        ok = o.equals(Integer.valueOf(9)) || die(response);





        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }
    }

}

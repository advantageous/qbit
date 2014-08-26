package org.qbit.service;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 8/11/14.
 */
public class MethodQueueTest {

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
        ServiceImpl methodQueue = new ServiceImpl(adder, 1000, TimeUnit.MILLISECONDS, 100);

        methodQueue.requests().offer(MethodImpl.method("add", "[1,2]"));

        methodQueue.requests().offer(MethodImpl.method("add", "[4,5]"));


        Response<Object> response = methodQueue.responses().take();

        ok = response != null || die(response);

        ok = response.body().equals(Integer.valueOf(3)) || die(response);

        response = methodQueue.responses().take();

        ok = response != null || die(response);

        ok = response.body().equals(Integer.valueOf(9)) || die(response);



        synchronized (adder) {
            ok = adder.all == 12 || die(adder.all);
        }
    }
}

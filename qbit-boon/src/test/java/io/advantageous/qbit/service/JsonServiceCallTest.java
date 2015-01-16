package io.advantageous.qbit.service;

import io.advantageous.qbit.Services;
import io.advantageous.qbit.message.MethodCallBuilder;
import org.boon.Lists;
import org.junit.Test;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.message.Response;

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
        Service service = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);

        ReceiveQueue<Response<Object>> responses = service.responses();
        SendQueue<MethodCall<Object>> requests = service.requests();



        requests.send(MethodCallBuilder.method("add", "[1,2]"));

        requests.send(MethodCallBuilder.method("add", "[4,5]"));
        requests.flushSends();

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
    public void testMany() {

        Adder adder = new Adder();

        Service service = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);

        ReceiveQueue<Response<Object>> responses = service.responses();
        SendQueue<MethodCall<Object>> requests = service.requests();


        requests.sendMany(
                MethodCallBuilder.method("add", "[1,2]"),
                MethodCallBuilder.method("add", "[4,5]"));



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

        Service service = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);

        ReceiveQueue<Response<Object>> responses = service.responses();
        SendQueue<MethodCall<Object>> requests = service.requests();

        List<MethodCall<Object>> methods = Lists.list(
                MethodCallBuilder.method("add", "[1,2]"),
                MethodCallBuilder.method("add", "[4,5]"));

        requests.sendBatch(methods);



        Response<Object> response = responses.take();

        ok = response != null || die(response);


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

}

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

package io.advantageous.qbit.service;

import io.advantageous.qbit.Services;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.boon.Lists;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.advantageous.boon.Boon.fromJson;
import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 8/11/14.
 */
public class JsonServiceCallTest {

    boolean ok;

    @Test
    public void test() {

        Adder adder = new Adder();
        ServiceQueue serviceQueue = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);

        ReceiveQueue<Response<Object>> responses = serviceQueue.responses();
        SendQueue<MethodCall<Object>> requests = serviceQueue.requests();


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


        synchronized ( adder ) {
            ok = adder.all == 12 || die(adder.all);
        }
    }

    @Test
    public void testMany() {

        Adder adder = new Adder();

        ServiceQueue serviceQueue = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);

        ReceiveQueue<Response<Object>> responses = serviceQueue.responses();
        SendQueue<MethodCall<Object>> requests = serviceQueue.requests();


        requests.sendMany(MethodCallBuilder.method("add", "[1,2]"), MethodCallBuilder.method("add", "[4,5]"));


        Response<Object> response = responses.take();

        Object o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = responses.take();

        ok = response != null || die(response);

        o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(9)) || die(response);


        synchronized ( adder ) {
            ok = adder.all == 12 || die(adder.all);
        }
    }

    @Test
    public void testBatch() {

        Adder adder = new Adder();

        ServiceQueue serviceQueue = Services.jsonService("test", adder, 1000, TimeUnit.MILLISECONDS, 100);

        ReceiveQueue<Response<Object>> responses = serviceQueue.responses();
        SendQueue<MethodCall<Object>> requests = serviceQueue.requests();

        List<MethodCall<Object>> methods = Lists.list(MethodCallBuilder.method("add", "[1,2]"), MethodCallBuilder.method("add", "[4,5]"));

        requests.sendBatch(methods);


        Response<Object> response = responses.take();

        ok = response != null || die(response);


        Object o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(3)) || die(response);

        response = responses.take();

        ok = response != null || die(response);

        o = fromJson(response.body().toString());

        ok = o.equals(Integer.valueOf(9)) || die(response);


        synchronized ( adder ) {
            ok = adder.all == 12 || die(adder.all);
        }
    }

    public static class Adder {
        int all;

        int add(int a, int b) {
            int total;

            total = a + b;
            all += total;
            return total;
        }
    }

}

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

package io.advantageous.qbit.service.impl;

import io.advantageous.boon.Boon;
import io.advantageous.boon.Lists;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.util.MultiMap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;

public class ServiceBundleImplTest {


    ServiceBundle serviceBundle;
    ServiceBundleImpl serviceBundleImpl;
    AdderService adderService;

    Factory factory;
    MultiMap<String, String> params = null;
    MethodCall<Object> call = null;
    boolean ok;


    ReceiveQueue<Response<Object>> responseReceiveQueue = null;

    Response<Object> response;

    Object responseBody = null;

    volatile int callCount = 0;
    MockServiceInterface proxy;

    @Before
    public void before() {

        factory = QBit.factory();

        final ServiceBundle bundle = new ServiceBundleBuilder().setAddress("/foo").buildAndStart();
        serviceBundle = bundle;
        serviceBundleImpl = (ServiceBundleImpl) bundle;
        adderService = new AdderService();
        callCount = 0;

    }

    @Test
    public void test() {

        serviceBundle.addService(new MockService());
        proxy = serviceBundle.createLocalProxy(MockServiceInterface.class, "mockService");
        serviceBundle.startReturnHandlerProcessor();

        proxy.method1();
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount == 1 || die();
    }


    @Test
    public void testWithService() {

        final ServiceQueue serviceQueue = serviceBuilder().setServiceObject(new MockService()).build();
        //serviceBundle.addService(new MockService());
        serviceBundle.addServiceQueue("mockService", serviceQueue);
        proxy = serviceBundle.createLocalProxy(MockServiceInterface.class, "mockService");
        serviceBundle.startReturnHandlerProcessor();

        proxy.method1();
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount == 1 || die();
    }

    @Test
    public void testCallback() throws Exception {


        serviceBundle.addService(new MockService());
        proxy = serviceBundle.createLocalProxy(MockServiceInterface.class, "mockService");
        serviceBundle.startReturnHandlerProcessor();

        AtomicInteger returnValue = new AtomicInteger();
        proxy.method2(integer -> {
            returnValue.set(integer);
        });
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount == 1 || die();

        ok = returnValue.get() == 1 || die(returnValue.get());
    }


    @Test
    public void testCallbackWithCallBackInService() throws Exception {


        serviceBundle.addService(new MockService());
        proxy = serviceBundle.createLocalProxy(MockServiceInterface.class, "mockService");
        serviceBundle.startReturnHandlerProcessor();

        AtomicReference<String> str = new AtomicReference<>();

        AtomicInteger returnValue = new AtomicInteger();
        proxy.methodWithCallBack(new Callback<String>() {
            @Override
            public void accept(String s) {
                puts("###############", s);
                str.set(s);
            }
        });
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount == 1 || die();

        ok = str.get().equals("hello") || die();
    }

    @Test
    public void testAddress() throws Exception {

        Str.equalsOrDie("/foo", serviceBundle.address());

    }

    @Test
    public void testAddService() throws Exception {

        serviceBundle.addServiceObject("/adder", adderService);
        final List<String> endPoints = serviceBundle.endPoints();
        puts(endPoints);
        endPoints.contains("/foo/adder");
    }

    @Test
    public void testResponses() throws Exception {

        call = factory.createMethodCallByAddress("/foo/adder/add", "", Lists.list(1, 2), params);
        serviceBundle.addServiceObject("/adder", adderService);

        serviceBundle.call(call);

        serviceBundle.flushSends();

        Sys.sleep(1000);

        responseReceiveQueue = serviceBundle.responses().receiveQueue();

        serviceBundle.flush();

        Sys.sleep(200);

        response = responseReceiveQueue.pollWait();

        responseBody = response.body();

        int sum = Conversions.toInt(responseBody);

        Boon.equalsOrDie("Sum should be 3", 3, sum);

        serviceBundle.stop();
    }

    @Test
    public void testCall() throws Exception {

    }


    interface MockServiceInterface {
        void method1();

        void method2(Callback<Integer> count);

        void clientProxyFlush();

        void methodWithCallBack(Callback<String> callback);


    }

    public static class AdderService {
        int sum;

        public int add(int a, int b) {

            puts("ADDER SERVICE CALLED", a, b);
            sum += (a + b);
            return a + b;
        }
    }

    class MockService {
        public void method1() {
            callCount++;
        }

        public void methodWithCallBack(Callback<String> callback) {
            callCount++;
            callback.accept("hello");
        }

        public int method2() {
            ++callCount;
            return callCount;
        }
    }
}

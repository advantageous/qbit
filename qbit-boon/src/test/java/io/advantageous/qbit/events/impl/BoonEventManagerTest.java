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

package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.Listen;
import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventSubscriber;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.test.TimedTesting;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.qbit.events.EventUtils.callbackEventListener;
import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;
import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class BoonEventManagerTest extends TimedTesting {

    EventManager eventManager;

    ClientProxy clientProxy;

    volatile int subscribeMessageCount = 0;

    volatile int consumerMessageCount = 0;

    boolean ok;

    @Before
    public void setup() {

        super.setupLatch();
        eventManager = QBit.factory().systemEventManager();
        clientProxy = (ClientProxy) eventManager;
        subscribeMessageCount = 0;
        consumerMessageCount = 0;

    }


    @Test
    public void test() throws Exception {


        String rick = "rick";

        MyEventListener myEventListener = new MyEventListener();

        eventManager.listen(myEventListener);
        clientProxy.clientProxyFlush();

        eventManager.register(rick, new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                //puts(event);
                consumerMessageCount++;
            }
        });


        eventManager.register(rick, new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                //puts(event);
                subscribeMessageCount++;
            }
        });

        eventManager.register(rick, callbackEventListener(event -> {
            if (subscribeMessageCount < 1000) puts(event);
            subscribeMessageCount++;
        }));


        final MyServiceConsumer myServiceConsumer = new MyServiceConsumer();

        final MyService myService = new MyService();

        Service consumerService = serviceBuilder().setServiceObject(myServiceConsumer).setInvokeDynamic(false).build().start();

        clientProxy.clientProxyFlush();

        Sys.sleep(100);


        eventManager.send(rick, "Hello Rick");
        clientProxy.clientProxyFlush();

        Sys.sleep(1000);

        ok = subscribeMessageCount == 2 || die(subscribeMessageCount);

        ok = consumerMessageCount == 1 || die();


        ok = myEventListener.callCount == 1 || die();

        ok = myServiceConsumer.callCount() == 1 || die();


        Sys.sleep(100);
        Service senderService = serviceBuilder().setServiceObject(myService).setInvokeDynamic(false).build().start();

        final MyServiceClient clientProxy = senderService.createProxy(MyServiceClient.class);

        clientProxy.sendHi("Hello");
        ServiceProxyUtils.flushServiceProxy(clientProxy);

        Sys.sleep(100);

        ok = subscribeMessageCount == 4 || die(subscribeMessageCount);

        ok = consumerMessageCount == 2 || die();


        ok = myEventListener.callCount == 2 || die();

        ok = myServiceConsumer.callCount() == 2 || die();

    }


    //@Test This takes a long time to run. I only need it for perf tuning.
    public void testPerfMultiple() throws Exception {

        for (int index = 0; index < 5; index++) {
            testPerf();
            Sys.sleep(5_000);
        }
    }

    @Test
    public void testPerf() throws Exception {


        eventManager = QBit.factory().systemEventManager();
        consumerMessageCount = 0;
        Sys.sleep(100);
        subscribeMessageCount = 0;
        Sys.sleep(100);


        String rick = "rick";

        eventManager.register(rick, event -> consumerMessageCount++);


        eventManager.register(rick, callbackEventListener(event -> {
            subscribeMessageCount++;
        }));

        clientProxy.clientProxyFlush();
        Sys.sleep(100);


        long start = System.currentTimeMillis();

        for (int index = 0; index < 100_000; index++) {
            eventManager.send(rick, "PERF");

        }


        clientProxy.clientProxyFlush();
        Sys.sleep(100);


        super.waitForTrigger(60, o -> consumerMessageCount >= 90_000);


        long stop = System.currentTimeMillis();
        Sys.sleep(100);


        long duration = (stop - start);

        if (duration > 10_000) {
            die("duration", duration);
        }


        if (consumerMessageCount < 90_000) {
            die("consumerMessageCount", consumerMessageCount);
        }

        puts("Duration to sendText messages", duration, "ms. \nconsume message count", consumerMessageCount, "\ntotal message count", consumerMessageCount + subscribeMessageCount);


    }


    public static interface MyServiceClient {

        void sendHi(String hi);
    }

    public static class MyEventListener {

        volatile int callCount = 0;

        @Listen("rick")
        void listen(String message) {
            callCount++;
        }
    }

    public static class MyService {

        private void queueInit() {
            puts("QUEUE START MyService");
        }

        public void sendHi(String hi) {
            serviceContext().send("rick", "hello rick " + hi);
        }
    }

    public static class MyServiceConsumer {

        int callCount = 0;

        public MyServiceConsumer() {
            puts("MyService created");
        }


        @OnEvent("rick")
        private void listen(String message) {
            //puts(message);
            callCount++;
        }


        private int callCount() {
            return callCount;
        }


    }


}
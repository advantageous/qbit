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

package io.advantageous.qbit.metrics;

import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.primitive.Arry;
import io.advantageous.boon.primitive.Int;
import io.advantageous.qbit.metrics.support.DebugRecorder;
import io.advantageous.qbit.metrics.support.DebugReplicator;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.qbit.util.Timer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.qbit.queue.QueueBuilder.queueBuilder;
import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;

/**
 * Created by rhightower on 1/28/15.
 */
public class StatServiceBundleTest extends TimedTesting {


    protected static Object context = Sys.contextToHold();
    boolean ok;
    StatServiceClientInterface statServiceClient;
    StatService statService;
    DebugRecorder recorder;
    DebugReplicator replicator;
    ServiceBundle serviceBundle;
    ServiceQueue serviceQueue;

    @Before
    public void setUp() throws Exception {
        super.setupLatch();
        recorder = new DebugRecorder();
        replicator = new DebugReplicator();
        statService = new StatServiceBuilder().setRecorder(recorder).setReplicator(replicator).build();
        QueueBuilder queueBuilder = queueBuilder()
                .setPollWait(40).setBatchSize(100_000).setLinkTransferQueue().setCheckEvery(1000);
        serviceQueue = serviceBuilder()
                .setRootAddress("/root")
                .setServiceAddress("/serviceAddress")
                .setServiceObject(statService)
                .setRequestQueueBuilder(queueBuilder)
                .setHandleCallbacks(true)
                .setInvokeDynamic(false)
                .build();
        serviceBundle = new ServiceBundleBuilder()
                .setEachServiceInItsOwnThread(true)
                .setRequestQueueBuilder(queueBuilder)
                .setResponseQueueBuilder(queueBuilder)
                .setInvokeDynamic(false)
                .buildAndStart();
        serviceBundle.addService(statService);
        serviceBundle.startReturnHandlerProcessor();
        serviceQueue.start();
        statServiceClient = serviceBundle.createLocalProxy(StatServiceClientInterface.class, "statService");
    }

    @After
    public void tearDown() throws Exception {

        serviceBundle.stop();

    }

    @Test
    public void testRecord() throws Exception {

        statServiceClient.recordCount("mystat", 1);
        serviceBundle.flush();


        triggerLatchWhen(o -> replicator.count.get() == 1);
        waitForLatch(20);

        ok = replicator.count.get() == 1 || die();

    }

    @Test
    public void testRecordAll() throws Exception {


        String[] names = Arry.array("stat1", "stat2");
        int[] counts = Int.array(1, 2);

        statServiceClient.recordAllCounts(Timer.timer().now(), names, counts);
        serviceBundle.flush();


        triggerLatchWhen(o -> replicator.count.get() == 3);
        waitForLatch(20);
        ok = replicator.count.get() == 3 || die(replicator.count);


    }


    @Test
    public void testRecord1Thousand() throws Exception {


        for (int index = 0; index < 1_000; index++) {
            statServiceClient.recordCount("mystat", 1);

        }
        serviceBundle.flush();


        triggerLatchWhen(o -> replicator.count.get() == 1000);

        for (int index = 0; index < 4; index++) {
            puts(replicator.count.get());
            Sys.sleep(100);
        }

        waitForLatch(20);
        Sys.sleep(1000);

        ok = replicator.count.get() == 1000 || die(replicator.count);

    }


    @Test
    public void testRecord4Thousand() throws Exception {
        for (int index = 0; index < 4_000; index++) {
            statServiceClient.recordCount("mystat", 1);

            if (index % 1000 == 0) {
                Sys.sleep(10);
            }

        }
        serviceBundle.flush();


        triggerLatchWhen(o -> replicator.count.get() == 4000);
        waitForLatch(20);

        Sys.sleep(1000);

        ok = replicator.count.get() == 4000 || die(replicator.count);

    }


    //@Test
    public void testRecord100Thousand() throws Exception {
        for (int index = 0; index < 100_000; index++) {
            statServiceClient.recordCount("mystat", 1);

            if (index % 10_000 == 0) {
                Sys.sleep(10);
            }

        }
        serviceBundle.flush();


        triggerLatchWhen(o -> replicator.count.get() == 100_000);
        waitForLatch(60);

        ok = replicator.count.get() == 100_000 || die(replicator.count);

    }


    //@Test
    public void testRecord16Million() throws Exception {

        Sys.sleep(200);

        final long start = System.currentTimeMillis();

        for (int index = 0; index < 16_000_000; index++) {
            statServiceClient.recordCount("mystat", 1);

            if (index % 1_000_000 == 0) {
                Sys.sleep(10);
            }
        }
        serviceBundle.flush();

        for (int index = 0; index < 20; index++) {
            Sys.sleep(1000);
            if (replicator.count.get() == 16_000_000) {
                break;
            }
            puts(replicator.count);

        }


        ok = replicator.count.get() == 16_000_000 || die(replicator.count);


        final long end = System.currentTimeMillis();


        puts(replicator.count, end - start);


    }

    //@Test
    public void testRecordServicePerf() throws Exception {

        Sys.sleep(200);

        int count = 100_000;
        runPerfTestService(count);

    }


    //@Test
    public void testRecordServicePerf2M() throws Exception {
        Sys.sleep(200);
        int count = 2_000_000;
        runPerfTestService(count);
    }


    //@Test
    public void testRecordServicePerf8M() throws Exception {
        Sys.sleep(200);
        int count = 8_000_000;
        runPerfTestService(count);
    }


    //@Test
    public void testRecordServicePerf10M() throws Exception {
        Sys.sleep(200);
        int count = 10_000_000;
        runPerfTestService(count);
    }


    //@Test
    public void testRecordServicePerf16M() throws Exception {
        Sys.sleep(200);
        int count = 16_000_000;
        runPerfTestService(count);
    }

    private void runPerfTestService(final int count) {

        statServiceClient = serviceQueue.createProxy(StatServiceClientInterface.class);
        final long start = System.currentTimeMillis();

        for (int index = 0; index < count; index++) {
            statServiceClient.recordCount("mystat", 1);

            if (index % 1_000 == 0) {
                Sys.sleep(1);
            }

        }

        statServiceClient.clientProxyFlush();


        for (int index = 0; index < 100; index++) {
            Sys.sleep(100);
            if (replicator.count.get() == count) {
                break;
            }
            puts(replicator.count);

        }


        ok = replicator.count.get() == count || die(replicator.count);


        final long end = System.currentTimeMillis();


        puts(replicator.count, end - start);

    }

    //@Test
    public void testRecord16MillionNoBundle() throws Exception {

        Sys.sleep(200);

        final long start = System.currentTimeMillis();

        for (int index = 0; index < 16_000_000; index++) {
            statService.recordCount("mystat", 1);

        }
        for (int index = 0; index < 10; index++) {
            Sys.sleep(100);
            puts(replicator.count);

        }
        ok = replicator.count.get() == 16_000_000 || die(replicator.count);


        final long end = System.currentTimeMillis();


        puts(replicator.count, end - start);


    }


    //@Test
    public void testRecord16MillionNoBundleReflection() throws Exception {

        Sys.sleep(200);

        final long start = System.currentTimeMillis();

        final ClassMeta<StatService> statServiceClassMeta = ClassMeta.classMeta(StatService.class);
        final MethodAccess record = statServiceClassMeta.method("recordCount");


        for (int index = 0; index < 16_000_000; index++) {
            record.invoke(statService, "mystat", 1);
        }
        for (int index = 0; index < 10; index++) {
            Sys.sleep(100);
            puts(replicator.count);

        }
        ok = replicator.count.get()  == 16_000_000 || die(replicator.count);


        final long end = System.currentTimeMillis();


        puts(replicator.count, end - start);


    }


}

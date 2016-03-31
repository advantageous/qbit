package io.advantageous.qbit.service;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.Named;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.TestTimer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertTrue;

public class ServiceBundleBuilderTest {

    ServiceBundleBuilder serviceBundleBuilder;
    TestTimer timer;

    @Before
    public void setUp() throws Exception {
        serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder();
        timer = new TestTimer();
        timer.setTime();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSetStatsCollector() throws Exception {

        Map<String, Long> stats = new ConcurrentHashMap<>();

        serviceBundleBuilder.getRequestQueueBuilder()
                .setBatchSize(6)
                .setEnqueueTimeout(1)
                .setEnqueueTimeoutTimeUnit(TimeUnit.SECONDS);

        final ServiceBundle serviceBundle = serviceBundleBuilder
                .setStatsCollector(new StatsCollector() {
                    @Override
                    public void recordCount(String name, long count) {
                        puts("recordCount", name, count);
                        stats.put(name, count);
                    }

                    @Override
                    public void recordLevel(String name, long level) {
                        puts("recordLevel", name, level);
                        stats.put(name, level);

                    }

                    @Override
                    public void recordTiming(String name, long duration) {
                        puts("recordTiming", name, duration);
                        stats.put(name, duration);

                    }
                }).setTimer(timer)
                .build();

        serviceBundle.start();

        serviceBundle.addService(new MyService());
        final IMyService proxy = serviceBundle.createLocalProxy(IMyService.class, "A");

        for (int index = 0; index < 1_000; index++) {
            proxy.foo();
            timer.seconds(6);

            if (index % 100 == 0)
                ServiceProxyUtils.flushServiceProxy(proxy);
        }


        ServiceProxyUtils.flushServiceProxy(proxy);

        Sys.sleep(1_000);


        assertTrue(stats.containsKey("A.queueRequestSize"));

        assertTrue(stats.containsKey("A.startBatchCount"));


        for (int index = 0; index < 1_000; index++) {
            proxy.foo();
            timer.seconds(6);

            if (index % 100 == 0)
                ServiceProxyUtils.flushServiceProxy(proxy);
        }


        Sys.sleep(1_000);

    }

    interface IMyService {
        void foo();
    }

    @Named("A")
    static class MyService {

        int counter;

        public void foo() {

            counter++;

            if (counter % 1000 == 0) {
                Sys.sleep(20);
            }

        }
    }
}
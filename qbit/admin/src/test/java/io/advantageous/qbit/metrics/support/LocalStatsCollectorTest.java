package io.advantageous.qbit.metrics.support;

import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.util.Timer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;

public class LocalStatsCollectorTest {

    LocalStatsCollector collector;

    @Before
    public void setUp() throws Exception {


        collector = new LocalStatsCollector(10_000, Timer.timer());

    }


    @Test
    public void testCountsCollect() throws Exception {
        collector.replicateCount("foo.bar.count1", 2, -1);
        collector.replicateCount("foo.bar.count1", 3, -1);
        collector.replicateCount("foo.bar.count1", 4, -1);

        final Map<String, Map<String, ?>> collectMap = collector.collect();

        final Map<String, ?> metricsC = collectMap.get("MetricsC");


        assertEquals(9L, metricsC.get("foo.bar.count1"));
    }


    @Test
    public void testLevelCollect() throws Exception {
        collector.replicateLevel("foo.bar.level1", 2, -1);
        collector.replicateLevel("foo.bar.level1", 3, -1);
        collector.replicateLevel("foo.bar.level1", 4, -1);

        final Map<String, Map<String, ?>> collectMap = collector.collect();

        final Map<String, ?> metricsC = collectMap.get("MetricsKV");


        assertEquals(4L, metricsC.get("foo.bar.level1"));
    }


    @Test
    public void testLevelCollectAsync() throws Exception {

        LocalStatsCollectorAsync collectorAsync = LocalStatsCollectorBuilder.localStatsCollectorBuilder().buildAndStart();
        collectorAsync.replicateLevel("foo.bar.level1", 2, -1);
        collectorAsync.replicateLevel("foo.bar.level1", 3, -1);
        collectorAsync.replicateLevel("foo.bar.level1", 4, -1);

        AtomicReference<Map<String, Map<String, ?>>> referenceMap = new AtomicReference<>();


        final CountDownLatch latch = new CountDownLatch(1);

        collectorAsync.collect(collectMap -> {

            referenceMap.set(collectMap);
            latch.countDown();
        });


        ServiceProxyUtils.flushServiceProxy(collectorAsync);

        latch.await();
        final Map<String, ?> metricsC = referenceMap.get().get("MetricsKV");


        assertEquals(4L, metricsC.get("foo.bar.level1"));
    }


    @Test
    public void testLevelCollectAll() throws Exception {

        for (int i = 0; i < 1000; i++) {
            collector.replicateLevel("foo.bar.level1", i, -1);
        }

        final Map<String, Map<String, ?>> collectMap = collector.collect();

        final Map<String, ?> metricsC = collectMap.get("MetricsKV");


        assertEquals(999L, metricsC.get("foo.bar.level1"));

        puts(collectMap);
    }


    @Test
    public void testTiming() throws Exception {
        collector.replicateTiming("foo.bar.timing1", 2, -1);
        collector.replicateTiming("foo.bar.timing1", 3, -1);
        collector.replicateTiming("foo.bar.timing1", 4, -1);


        final Map<String, Map<String, ?>> collectMap = collector.collect();

        final Map<String, ?> metricsC = collectMap.get("MetricsMS");


        assertEquals(Lists.list(2L, 3L, 4L), metricsC.get("foo.bar.timing1"));
    }


    @Test
    public void testTimingNoAvg() throws Exception {
        collector.replicateTiming("foo.bar.timing1", 2, -1);
        collector.replicateTiming("foo.bar.timing1", 3, -1);
        collector.replicateTiming("foo.bar.timing1", 4, -1);

        collector.queueProcess();


        final Map<String, Map<String, ?>> collectMap = collector.collect();

        final Map<String, ?> metricsC = collectMap.get("MetricsMS");


        assertEquals(Lists.list(2L, 3L, 4L), metricsC.get("foo.bar.timing1"));
    }


    @Test
    public void testLevelTimingOver100Avg() throws Exception {


        for (int i = 0; i < 200; i++) {

            collector.replicateTiming("foo.bar.timing1", i, -1);
        }
        collector.queueProcess();


        final Map<String, Map<String, ?>> collectMap = collector.collect();

        final Map<String, ?> metricsC = collectMap.get("MetricsMS");

        List list = (List) metricsC.get("foo.bar.timing1");

        assertEquals(1, list.size());


    }


}
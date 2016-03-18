package io.advantageous.qbit.service.stats;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

public class StatsCollectorBufferTest {

    @Test
    public void testCount() {
        final AtomicLong countHolder = new AtomicLong();
        StatsCollector collector = new StatsCollector() {
            @Override
            public void recordCount(String name, long count) {
                countHolder.addAndGet(count);
            }
        };
        StatsCollectorBuffer buffer = new StatsCollectorBuffer(collector);

        buffer.recordCount("Rick", 2);
        buffer.recordCount("Rick", 2);
        buffer.recordCount("Rick", 2);

        assertEquals(0, countHolder.get());

        buffer.clientProxyFlush();

        assertEquals(6, countHolder.get());

    }


    @Test
    public void testLevel() {
        final AtomicLong holder = new AtomicLong();
        StatsCollector collector = new StatsCollector() {
            @Override
            public void recordLevel(String name, long level) {
                holder.addAndGet(level);
            }
        };
        StatsCollectorBuffer buffer = new StatsCollectorBuffer(collector);

        buffer.recordLevel("Rick", 1);
        buffer.recordLevel("Rick", 2);
        buffer.recordLevel("Rick", 6);

        assertEquals(0, holder.get());

        buffer.clientProxyFlush();

        assertEquals(6, holder.get());

    }


    @Test
    public void testTiming() {
        final AtomicLong holder = new AtomicLong();
        StatsCollector collector = new StatsCollector() {
            @Override
            public void recordTiming(String name, long timing) {
                holder.addAndGet(timing);
            }
        };
        StatsCollectorBuffer buffer = new StatsCollectorBuffer(collector);

        buffer.recordTiming("Rick", 6);
        buffer.recordTiming("Rick", 2);
        buffer.recordTiming("Rick", 1);

        assertEquals(0, holder.get());

        buffer.sendStats();

        assertEquals(6, holder.get());

    }
}
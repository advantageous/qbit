package io.advantageous.qbit.admin.jobs;

import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.stats.StatsCollector;

import java.lang.management.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class JavaStatsCollectorJob extends AdminJobBase {

    private static class GCInfo {
        private final String gcName;
        private final String statName;
        private long lastCollectionCount;
        private long lastCollectionTime;
        private long currentCollectionCount;
        private long currentCollectionTime;

        private GCInfo(final String gcName,
                       final long collectionCount,
                       final long collectionTime) {

            lastCollectionCount = collectionCount;
            lastCollectionTime = collectionTime;
            String name = gcName.replace(" ", ".").toLowerCase();
            statName = "jvm.gc.collector." + name;
            this.gcName = gcName;
        }

        public GCInfo setCurrentCollectionCount(long currentCollectionCount) {
            this.lastCollectionCount = this.currentCollectionCount;
            this.currentCollectionCount = currentCollectionCount;
            return this;
        }

        public GCInfo setCurrentCollectionTime(long currentCollectionTime) {
            this.lastCollectionTime = this.currentCollectionTime;
            this.currentCollectionTime = currentCollectionTime;
            return this;
        }

        public long getCollectionTime() {
            return currentCollectionTime - lastCollectionTime;
        }


        public long getCollectionCount() {
            return currentCollectionCount - lastCollectionCount;
        }

        public String getStatName() {
            return statName;
        }
    }

    static class JavaStatsCollectorRunnable implements Runnable {


        private final StatsCollector statsCollector;
        private final MemoryMXBean memoryMXBean;
        private final OperatingSystemMXBean operatingSystemMXBean;
        private final ThreadMXBean threadMXBean;
        private final RuntimeMXBean runtimeMXBean;
        private final String prefix;
        private final List<GarbageCollectorMXBean> garbageCollectorMXBeans;

        private final Map<String, GCInfo> gcInfoMap;

        JavaStatsCollectorRunnable(
                final String prefix,
                final StatsCollector statsCollector) {

            this.prefix = prefix;
            memoryMXBean = ManagementFactory.getMemoryMXBean();
            operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            threadMXBean = ManagementFactory.getThreadMXBean();
            runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            this.statsCollector = statsCollector;
            gcInfoMap = new HashMap<>();
        }

        @Override
        public void run() {
            statsCollector.recordLevel(prefix + ".jvm.os.load.level",
                    (int) operatingSystemMXBean.getSystemLoadAverage() * 100);

            statsCollector.recordLevel(prefix + ".jvm.up.time.seconds",
                    (int) (runtimeMXBean.getUptime() / 1_000));

            collectMemoryStats();
            collectThreadStats();
            collectGCStats();


            ServiceProxyUtils.flushServiceProxy(statsCollector);
        }

        private void collectGCStats() {

            garbageCollectorMXBeans.forEach(garbageCollectorMXBean -> {
                if (!garbageCollectorMXBean.isValid()) {
                    return;
                }

                collectGC(garbageCollectorMXBean);


            });

        }

        private void collectGC(GarbageCollectorMXBean garbageCollectorMXBean) {
            final String name = garbageCollectorMXBean.getName();

            GCInfo gcInfo = gcInfoMap.get(name);
            if (gcInfo == null) {
                gcInfo = new GCInfo(name, garbageCollectorMXBean.getCollectionCount(),
                        garbageCollectorMXBean.getCollectionTime());
                gcInfoMap.put(name, gcInfo);
                return;
            }


            statsCollector.recordCount(prefix + gcInfo.getStatName() + "collection.count",
                    (int) gcInfo.getCollectionCount());


            statsCollector.recordTiming(prefix + gcInfo.getStatName() + "collection.time",
                    (int) gcInfo.getCollectionTime());
        }

        private void collectThreadStats() {
            statsCollector.recordLevel(prefix + ".jvm.thread.count", threadMXBean.getThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.thread.peak.count", threadMXBean.getPeakThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.thread.daemon.count", threadMXBean.getDaemonThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.thread.started.count",
                    threadMXBean.getTotalStartedThreadCount() > Integer.MAX_VALUE ? Integer.MAX_VALUE
                            : (int) threadMXBean.getTotalStartedThreadCount());
        }

        private void collectMemoryStats() {
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.max.mb",
                    (int) (memoryMXBean.getHeapMemoryUsage().getMax() / 1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.used.mb",
                    (int) (memoryMXBean.getHeapMemoryUsage().getUsed() / 1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.non.heap.max.mb",
                    (int) (memoryMXBean.getNonHeapMemoryUsage().getMax() / 1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.non.heap.used.mb",
                    (int) (memoryMXBean.getNonHeapMemoryUsage().getUsed() / 1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.free.mb",
                    (int) (Runtime.getRuntime().freeMemory() / 1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.total.mb", (int) (
                    Runtime.getRuntime().totalMemory() / 1_000_000));
        }
    }

    public JavaStatsCollectorJob(final int every,
                                 final TimeUnit timeUnit,
                                 final StatsCollector statsCollector,
                                 final String prefix) {

        super(every, timeUnit, new JavaStatsCollectorRunnable(prefix, statsCollector));
    }
}

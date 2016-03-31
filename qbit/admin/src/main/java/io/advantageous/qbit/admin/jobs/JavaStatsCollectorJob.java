package io.advantageous.qbit.admin.jobs;

import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.stats.StatsCollector;

import java.lang.management.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JavaStatsCollectorJob extends AdminJobBase {

    public JavaStatsCollectorJob(final int every,
                                 final TimeUnit timeUnit,
                                 final StatsCollector statsCollector,
                                 final String prefix) {

        super(every, timeUnit, new JavaStatsCollectorRunnable(prefix, statsCollector));
    }

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
            statName = ".jvm.gc.collector." + name;
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

        @Override
        public String toString() {
            return "GCInfo{" +
                    "gcName='" + gcName + '\'' +
                    ", statName='" + statName + '\'' +
                    ", lastCollectionCount=" + lastCollectionCount +
                    ", lastCollectionTime=" + lastCollectionTime +
                    ", currentCollectionCount=" + currentCollectionCount +
                    ", currentCollectionTime=" + currentCollectionTime +
                    '}';
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
                    (long) operatingSystemMXBean.getSystemLoadAverage());

            statsCollector.recordLevel(prefix + ".jvm.up.time.seconds",
                    (runtimeMXBean.getUptime() / 1_000));

            statsCollector.recordLevel(prefix + ".jvm.up.time.minutes",
                    (runtimeMXBean.getUptime() / (1_000 * 60)));

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

            gcInfo.setCurrentCollectionCount(garbageCollectorMXBean.getCollectionCount());
            gcInfo.setCurrentCollectionTime(garbageCollectorMXBean.getCollectionTime());

            final long collectionCount = gcInfo.getCollectionCount();

            final long collectionTime = gcInfo.getCollectionTime();

            if (collectionCount > 0) {
                statsCollector.recordCount(prefix + gcInfo.getStatName() + "collection.count",
                        collectionCount);
            }

            if (collectionTime > 0) {
                statsCollector.recordTiming(prefix + gcInfo.getStatName() + "collection.time",
                        collectionTime);
            }
        }

        private void collectThreadStats() {
            statsCollector.recordLevel(prefix + ".jvm.thread.count", threadMXBean.getThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.thread.peak.count", threadMXBean.getPeakThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.thread.daemon.count", threadMXBean.getDaemonThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.thread.started.count", threadMXBean.getTotalStartedThreadCount());
        }

        private void collectMemoryStats() {
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.max",
                    (memoryMXBean.getHeapMemoryUsage().getMax()));
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.used",
                    (memoryMXBean.getHeapMemoryUsage().getUsed()));
            statsCollector.recordLevel(prefix + ".jvm.mem.non.heap.max",
                    (memoryMXBean.getNonHeapMemoryUsage().getMax()));
            statsCollector.recordLevel(prefix + ".jvm.mem.non.heap.used",
                    (memoryMXBean.getNonHeapMemoryUsage().getUsed()));
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.free",
                    (Runtime.getRuntime().freeMemory()));
            statsCollector.recordLevel(prefix + ".jvm.mem.total", (int) (
                    Runtime.getRuntime().totalMemory()));
        }
    }
}

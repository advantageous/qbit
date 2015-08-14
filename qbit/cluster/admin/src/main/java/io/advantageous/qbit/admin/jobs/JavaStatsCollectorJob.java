package io.advantageous.qbit.admin.jobs;

import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.stats.StatsCollector;

import java.lang.management.*;
import java.util.concurrent.TimeUnit;

public class JavaStatsCollectorJob extends AdminJobBase {

    static class JavaStatsCollectorRunnable implements Runnable {


        private final StatsCollector statsCollector;
        private final MemoryMXBean memoryMXBean;
        private final OperatingSystemMXBean operatingSystemMXBean;
        private final ThreadMXBean threadMXBean;
        private final RuntimeMXBean runtimeMXBean;
        private final String prefix;
        //private final List<GarbageCollectorMXBean> garbageCollectorMXBeans;

        JavaStatsCollectorRunnable(
                final String prefix,
                final StatsCollector statsCollector) {

            this.prefix = prefix;
            memoryMXBean = ManagementFactory.getMemoryMXBean();
            operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            threadMXBean = ManagementFactory.getThreadMXBean();
            runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            //garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            this.statsCollector = statsCollector;
        }

        @Override
        public void run() {
            statsCollector.recordLevel(prefix + ".jvm.os.load.level",
                    (int) operatingSystemMXBean.getSystemLoadAverage() * 100);
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.max.mb",
                    (int) (memoryMXBean.getHeapMemoryUsage().getMax()/1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.used.mb",
                    (int) (memoryMXBean.getHeapMemoryUsage().getUsed()/1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.non.heap.max.mb",
                    (int) (memoryMXBean.getNonHeapMemoryUsage().getMax()/1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.non.heap.used.mb",
                    (int) (memoryMXBean.getNonHeapMemoryUsage().getUsed()/1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.thread.count", threadMXBean.getThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.peak.thread.count", threadMXBean.getPeakThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.daemon.thread.count", threadMXBean.getDaemonThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.started.thread.count",
                    threadMXBean.getTotalStartedThreadCount() > Integer.MAX_VALUE ? Integer.MAX_VALUE
                            : (int) threadMXBean.getTotalStartedThreadCount());
            statsCollector.recordLevel(prefix + ".jvm.up.time.seconds",
                    (int) (runtimeMXBean.getUptime()/1_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.heap.free.mb",
                    (int) (Runtime.getRuntime().freeMemory()/1_000_000));
            statsCollector.recordLevel(prefix + ".jvm.mem.total.mb", (int) (
                    Runtime.getRuntime().totalMemory()/1_000_000));


//            garbageCollectorMXBeans.forEach(new Consumer<GarbageCollectorMXBean>() {
//                @Override
//                public void accept(final GarbageCollectorMXBean garbageCollectorMXBean) {
//                    if (!garbageCollectorMXBean.isValid()) {
//                        return;
//                    }
//                    /* This needs more thought. */
//                    //final String name = garbageCollectorMXBean.getName().replace(" ", ".").toLowerCase();
//                    //final String statName = "jvm.gc.collector." + name;
//                    //garbageCollectorMXBean.getCollectionCount();
//                }
//            });



            ServiceProxyUtils.flushServiceProxy(statsCollector);
        }
    }

    public JavaStatsCollectorJob(final int every,
                                 final TimeUnit timeUnit,
                                 final StatsCollector statsCollector,
                                 final String prefix) {

        super(every, timeUnit, new JavaStatsCollectorRunnable(prefix, statsCollector));
    }
}

package io.advantageous.qbit.metrics.support;

import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.stats.StatList;
import io.advantageous.qbit.util.Timer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LocalStatsCollector implements StatReplicator, QueueCallBackHandler {


    private final Reactor reactor;
    private final ConcurrentHashMap<String, Metric> statsMap = new ConcurrentHashMap<>();


    public LocalStatsCollector(final int seconds, final Timer timer) {
        reactor = ReactorBuilder.reactorBuilder().setTimer(timer).build();
        reactor.addRepeatingTask(seconds, TimeUnit.SECONDS, this::packStat);
    }

    @Override
    public void replicateCount(String name, long count, long time) {

        Metric metric = statsMap.get(name);
        if (metric == null) {
            metric = Metric.count(name);
            statsMap.put(name, metric);

        }

        metric.value += count;

    }

    @Override
    public void replicateLevel(String name, long level, long time) {


        Metric metric = statsMap.get(name);
        if (metric == null) {
            metric = Metric.level(name);
            statsMap.put(name, metric);

        }

        metric.value = level;
        metric.stats.add(level);

    }

    @Override
    public void replicateTiming(String name, long timing, long time) {

        Metric metric = statsMap.get(name);
        if (metric == null) {
            metric = Metric.timing(name);
            statsMap.put(name, metric);
        }

        metric.stats.add(timing);
        metric.value = timing;


    }

    @Override
    public void queueProcess() {

        reactor.process();


    }

    private void packStat() {

        final Set<Map.Entry<String, Metric>> entries = statsMap.entrySet();

        entries.stream().forEach(
                entry -> packMetric(entry.getValue()));
    }

    private void packMetric(final Metric metric) {
        switch (metric.type) {
            case COUNT:
                break;
            case LEVEL:
                break;
            case TIMING:
                metric.value = (int) metric.stats.mean();
                break;


        }

        if (metric.stats.size() > 100) {
            metric.stats.clear();
        }

    }

    public Map<String, Map<String, ?>> collect() {

        final Map<String, Map<String, ?>> metricMap = new HashMap<>();
        final Map<String, List<Number>> metricsTimingMap = new HashMap<>();
        final Map<String, Number> metricsCountMap = new HashMap<>();
        final Map<String, Number> metricsLevelMap = new HashMap<>();

        Map map = metricMap;
        map.put("version", 1);

        metricMap.put("MetricsMS", metricsTimingMap);
        metricMap.put("MetricsC", metricsCountMap);
        metricMap.put("MetricsKV", metricsLevelMap);


        final Set<Map.Entry<String, Metric>> entries = statsMap.entrySet();

        entries.stream().filter(entry -> entry.getValue().type == MetricType.TIMING && entry.getValue().value != 0)
                .forEach(entry -> collectTiming(entry.getValue(), metricsTimingMap));


        entries.stream().filter(entry -> entry.getValue().type == MetricType.COUNT && entry.getValue().value != 0)
                .forEach(entry -> collectCount(entry.getValue(), metricsCountMap));


        entries.stream().filter(entry -> entry.getValue().type == MetricType.LEVEL && entry.getValue().value != 0)
                .forEach(entry -> collectLevel(entry.getValue(), metricsLevelMap));

        return metricMap;

    }

    private void collectCount(final Metric metric, final Map<String, Number> metricsCountMap) {
        metricsCountMap.put(metric.name, metric.value);
        metric.value = 0;
        metric.stats.clear();

    }

    private void collectTiming(final Metric metric, final Map<String, List<Number>> metricsTimingMap) {

        if (metric.stats.size() > 0) {
            metricsTimingMap.put(metric.name, new ArrayList<>(metric.stats));
        } else {

            metricsTimingMap.put(metric.name, Collections.singletonList(metric.value));
        }
        metric.value = 0;
        metric.stats.clear();

    }

    private void collectLevel(final Metric metric, final Map<String, Number> metricsLevelMap) {
        metricsLevelMap.put(metric.name, metric.value);

        if (metric.stats.size() > 1) {
            metricsLevelMap.put(metric.name + ".mean", metric.stats.mean());
            metricsLevelMap.put(metric.name + ".std", metric.stats.standardDeviation());
            metricsLevelMap.put(metric.name + ".median", metric.stats.median());
            metricsLevelMap.put(metric.name + ".min", metric.stats.min());
            metricsLevelMap.put(metric.name + ".max", metric.stats.max());
            metricsLevelMap.put(metric.name + ".count", metric.stats.size());
        }

    }


    enum MetricType {
        COUNT, LEVEL, TIMING
    }

    final static class Metric {

        final StatList stats;
        final String name;
        final MetricType type;
        long value;


        public Metric(String name, MetricType type) {
            this.name = name;
            this.type = type;
            this.stats = new StatList(100);
        }

        public static Metric count(String name) {

            return new Metric(name, MetricType.COUNT);

        }

        public static Metric level(String name) {

            return new Metric(name, MetricType.LEVEL);

        }

        public static Metric timing(String name) {

            return new Metric(name, MetricType.TIMING);

        }
    }

}

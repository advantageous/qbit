package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.TimeUnit;

public class ServiceStatsListener implements QueueCallBackHandler {

    private final String serviceName;
    private final long flushStatsInterval;
    private final Timer timer;
    private final int sampleEvery;
    private long now;
    private long lastFlush;
    private final StatsCollector statsCollector;


    private final String startBatchCountKey;
    private final String receiveCountKey;
    private final String receiveTimeKey;

    private int startBatchCount;
    private int receiveCount;

    private long totalCount;


    private long beforeReceiveTime;

    private boolean timeIt;


    public ServiceStatsListener(final String serviceName,
                                final StatsCollector statsCollector,
                                final Timer timer,
                                final long checkInInterval,
                                final TimeUnit timeUnit,
                                final int sampleEvery) {
        this.serviceName = serviceName;
        this.flushStatsInterval = timeUnit.toMillis(checkInInterval);
        this.timer = timer;
        this.statsCollector = statsCollector;
        lastFlush = timer.now();
        startBatchCountKey = serviceName + ".startBatchCount";
        receiveCountKey = serviceName + ".receiveCount";
        receiveTimeKey = serviceName + ".callTimeSample";

        this.sampleEvery = sampleEvery;
    }

    @Override
    public void queueStartBatch() {
        startBatchCount++;
    }


    @Override
    public void beforeReceiveCalled() {
        totalCount++;

        if (totalCount % sampleEvery == 0) {
            timeIt = true;
            beforeReceiveTime = System.nanoTime();
        }


    }

    @Override
    public void afterReceiveCalled() {
        receiveCount++;

        if (timeIt) {
            timeIt = false;

            long stopTime = System.nanoTime();
            long duration = stopTime - beforeReceiveTime;
            statsCollector.recordTiming(receiveTimeKey, (int) duration);
        }
    }


    @Override
    public void queueLimit() {
        sendStats();
    }

    @Override
    public void queueEmpty() {

        sendStats();
    }

    @Override
    public void queueInit() {


        statsCollector.recordLevel(serviceName, 1);
    }

    @Override
    public void queueIdle() {
        sendStats();
    }

    @Override
    public void queueShutdown() {

        statsCollector.recordLevel(serviceName, 1);
    }



    private void sendStats() {
        now = timer.now();

        long duration = now - lastFlush;

        if (duration > flushStatsInterval) {
            statsCollector.recordCount(startBatchCountKey, startBatchCount);
            startBatchCount = 0;


            statsCollector.recordCount(receiveCountKey, receiveCount);
            receiveCount = 0;
        }
    }


}

package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.TimeUnit;

public class ServiceStatsListener implements QueueCallBackHandler {

    private final String serviceName;
    private final long flushStatsInterval;
    private final long checkQueueSizeInterval;
    private final Timer timer;
    private final int sampleEvery;
    private final ServiceQueueSizer serviceQueueSizer;
    private long now;
    private long lastFlush;
    private long lastSizeCheck;
    private final StatsCollector statsCollector;


    private final String startBatchCountKey;

    private final String queueRequestSizeKey;
    private final String queueResponseSizeKey;
    private final String receiveCountKey;
    private final String receiveTimeKey;

    private int startBatchCount;
    private int receiveCount;

    private long sampleUntilCount;


    private long beforeReceiveTime;

    private boolean timeIt;


    /** Added these so we are only sending if they change and not all of the time. */
    private int lastRequestSize=-1;
    private int lastResponseSize=-1;



    public ServiceStatsListener(final String serviceName,
                                final StatsCollector statsCollector,
                                final Timer timer,
                                final long checkInInterval,
                                final TimeUnit timeUnit,
                                final int sampleEvery,
                                final ServiceQueueSizer serviceQueueSizer) {
        this.serviceName = serviceName;
        this.flushStatsInterval = timeUnit.toMillis(checkInInterval);
        this.checkQueueSizeInterval = flushStatsInterval * 10;
        this.timer = timer;
        this.statsCollector = statsCollector;
        lastFlush = timer.now();
        startBatchCountKey = serviceName + ".startBatchCount";
        receiveCountKey = serviceName + ".receiveCount";
        receiveTimeKey = serviceName + ".callTimeSample";
        this.queueRequestSizeKey =  serviceName + ".queueRequestSize";
        this.queueResponseSizeKey =  serviceName + ".queueResponseSize";
        this.sampleEvery = sampleEvery == 0 ? -1 : sampleEvery;
        this.serviceQueueSizer = serviceQueueSizer;
    }

    @Override
    public void queueStartBatch() {
        startBatchCount++;
    }


    @Override
    public void beforeReceiveCalled() {

        if (sampleEvery == -1) {
            return;
        }

        sampleUntilCount++;

        if (sampleUntilCount > sampleEvery ) {
            sampleUntilCount = 0;
            timeIt = true;
            beforeReceiveTime = System.nanoTime();
        }


    }

    @Override
    public void afterReceiveCalled() {


        if (sampleEvery == -1) {
            return;
        }

        receiveCount++;

        if (timeIt) {
            timeIt = false;

            long stopTime = System.nanoTime();
            long duration = stopTime - beforeReceiveTime;

            if (duration > 0) {
                statsCollector.recordTiming(receiveTimeKey, (int) duration);
            }
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

        calculateSizeIfNeeded();
        flushStatsIfNeeded();

    }


    private void calculateSizeIfNeeded() {

        if (serviceQueueSizer == null) {
            return;
        }

        long duration = now - lastSizeCheck;


        if (duration > checkQueueSizeInterval) {
            lastSizeCheck = now;

            /* Changed this so we only add if the level is different than the one we sent before. */
            final int requestSize = serviceQueueSizer.requestSize();
            final int responseSize = serviceQueueSizer.responseSize();

            if (requestSize != lastRequestSize) {
                lastRequestSize = requestSize;
                statsCollector.recordLevel(queueRequestSizeKey, requestSize);
            }

            if (responseSize != lastResponseSize) {
                lastResponseSize = responseSize;
                statsCollector.recordLevel(queueResponseSizeKey, responseSize);
            }
        }

    }

    private void flushStatsIfNeeded() {

        long duration = now - lastFlush;


        if (duration > flushStatsInterval) {
            lastFlush = now;

            /* We are only sending the count if it is not 0. */
            if (startBatchCount > 0) {
                statsCollector.recordCount(startBatchCountKey, startBatchCount);
            }
            startBatchCount = 0;


            /* We are only sending the count if it is not 0. */
            if (receiveCount > 0) {
                statsCollector.recordCount(receiveCountKey, receiveCount);
            }
            receiveCount = 0;

            statsCollector.clientProxyFlush();
        }

    }


}

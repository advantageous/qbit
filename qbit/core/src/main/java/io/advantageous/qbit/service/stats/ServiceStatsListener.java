/**
 * ****************************************************************************
 * <p>
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 * http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 * http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 * http://rick-hightower.blogspot.com/2015/01/quick-startClient-qbit-programming.html
 * http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 * http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 * <p>
 * ****************************************************************************
 */
package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.TimeUnit;

/**
 * The `ServiceStatsListener` is used to intercept queue calls for the `ServiceQueue`.
 * All services and end-points end up using the `ServiceQueue`.
 * This class is able to track stats for services.
 * <p>
 * #### Keys
 * ```java
 * <p>
 * startBatchCountKey = serviceName + ".startBatchCount";
 * receiveCountKey = serviceName + ".receiveCount";
 * receiveTimeKey = serviceName + ".callTimeSample";
 * this.queueRequestSizeKey =  serviceName + ".queueRequestSize";
 * this.queueResponseSizeKey =  serviceName + ".queueResponseSize";
 * ```
 * <p>
 * The `${serviceName}.startBatchCount` tracks how many times a batch has been sent.
 * This can tell you how well your batching is setup.
 * <p>
 * The `${serviceName}.receiveCount` is how many times the service has been called.
 * The `${serviceName}.callTimeSample` is how long do methods take for this service (if enabled, call times are sampled).
 * <p>
 * The `${serviceName}.queueRequestSize` keeps track of how large the request queue is.
 * This is an indication of calls not getting handled if greater than 0. If this continues to rise then the service could be down.
 * (Note there is a health check to see a queue is blocked, and the service will be marked unhealthy.)
 * <p>
 * The `${serviceName}.queueResponseSize` keeps track of how large the response queue is getting.
 * This is an indication that responses are not getting drained.
 */
public class ServiceStatsListener implements QueueCallBackHandler {

    private final String serviceName;
    private final long flushStatsInterval;
    private final long checkQueueSizeInterval;
    private final Timer timer;
    private final int sampleEvery;
    private final ServiceQueueSizer serviceQueueSizer;
    private final StatsCollector statsCollector;
    private final String startBatchCountKey;
    private final String queueRequestSizeKey;
    private final String queueResponseSizeKey;
    private final String receiveCountKey;
    private final String receiveTimeKey;
    private long now;
    private long lastFlush;
    private long lastSizeCheck;
    private int startBatchCount;
    private int receiveCount;

    private long sampleUntilCount;


    private long beforeReceiveTime;

    private boolean timeIt;


    /**
     * Added these so we are only sending if they change and not all of the time.
     */
    private int lastRequestSize = -1;
    private int lastResponseSize = -1;


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
        this.queueRequestSizeKey = serviceName + ".queueRequestSize";
        this.queueResponseSizeKey = serviceName + ".queueResponseSize";
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

        if (sampleUntilCount > sampleEvery) {
            sampleUntilCount = 0;
            timeIt = true;
            beforeReceiveTime = System.nanoTime();
        }


    }

    @Override
    public void afterReceiveCalled() {


        receiveCount++;

        if (sampleEvery == -1) {
            return;
        }


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

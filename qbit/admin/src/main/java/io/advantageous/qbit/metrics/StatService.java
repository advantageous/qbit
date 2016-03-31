/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.metrics;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.stats.Stats;
import io.advantageous.qbit.service.stats.StatsCollector;


/**
 * The StatService collects stats, and allows stats to be queried.
 * This collects key performance indicators: timings, counts and levels/gauges.
 * It also allow internal or external clients to query this system.
 * <p>
 * Created by rick on 6/6/15.
 */
public interface StatService extends StatsCollector {


    /**
     * Get the last n Seconds of stats (up to two minutes of stats typically kept in memory).
     * <p>
     * The `Stat` object has the mean, median, etc.
     * <p>
     * ```java
     * <p>
     * private final float mean;
     * private final float stdDev;
     * private final float variance;
     * private final long sum;
     * private final long max;
     * private final long min;
     * private final long median;
     * ```
     *
     * @param callback    callback to get Stat
     * @param name        name metric, KPI, etc.
     * @param secondCount secondCount
     */
    default void statsForLastSeconds(Callback<Stats> callback, String name, int secondCount) {
    }

    /**
     * Gets the average last n Seconds of of a level.
     *
     * @param callback    callback
     * @param name        name of metric, KPI, etc.
     * @param secondCount secondCount
     */
    default void averageLastLevel(Callback<Long> callback, String name, int secondCount) {
    }

    /**
     * Gets count of the current minute
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void currentMinuteCount(Callback<Long> callback, String name) {
    }


    /**
     * Gets count of the current second.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void currentSecondCount(Callback<Long> callback, String name) {
    }


    /**
     * Gets count of the last recorded full second.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void lastSecondCount(Callback<Long> callback, String name) {
    }


    /**
     * Gets count of the last recorded ten full seconds.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void lastTenSecondCount(Callback<Long> callback, String name) {
    }


    /**
     * Gets count of the last recorded five full seconds.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void lastFiveSecondCount(Callback<Long> callback, String name) {
    }


    /**
     * Gets count of the last recorded N full seconds.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void lastNSecondsCount(Callback<Long> callback, String name, int secondCount) {
    }


    /**
     * Gets count of the last recorded N full seconds.
     * This is more exact if the count overlaps two minutes.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void lastNSecondsCountExact(Callback<Long> callback, String name, int secondCount) {
    }


    /**
     * Gets count of the last recorded N full seconds.
     * This is more exact if the count overlaps two minutes.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void lastTenSecondCountExact(Callback<Long> callback, String name) {
    }

    /**
     * Gets count of the last recorded N full seconds.
     * This is more exact if the count overlaps two minutes.
     *
     * @param callback callback
     * @param name     name of metric
     */
    default void lastFiveSecondCountExact(Callback<Long> callback, String name) {
    }

    /**
     * Bulk record.
     *
     * @param name      name of metric
     * @param count     count
     * @param timestamp timestamp
     */
    default void recordWithTime(String name, int count, long timestamp) {
    }


    /**
     * Bulk record.
     *
     * @param names     names of metric
     * @param counts    counts of metrics
     * @param timestamp timestamp
     */
    default void recordAll(long timestamp, String[] names, long[] counts) {
    }


    /**
     * Bulk record.
     *
     * @param names  names of metric
     * @param counts counts of metrics
     * @param times  times
     */
    default void recordAllWithTimes(String[] names, long[] counts, long[] times) {
    }
}

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

import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.stats.StatsCollector;

/**
 * StatService
 * created by rhightower on 1/28/15.
 */
public interface StatService extends ClientProxy, StatsCollector {
    default void recordCount(String name, long count) {
    }

    default void increment(String name) {
        recordCount(name, 1);
    }

    default void recordLevel(String name, long level) {
    }

    default void recordTiming(String name, long duration) {
    }

    default void statsForLastSeconds(Callback<Stats> callback, String name, int secondCount) {
    }

    default void averageLastLevel(Callback<Long> callback, String name, int secondCount) {
    }

    default void currentMinuteCount(Callback<Long> callback, String name) {
    }

    default void currentSecondCount(Callback<Long> callback, String name) {
    }

    default void lastSecondCount(Callback<Long> callback, String name) {
    }


    default void lastTenSecondCount(Callback<Long> callback, String name) {
    }

    default void lastFiveSecondCount(Callback<Long> callback, String name) {
    }

    default void lastNSecondsCount(Callback<Long> callback, String name, int secondCount) {
    }

    default void lastNSecondsCountExact(Callback<Long> callback, String name, int secondCount) {
    }

    default void lastTenSecondCountExact(Callback<Long> callback, String name) {
    }

    default void lastFiveSecondCountExact(Callback<Long> callback, String name) {
    }

    default void recordWithTime(String name, int count, long timestamp) {
    }

    default void recordAll(long timestamp, String[] names, long[] counts) {
    }

    default void recordAllWithTimes(String[] names, long[] counts, long[] times) {
    }
}

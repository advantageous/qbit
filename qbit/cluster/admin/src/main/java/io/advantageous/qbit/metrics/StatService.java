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

/**
 * StatService
 * Created by rhightower on 1/28/15.
 */

public interface StatService extends ClientProxy {
    default void recordCount(String name, int count) {}


    default void increment(String name) {
        recordCount(name, 1);
    }


    default void incrementAll(String... names) {
        for (String name : names) {
            increment(name);
        }
    }

    default void currentMinuteCount(Callback<Integer> callback, String name) {}

    default void currentSecondCount(Callback<Integer> callback, String name) {}

    default void lastSecondCount(Callback<Integer> callback, String name) {}


    default void lastTenSecondCount(Callback<Integer> callback, String name){}

    default void lastFiveSecondCount(Callback<Integer> callback, String name){}

    default void lastNSecondsCount(Callback<Integer> callback, String name, int secondCount){}


    default void lastNSecondsCountExact(Callback<Integer> callback, String name, int secondCount){}


    default void lastTenSecondCountExact(Callback<Integer> callback, String name){}

    default void lastFiveSecondCountExact(Callback<Integer> callback, String name){}



    default void recordCountWithTime(String name, int count, long timestamp){}

    default void recordAllCounts(long timestamp, String[] names, int[] counts){}

    default void recordAllCountsWithTimes(String[] names, int[] counts, long[] times){}
}

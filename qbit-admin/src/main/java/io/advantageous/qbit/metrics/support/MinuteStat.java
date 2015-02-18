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

package io.advantageous.qbit.metrics.support;

import java.util.Arrays;

/**
 * Created by rhightower on 1/28/15.
 */
public class MinuteStat {

    private final String name;
    private final long startTime;
    private final int[] secondCounts;
    private long endTime;
    private int totalCount;

    public MinuteStat(long now, String name) {
        startTime = now;

        secondCounts = new int[60];
        this.name = name;

    }


    public int countLastSecond(long now) {
        int secondIndex = secondIndex(now);


        if (secondIndex - 1 >= secondCounts.length) {
            return Integer.MIN_VALUE;
        }

        return secondCounts[secondIndex - 1];
    }


    public int countThisSecond(long now) {
        int secondIndex = secondIndex(now);


        if (secondIndex >= secondCounts.length) {
            return Integer.MIN_VALUE;
        }

        return secondCounts[secondIndex];
    }

    public int changeBy(int count, long now) {
        totalCount += count;

        int secondIndex = secondIndex(now);


        if (secondIndex >= secondCounts.length) {
            return -1;
        }

        secondCounts[secondIndex] += count;

        endTime = now;
        return totalCount;
    }

    private int secondIndex(long now) {

        if (now >= startTime && now < (startTime + 60 * 1000)) {
            return (int) ((now - startTime) / 1000);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int[] getSecondCounts() {
        return secondCounts;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "MinuteMeasurement{" +
                "name='" + getName() + '\'' +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", secondCounts=" + Arrays.toString(getSecondCounts()) +
                ", totalCount=" + getTotalCount() +
                '}';
    }

    public int getTotalCount() {
        return totalCount;
    }
}

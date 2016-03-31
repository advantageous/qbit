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

import io.advantageous.qbit.service.stats.StatList;
import io.advantageous.qbit.service.stats.Stats;

import java.util.Arrays;

/**
 * MinuteStat
 * created by rhightower on 1/28/15.
 */
public class MinuteStat {

    private final String name;
    private final long startTime;
    private final long[] secondCounts;
    private long endTime;
    private int totalCount;

    public MinuteStat(long now, String name) {
        startTime = now;

        secondCounts = new long[60];

        for (int index = 0; index < 60; index++) {
            secondCounts[index] = -1;
        }
        this.name = name;

    }


    public long countLastSecond(long now) {
        int secondIndex = secondIndex(now);


        if (secondIndex - 1 >= secondCounts.length) {
            return 0;
        }

        return secondCounts[secondIndex - 1];
    }


    public long countLastTenSeconds(long now) {
        return countLastSeconds(now, 10);
    }


    public long countLastSeconds(long now, int secondCount) {

        long sum = 0;

        int secondIndex = secondIndex(now);

        int index = 0;


        while (secondIndex >= 0) {


            if (secondIndex >= 60) {

                now = now - 1000;
                secondIndex = secondIndex(now);
                index++;
                if (index >= secondCount) {
                    break;
                }
                continue;
            }
            index++;
            long value = secondCounts[secondIndex];

            if (value != -1) {
                sum += value;
            }
            if (index >= secondCount) {
                break;
            }
            secondIndex--;
        }
        return sum;
    }


    public Stats statsForLastSeconds(long now, int secondCount) {

        StatList list = new StatList(60);

        int secondIndex = secondIndex(now);

        int index = 0;


        while (secondIndex >= 0) {


            if (secondIndex >= 60) {

                now = now - 1000;
                secondIndex = secondIndex(now);
                index++;
                if (index >= secondCount) {
                    break;
                }
                continue;
            }
            index++;


            long value = secondCounts[secondIndex];

            if (value != -1) {
                list.add(value);
            }

            if (index >= secondCount) {
                break;
            }
            secondIndex--;
        }
        return new Stats(list);

    }

    public long averageLastLevel(long now, int secondCount) {

        long sum = 0;

        int secondIndex = secondIndex(now);

        int index = 0;

        int readingCount = 0;


        while (secondIndex >= 0) {


            if (secondIndex >= 60) {

                now = now - 1000;
                secondIndex = secondIndex(now);
                index++;
                if (index >= secondCount) {
                    break;
                }
                continue;
            }
            index++;


            long value = secondCounts[secondIndex];
            if (value != -1) {
                sum += value;
                readingCount++;
            }
            if (index >= secondCount) {
                break;
            }
            secondIndex--;
        }
        return (readingCount != 0) ? sum / readingCount : -1;
    }

    public long countLastFiveSeconds(long now) {

        return countLastSeconds(now, 5);
    }

    public long countThisSecond(long now) {
        int secondIndex = secondIndex(now);


        if (secondIndex >= secondCounts.length) {
            return Integer.MIN_VALUE;
        }

        return secondCounts[secondIndex];
    }

    @SuppressWarnings("UnusedReturnValue")
    public long changeBy(long count, long now) {
        totalCount += count;

        int secondIndex = secondIndex(now);


        if (secondIndex >= secondCounts.length) {
            return 0;
        }

        long value = secondCounts[secondIndex];

        if (value == -1) {
            value = 0;
        }

        value += count;

        secondCounts[secondIndex] = value;

        endTime = now;
        return totalCount;
    }

    private int secondIndex(long now) {

        if (now >= startTime && now < (startTime + 60 * 1000)) {
            return (int) ((now - startTime) / 1000);
        } else {
            return 60;
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long[] getSecondCounts() {
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

    public long getTotalCount() {
        return totalCount;
    }

    public void recordLevel(long level, long now) {
        int secondIndex = secondIndex(now);


        if (secondIndex >= secondCounts.length) {
            return;
        }

        secondCounts[secondIndex] = level;

        endTime = now;

    }
}

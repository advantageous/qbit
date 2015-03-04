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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;

public class MinuteMeasurementTest {

    MinuteStat measurement;
    long time = System.currentTimeMillis();
    boolean ok;

    @Before
    public void setUp() throws Exception {

        measurement = new MinuteStat(time, "one");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test() throws Exception {


        measurement.changeBy(1, time);
        measurement.changeBy(2, time + 1000);

        measurement.changeBy(3, time + 5000);
        measurement.changeBy(4, time + 40000);

        measurement.changeBy(5, time + 50000);

        measurement.changeBy(6, time + 59000);

        int status = measurement.changeBy(7, time + 61000);

        final int[] secondCounts = measurement.getSecondCounts();

        ok = measurement.getTotalCount() == 28 || die();
        ok = secondCounts[0] == 1 || die(secondCounts[0]);
        ok = secondCounts[1] == 2 || die();
        ok = secondCounts[5] == 3 || die();
        ok = secondCounts[40] == 4 || die();
        ok = secondCounts[50] == 5 || die();
        ok = secondCounts[59] == 6 || die();

        ok = status == -1 || die();

        puts(measurement);


        final int countLastSecond = measurement.countLastSecond(time + 6000);

        ok = countLastSecond == 3 || die(countLastSecond);


        final int countThisSecond = measurement.countThisSecond(time + 5000);

        ok = countThisSecond == 3 || die(countLastSecond);


        final int crap = measurement.countThisSecond(Long.MAX_VALUE);

        ok = crap == Integer.MIN_VALUE || die(crap);


        final int crap2 = measurement.countLastSecond(Long.MAX_VALUE);

        ok = crap2 == Integer.MIN_VALUE || die(crap2);

    }

}
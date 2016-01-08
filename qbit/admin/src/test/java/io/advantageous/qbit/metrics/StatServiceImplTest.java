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

import io.advantageous.boon.primitive.Arry;
import io.advantageous.boon.primitive.Lng;
import io.advantageous.qbit.metrics.support.DebugRecorder;
import io.advantageous.qbit.metrics.support.DebugReplicator;
import io.advantageous.qbit.metrics.support.NoOpReplicator;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.util.Timer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.core.Exceptions.die;
import static junit.framework.Assert.assertEquals;


public class StatServiceImplTest {


    boolean ok;
    StatServiceImpl statServiceImpl;
    DebugRecorder recorder;
    DebugReplicator replicator;

    TestTimer testTimer = new TestTimer();

    @Before
    public void setUp() throws Exception {

        recorder = new DebugRecorder();
        replicator = new DebugReplicator();
        testTimer.setTime();
        statServiceImpl = new StatServiceBuilder().setRecorder(recorder)
                .setReplicator(replicator)
                .setTimer(testTimer)
                .build();

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void lastFiveSecondCountTest() throws Exception {

        for (int index = 0; index < 20; index++) {
            statServiceImpl.recordCount("mystat", 1);
            statServiceImpl.tick();
            testTimer.seconds(1);
        }

        testTimer.seconds(-2);
        statServiceImpl.tick();


        long count = statServiceImpl.lastFiveSecondCount("mystat");

        assertEquals(5, count);

    }


    @Test
    public void lastTenSecondCountTest() throws Exception {

        for (int index = 0; index < 20; index++) {
            statServiceImpl.recordCount("mystat", 1);
            statServiceImpl.tick();
            testTimer.seconds(1);
        }

        testTimer.seconds(-2);
        statServiceImpl.tick();


        long count = statServiceImpl.lastTenSecondCount("mystat");

        assertEquals(10, count);

    }


    @Test
    public void lastSevenSeconds() throws Exception {

        for (int index = 0; index < 20; index++) {
            statServiceImpl.recordCount("mystat", 1);
            statServiceImpl.process();
            testTimer.seconds(1);
        }

        testTimer.seconds(-2);
        statServiceImpl.tick();


        long count = statServiceImpl.lastNSecondsCount("mystat", 7);

        assertEquals(7, count);

    }


    @Test
    public void lastSecondsNotExact() throws Exception {

        for (int index = 0; index < 70; index++) {
            statServiceImpl.recordCount("mystat", 1);
            statServiceImpl.tick();
            testTimer.seconds(1);
        }


        long count = statServiceImpl.lastNSecondsCount("mystat", 20);

        assertEquals(10, count);

    }


    @Test
    public void lastMinute() throws Exception {

        for (int index = 0; index < 60; index++) {
            testTimer.seconds(1);
            statServiceImpl.recordCount("mystat", 1);
            statServiceImpl.process();
        }


        long count = statServiceImpl.currentMinuteCount("mystat");

        assertEquals(60, count);

    }

    @Test
    public void lastSecondsExact() throws Exception {

        for (int index = 0; index < 140; index++) {
            testTimer.ms(500);
            statServiceImpl.tick();
            statServiceImpl.recordCount("mystat", 1);
            statServiceImpl.process();
        }


        long count = statServiceImpl.currentMinuteCount("mystat");

        assertEquals(18, count);

        count = statServiceImpl.lastMinuteCount("mystat");

        assertEquals(122, count);


        testTimer.seconds(-1);
        statServiceImpl.tick();
        count = statServiceImpl.lastNSecondsCountExact("mystat", 20);


        assertEquals(38, count);

    }


    @Test
    public void testRecord() throws Exception {

        statServiceImpl.recordCount("mystat", 1);

        ok = replicator.count.get() == 1 || die();
        ok = recorder.count == 0 || die();

        statServiceImpl.process();
        statServiceImpl.queueLimit();
        statServiceImpl.queueEmpty();

        ok = recorder.count == 0 || die();


        //Time dilation
        //statServiceImpl.time(System.currentTimeMillis() + 61 * 1000);
        testTimer.seconds(61);
        statServiceImpl.process();

        ok = recorder.count == 1 || die();

    }

    @Test
    public void testRecordAll() throws Exception {


        String[] names = Arry.array("stat1", "stat2");
        long[] counts = Lng.array(1, 2);
        //long[] times = Lng.array(Timer.timer().now(), Timer.timer().now() + 2000);

        statServiceImpl.recordAll(Timer.timer().now(), names, counts);

        ok = replicator.count.get() == 3 || die(replicator.count);
        ok = recorder.count == 0 || die(recorder.count);

        statServiceImpl.process();
        statServiceImpl.queueLimit();
        statServiceImpl.queueEmpty();

        ok = recorder.count == 0 || die(recorder.count);


        //Time dilation
        testTimer.seconds(61);
        statServiceImpl.process();

        ok = recorder.count == 2 || die(recorder.count);

    }


    @Test
    public void testReplicators() throws Exception {


        statServiceImpl = new StatServiceBuilder().setRecorder(recorder)
                .addReplicator(replicator)
                .addReplicator(replicator)
                .addReplicator(new NoOpReplicator())
                .build();


        String[] names = Arry.array("stat1", "stat2");
        long[] counts = Lng.array(1, 2);
        long[] times = Lng.array(Timer.timer().now(), Timer.timer().now() + 2000);

        statServiceImpl.recordAllWithTimes(names, counts, times);

        ok = replicator.count.get() == 6 || die(replicator.count);
        ok = recorder.count == 0 || die(recorder.count);

        statServiceImpl.process();
        statServiceImpl.queueLimit();
        statServiceImpl.queueEmpty();

        ok = recorder.count == 0 || die(recorder.count);


    }
}
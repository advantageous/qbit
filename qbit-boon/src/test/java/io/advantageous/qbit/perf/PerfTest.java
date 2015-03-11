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

package io.advantageous.qbit.perf;

import io.advantageous.boon.collections.LongList;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by rhightower on 2/7/15.
 */
public class PerfTest {

    public static String fmt(int num) {
        return String.format("%,d", num);
    }

    public static String fmt(long num) {
        return String.format("%,d", num);
    }

    public static void perfTest(final QueueBuilder queueBuilder, final int readers, final int writers, final int totalCountExpected, final int timeOut, LongList readTimes, int extra, int sleepAmount, int sleepEvery, boolean cpuIntensive, int times) throws Exception {

        final int itemsEachThread = totalCountExpected / writers + 1;


        final long start = System.currentTimeMillis();

        puts("---------------------------------------------------------");


        final Queue<Integer> queue = queueBuilder.build();
        final List<TestReader> readerList = new ArrayList<>(readers);
        final List<Thread> writeThreadList = new ArrayList<>(writers);
        final List<Thread> readerThreadList = new ArrayList<>(readers);


        for ( int index = 0; index < writers; index++ ) {
            int amountEachThread = itemsEachThread + ( totalCountExpected % writers ) + extra;
            createWriterThread(writeThreadList, queue, amountEachThread, sleepAmount, sleepEvery);
        }

        final long writeThreadsStarted = System.currentTimeMillis();


        for ( int index = 0; index < readers; index++ ) {

            final TestReader reader = new TestReader(queue, cpuIntensive, times);
            readerList.add(reader);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    reader.read();
                }
            });
            thread.start();
            readerThreadList.add(thread);
        }

        final long readThreadsStarted = System.currentTimeMillis();
        final AtomicLong writeDuration = new AtomicLong();

        final Thread writerThreadCounter = writeTimer(writeThreadList, writeThreadsStarted, writeDuration);

        int localCount = 0;

        int statusCount = 0;


        exitLoop:
        while ( true ) {
            Sys.sleep(10);
            statusCount++;

            if ( statusCount % 10 == 0 ) {
                System.out.print(".");
            }


            if ( statusCount % 100 == 0 ) {
                System.out.println(fmt(localCount));
            }
            if ( start - System.currentTimeMillis() > timeOut ) {
                break;
            }
            for ( TestReader reader : readerList ) {
                localCount = reader.totalOut;
                if ( localCount >= totalCountExpected ) {
                    break exitLoop;
                }
            }
        }


        long end = System.currentTimeMillis();


        for ( TestReader testReader : readerList ) {
            testReader.stop();
            puts(testReader.answer.get());
        }
        Sys.sleep(100);


        puts("\n---------------------------------------------------------");
        puts("\nThreads readers    \t", readers, "\n        writers    \t", writers, "\nMessage count      \t", fmt(totalCountExpected), "\nMsg cnt per thrd   \t", fmt(itemsEachThread), "\nBatch size         \t", fmt(queueBuilder.getBatchSize()), "\nNum batches        \t", fmt(queueBuilder.getSize()), "\n---------------------------------------------------");


        puts("\nCount          \t", fmt(localCount), "\nRead time total\t", fmt(end - readThreadsStarted), "\nWrite time     \t", fmt(writeDuration.get()));

        readTimes.add(end - readThreadsStarted);


        try {
            writerThreadCounter.join(2000);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }


    }

    private static Thread writeTimer(final List<Thread> writeThreadList, final long writeThreadsStarted, final AtomicLong writeDuration) throws Exception {


        ListIterator<Thread> threadListIterator = writeThreadList.listIterator();

        while ( threadListIterator.hasNext() ) {
            Thread thread = threadListIterator.next();
            if ( !thread.isAlive() ) {
                threadListIterator.remove();
            }
        }
        Sys.sleep(10);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Sys.sleep(10);

                ListIterator<Thread> threadListIterator = writeThreadList.listIterator();

                while ( threadListIterator.hasNext() ) {
                    Thread thread = threadListIterator.next();
                    if ( !thread.isAlive() ) {
                        threadListIterator.remove();
                    }
                }
                Sys.sleep(10);


                while ( threadListIterator.hasNext() ) {
                    Thread thread = threadListIterator.next();
                    if ( !thread.isAlive() ) {
                        threadListIterator.remove();
                    }
                }
                Sys.sleep(10);
                for ( Thread writerThread : writeThreadList ) {
                    try {
                        writerThread.join(1000);
                    } catch ( InterruptedException e ) {
                    }
                }
                writeDuration.set(System.currentTimeMillis() - writeThreadsStarted);
            }
        });
        thread.start();
        return thread;
    }

    private static void createWriterThread(final List<Thread> threadList, final Queue<Integer> queue, final int itemsEachThread, final int sleepAmount, final int sleepEvery) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                final SendQueue<Integer> integerSendQueue = queue.sendQueue();

                int count = 0;

                for ( int index = 0; index < itemsEachThread; index++ ) {
                    count++;
                    if ( count > sleepEvery ) {
                        count = 0;
                        if ( sleepAmount > 0 ) {
                            Sys.sleep(sleepAmount);
                        }
                    }

                    integerSendQueue.send(1);
                }
                integerSendQueue.flushSends();
            }
        });
        threadList.add(thread);
        thread.start();
    }

    public static void main(String... args) throws Exception {


        final int batchSize = 100_000;
        final int totalSends = 400_000_000;
        final int timeout = 5_000;
        final int fudgeFactor = 100;
        final int sleepAmount = 1;
        final int sleepEvery = 400_000_000;
        final int checkEvery = 1000;
        final boolean cpuIntensive = false;
        final int times = 2_000_000_000;


//            final QueueBuilder queueBuilder = queueBuilder()
//                    .setBatchSize(batchSize)
//                    .setLinkTransferQueue()
//                    .setCheckEvery(checkEvery).setTryTransfer(true);


//        final QueueBuilder queueBuilder = queueBuilder()
//                .setBatchSize(batchSize)
//                .setLinkTransferQueue();


        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setBatchSize(batchSize).setSize(10_000_000).setArrayBlockingQueue();

        perfTest(queueBuilder, 1, 10, totalSends, 50_000, new LongList(), fudgeFactor, sleepAmount, sleepEvery, cpuIntensive, times);
        System.gc();
        Sys.sleep(10_000);


        final LongList timeMeasurements = new LongList();

        for ( int writers = 0; writers < 25; writers += 5 ) {
            int numThreads = writers == 0 ? writers + 1 : writers;
            perfTest(queueBuilder, 1, numThreads, totalSends, timeout, timeMeasurements, fudgeFactor, sleepAmount, sleepEvery, cpuIntensive, times);
            Sys.sleep(500);
            System.gc();
            Sys.sleep(3_000);
        }


        for ( Long value : timeMeasurements ) {
            puts(value);
        }

        puts(timeMeasurements);
        puts("\nmin    \t", timeMeasurements.min(), "\nmax    \t", timeMeasurements.max(), "\nmean   \t", timeMeasurements.mean(), "\nmedian \t", timeMeasurements.median(), "\nstddev \t", timeMeasurements.standardDeviation());
    }

    @Before
    public void setup() {

    }

    static class TestReader {

        private final boolean cpuIntensive;
        private final int times;
        private final Queue<Integer> queue;
        private final ReceiveQueue<Integer> receiveQueue;
        public AtomicLong answer = new AtomicLong();
        int total;
        volatile int totalOut;
        AtomicBoolean stop = new AtomicBoolean();

        TestReader(final Queue<Integer> queue, boolean cpuIntensive, int times) {
            this.queue = queue;
            this.receiveQueue = queue.receiveQueue();
            this.cpuIntensive = cpuIntensive;
            this.times = times;
        }


        public void stop() {
            stop.set(true);
        }

        public void read() {

            Integer value = receiveQueue.poll();

            while ( true ) {

                while ( value != null ) {
                    total += value;

                    if ( total % 10 == 0 ) {
                        totalOut = total;
                    }
                    if ( cpuIntensive && total % 13 == 0 ) {
                        doSomething(value);
                    }
                    value = receiveQueue.poll();
                }

                totalOut = total;
                value = receiveQueue.pollWait();
                if ( stop.get() ) {
                    return;
                }
            }
        }

        private void doSomething(Integer value) {

            long lv = 0;
            for ( int j = 0; j < 10; j++ ) {
                for ( int index = 0; index < times; index++ ) {
                    lv = value * index % 13 + index;
                    lv = lv * 47;
                    lv = lv * 1000;
                    lv = lv * 13 + lv % 31;
                }
                this.answer.set(this.answer.get() + lv);
            }
        }
    }
}

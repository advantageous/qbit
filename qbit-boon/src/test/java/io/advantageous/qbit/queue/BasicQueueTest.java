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

package io.advantageous.qbit.queue;

import io.advantageous.boon.Lists;
import io.advantageous.boon.core.Sys;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.core.Sys.sleep;

/**
 * Created by Richard on 8/11/14.
 */
public class BasicQueueTest {

    boolean ok;


    @Test
    public void testUsingListener() {

        final QueueBuilder builder = new QueueBuilder().setName("test").setPollWait(1000).setBatchSize(10);
        Queue<String> queue = builder.build();

        //new BasicQueue<>("test", 1000, TimeUnit.MILLISECONDS, 10);

        final int[] counter = new int[1];

        queue.startListener(new ReceiveQueueListener<String>() {
            @Override
            public void receive(String item) {
                puts(item);
                synchronized (counter) {
                    counter[0]++;
                }
            }

            @Override
            public void empty() {
                puts("Queue is empty");

            }

            @Override
            public void limit() {

                puts("Batch size limit is reached");
            }

            @Override
            public void shutdown() {

                puts("Queue is shut down");
            }

            @Override
            public void idle() {

                puts("Queue is idle");

            }
        });

        final SendQueue<String> sendQueue = queue.sendQueue();
        for (int index = 0; index < 10; index++) {
            sendQueue.send("item" + index);
        }


        sendQueue.flushSends();

        sleep(100);
        synchronized (counter) {
            puts("1", counter[0]);
        }


        for (int index = 0; index < 100; index++) {
            sendQueue.send("item2nd" + index);
        }

        sendQueue.flushSends();


        sleep(100);
        synchronized (counter) {
            puts("2", counter[0]);
        }

        for (int index = 0; index < 5; index++) {
            sleep(100);
            sendQueue.send("item3rd" + index);
        }
        sendQueue.flushSends();

        sleep(100);
        synchronized (counter) {
            puts("3", counter[0]);
        }


        sendQueue.sendMany("hello", "how", "are", "you");


        sleep(100);
        synchronized (counter) {
            puts("4", counter[0]);
        }

        List<String> list = Lists.linkedList("Good", "Thanks");

        sendQueue.sendBatch(list);


        sleep(100);
        synchronized (counter) {
            puts("1", counter[0]);
        }


        sleep(100);
        synchronized (counter) {
            ok = counter[0] == 121 || die("Crap not 121", counter[0]);
        }


        queue.stop();

    }


    @Test
    public void testUsingInput() throws Exception {


        final QueueBuilder builder = new QueueBuilder().setName("test").setPollWait(1000).setBatchSize(10);
        Queue<String> queue = builder.build();

        final int count[] = new int[1];


        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {


                final SendQueue<String> sendQueue = queue.sendQueue();

                for (int index = 0; index < 1000; index++) {
                    sendQueue.send("item" + index);
                }
                sendQueue.flushSends();
            }
        });


        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                ReceiveQueue<String> receiveQueue = queue.receiveQueue();

                while (receiveQueue.poll() != null) {
                    count[0]++;
                }
            }
        });

        writer.start();

        sleep(100);

        reader.start();

        writer.join();
        reader.join();

        puts(count[0]);

        ok = count[0] == 1000 || die("count should be 1000", count[0]);

    }


    @Test
    public void testUsingInputTake() throws Exception {


        final QueueBuilder builder = new QueueBuilder().setName("test").setPollWait(1000).setBatchSize(10);
        Queue<String> queue = builder.build();

        final AtomicLong count = new AtomicLong();

        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {

                long cnt = 0;
                final ReceiveQueue<String> receiveQueue = queue.receiveQueue();
                String item = receiveQueue.take();

                while (item != null) {
                    cnt++;
                    puts(item);
                    item = receiveQueue.take();

                    if (cnt >= 900) {
                        count.set(cnt);
                        break;
                    }
                }
            }
        });


        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {

                final SendQueue<String> sendQueue = queue.sendQueue();

                for (int index = 0; index < 1000; index++) {
                    sendQueue.send("this item " + index);
                }
                sendQueue.flushSends();
            }
        });


        writer.start();


        reader.start();

        writer.join();
        reader.join();

        puts(count.get());

        ok = count.get() == 900 || die("count should be 1000", count.get());

    }


    @Test
    public void testUsingInputPollWait() throws Exception {


        /** Build our queue. */
        final QueueBuilder builder = new QueueBuilder().setName("test").setPollWait(1000).setBatchSize(10);
        Queue<String> queue = builder.build();


        final AtomicInteger count = new AtomicInteger();

        /* Create a sender queue. */
        final SendQueue<String> sendQueue = queue.sendQueue();


        /* Create a receiver queue. */
        final ReceiveQueue<String> receiveQueue = queue.receiveQueue();


        /* Create a writer thread that uses the send queue. */
        Thread writerThread = new Thread(() -> {


            for (int index = 0; index < 1000; index++) {
                sendQueue.send("item" + index); //It will flush every 10 or so
            }
            sendQueue.flushSends(); //We can also call flushSends so it sends what remains.
        });



        /* Create a reader thread that consumes queue items. */
        Thread readerThread = new Thread(() -> {
            String item = receiveQueue.pollWait();

            while (item != null) {
                count.incrementAndGet();
                item = receiveQueue.pollWait();

            }
        });

        /* Starts the threads and wait for them to end. */
        writerThread.start();
        readerThread.start();

        /* Wait for them to end. */
        writerThread.join();
        readerThread.join();

        puts(count);

        ok = count.get() == 1000 || die("count should be 1000", count.get());

    }




    @Test
    public void testUsingAutoFlush() throws Exception {


        final QueueBuilder builder = new QueueBuilder().setName("test").setPollWait(1000).setBatchSize(20_000);
        final Queue<String> queue = builder.build();

        final AtomicInteger count = new AtomicInteger();

        final SendQueue<String> sendQueue = queue.sendQueueWithAutoFlush(50, TimeUnit.MILLISECONDS);
        final ReceiveQueue<String> receiveQueue = queue.receiveQueue();

        sendQueue.start();

        Thread writerThread = new Thread(() -> {
            for (int index = 0; index < 1000; index++) {
                sendQueue.send("item" + index);
            }
        });


        Thread readerThread = new Thread(() -> {
            while (receiveQueue.pollWait() != null) {
                count.incrementAndGet();
            }
        });

        writerThread.start();
        readerThread.start();
        writerThread.join();
        readerThread.join();
        sleep(1000); //simulate a long sleep
        sendQueue.stop();

        puts(count);

        ok = count.get() == 1000 || die("count should be 1000", count);

    }


}

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

package io.advantageous.qbit.example.queues;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import org.boon.core.Sys;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by Richard on 9/12/14.
 */
public class ExampleMainQBitSingleWriterSingleReader {


    static final Queue<Integer> queue = new QueueBuilder().setBatchSize(1_000).build();
    static final int status = 1_000_000;
    static final int sleepEvery = 1_000_000;
    static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void sender(int amount, int code) throws InterruptedException {

        SendQueue<Integer> sendQueue = queue.sendQueue();

        for (int index = 0; index < amount; index++) {

            sendQueue.send(index);

        }
        sendQueue.send(code);
        sendQueue.flushSends();
    }

    public static long counter() throws Exception {


        ReceiveQueue<Integer> receiveQueue = queue.receiveQueue();

        long count = 0;
        long index = 0;

        while (true) {
            index++;

            Integer item = receiveQueue.take();

            if (index % status == 0) {
                System.out.println("Got " + item);
            }


            if (item % sleepEvery == 0) {
                Sys.sleep(50);
            }


            if (item == -1) {

                System.out.println("DONE");
                return count;
            }
            count += item;
        }


    }

    public static void main(String... args) throws Exception {


        long startTime = System.currentTimeMillis();


        final Future<Long> receiverJob = executorService.submit(new Callable<Long>() {
            @Override
            public Long call() {
                try {
                    return counter();
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1L;
                }
            }
        });


        final Future<?> senderJob = executorService.submit(new Runnable() {
            @Override
            public void run() {

                try {
                    sender(50_000_000, -1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        senderJob.get();

        Long count = receiverJob.get();

        System.out.println("Count " + count);

        if (count != 1249999975000000L) {
            System.err.println("TEST FAILED");
        }


        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println(duration);

        executorService.shutdown();

    }
}

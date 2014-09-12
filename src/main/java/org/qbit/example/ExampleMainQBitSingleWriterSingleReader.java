package org.qbit.example;

import org.boon.core.Sys;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.SendQueue;
import org.qbit.queue.impl.BasicQueue;

import java.util.concurrent.*;


/**
 * Created by Richard on 9/12/14.
 */
public class ExampleMainQBitSingleWriterSingleReader {



    static ExecutorService executorService = Executors.newCachedThreadPool();

    static final BasicQueue<Integer> queue =  BasicQueue.create(Integer.class, 1_000);


    static final int status = 1_000_000;

    static final int sleepEvery = 1_000_000;


    public static void sender(int amount, int code) throws InterruptedException{

        SendQueue<Integer> sendQueue = queue.sendQueue();

        for (int index = 0; index < amount; index++) {

            sendQueue.send(index);

        }
        sendQueue.send(code);
        sendQueue.flushSends();
    }

    public static long counter() throws Exception {


        ReceiveQueue<Integer> receiveQueue =  queue.receiveQueue();

        long count = 0;
        long index=0;

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

        if (count!=1249999975000000L) {
            System.err.println("TEST FAILED");
        }


        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println(duration);

        executorService.shutdown();

    }
}

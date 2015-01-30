package io.advantageous.qbit.example.queues;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import org.boon.core.Sys;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.queue.impl.BasicQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Richard on 9/12/14.
 */
public class ExampleMainQBitSingleWriterMultiReader {



    static final Queue<Integer> queue =    new QueueBuilder().setBatchSize(1_000).build();




    static ExecutorService executorService = Executors.newCachedThreadPool();

    static final int status = 1_000_000;

    static final int sleepEvery = 1_000_000;

    static final int numReaders = 10;

    static final List<Future<Long>> receiverJobs = new ArrayList<>();

    static AtomicBoolean stop = new AtomicBoolean();



    public static void sender(int amount, int code) throws InterruptedException{

        final SendQueue<Integer> sendQueue = queue.sendQueue();
        try {

            for (int index = 0; index < amount; index++) {

                sendQueue.send(index);

            }
            sendQueue.flushSends();

            for (int index = 0; index < 1_000_000; index++) {
                sendQueue.sendAndFlush(code);
            }

        }catch (Exception ex) {
            if (stop.get()) {
                Thread.interrupted();
                return;
            }
        }

    }
    public static long counter(int workerId) throws Exception {


        final ReceiveQueue<Integer> receiveQueue = queue.receiveQueue();


        long count = 0;

        while (true) {

            Integer item = receiveQueue.take();

            if (item % status == 0) {
                System.out.println(" " + workerId + " Got " + item);
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

        for (int index = 0; index < numReaders; index++) {
            final int workerId = index;
            receiverJobs.add(executorService.submit(new Callable<Long>() {
                @Override
                public Long call() {
                    try {
                        return counter(workerId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return -1L;
                    }
                }
            }));
        }


        final Future<?> senderJob = executorService.submit(new Runnable() {
            @Override
            public void run() {

                try {
                    sender(200_000_000, -1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });


        long count = 0L;
        for (Future<Long> future : receiverJobs) {
            count+=future.get();
        }

        System.out.println("Count " + count);

        if (count!=1249999975000000L) {
            System.err.println("TEST FAILED");
        }


        senderJob.cancel(true);

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println(duration);


        executorService.shutdown();

    }
}



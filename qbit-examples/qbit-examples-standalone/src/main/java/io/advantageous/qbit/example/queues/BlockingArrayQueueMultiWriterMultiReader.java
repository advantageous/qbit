package io.advantageous.qbit.example.queues;

import org.boon.core.Sys;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Richard on 9/12/14.
 */
public class BlockingArrayQueueMultiWriterMultiReader {


    static ExecutorService executorService = Executors.newCachedThreadPool();

    static final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>(100_000);

    static final int status = 1_000_000;

    static final int sleepEvery = 1_000_000;

    static final int numReaders = 2;

    static final int numWriters = 10;

    static final int amountOfMessagesToSend = 10_000_000; //Each

    static final List<Future<Long>> receiverJobs = new ArrayList<>();

    static final List<Future<?>> writerJobs = new ArrayList<>();

    static AtomicBoolean stop = new AtomicBoolean();


    public static void sender(int workerId, int amount, int code) throws InterruptedException {

        try {

            for (int index = 0; index < amount; index++) {

                queue.put(index);

            }


            Sys.sleep(2000); //This avoids race condition adds ane extra two seconds on time only needed for multi reader


            for (int index = 0; index < 1_000_000; index++) {
                queue.put(code);
            }
        } catch (InterruptedException ex) {

            System.out.println("SENDER " + workerId);
            if (stop.get()) {
                Thread.interrupted();
                return;
            }
        }

    }

    public static long counter(int workerId) throws Exception {


        long count = 0;

        while (true) {

            Integer item = queue.take();

            if (item % status == 0) {
                System.out.println(" " + workerId + " Got " + item);
            }

            if (item % sleepEvery == 0) {
                Sys.sleep(50);
            }

            if (item == -1) {

                System.out.println("DONE " + workerId);
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

        for (int index = 0; index < numWriters; index++) {
            final int workerId = index;
            writerJobs.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        sender(workerId, amountOfMessagesToSend, -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }


        long count = 0L;
        for (Future<Long> future : receiverJobs) {
            count += future.get();
        }

        System.out.println("Count " + count);

        if (count != 499999950000000L) {
            System.err.println("TEST FAILED");
        }


        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println("TIME " + duration);

        stop.set(true);

        for (Future<Long> future : receiverJobs) {
            future.cancel(true);
        }


        executorService.shutdown();

    }
}

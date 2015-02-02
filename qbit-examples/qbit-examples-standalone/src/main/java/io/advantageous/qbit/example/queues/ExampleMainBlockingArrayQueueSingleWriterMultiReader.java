package io.advantageous.qbit.example.queues;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExampleMainBlockingArrayQueueSingleWriterMultiReader {

    static ExecutorService executorService = Executors.newCachedThreadPool();

    static final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>(100_000);

    static final int status = 1_000_000;

    static final int sleepEvery = 1_000_000;

    static final int numReaders = 10;

    static final List<Future<Long>> receiverJobs = new ArrayList<>();

    static AtomicBoolean stop = new AtomicBoolean();


    public static void sender(int amount, int code) throws InterruptedException {

        try {

            for (int index = 0; index < amount; index++) {

                queue.put(index);

            }

            for (int index = 0; index < 1_000_000; index++) {
                queue.put(code);
            }
        } catch (InterruptedException ex) {
            if (stop.get()) {
                Thread.interrupted();
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
                Thread.sleep(50);
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
            receiverJobs.add(executorService.submit(() -> {
                try {
                    return counter(workerId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1L;
                }
            }));
        }

        final Future<?> senderJob = executorService.submit(() -> {

            try {
                sender(50_000_000, -1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

        long count = 0L;
        for (Future<Long> future : receiverJobs) {
            count += future.get();
        }

        System.out.println("Count " + count);

        if (count != 1249999975000000L) {
            System.err.println("TEST FAILED");
        }
        senderJob.cancel(true);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println(duration);
        executorService.shutdown();

    }
}



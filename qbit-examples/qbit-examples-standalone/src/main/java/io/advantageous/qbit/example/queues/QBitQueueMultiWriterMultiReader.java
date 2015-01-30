package io.advantageous.qbit.example.queues;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.queue.impl.BasicQueue;
import org.boon.core.Sys;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Richard on 9/12/14.
 * <p>
 * Description                        QBIT(ms)          LinkedBlockingQueue(ms)                         %Better
 * One Reader, One Writer                6276                            10,003                          159.38
 * Two Reader, One Writer                4235                             9,105                          214.99
 * Ten Readers, One Writer                586                             9,196                        1,542.95
 * 10 Readers, 10 Writers                4782                            15,182                          317.48
 * 1 Readers, 10 Writers               40,618                            16,472                         -246.59     QBIT LOST!
 * 2 Readers, 10 Writer                16,491                            18,342                          111.22
 * 5 Readers, 10 Writers               10,598                            17,587                          165.95
 * 10 Readers, 1 Writer                   316                             1,616                          511.39
 * 10 Readers, 5 Writer                 1,060                             7,589                          715.94
 */
public class QBitQueueMultiWriterMultiReader {


    static final Queue<Integer> queue = new QueueBuilder()
            .setBatchSize(100_000)
            .setPollWait(100).setSize(6).setTryTransfer(true).setLinkTransferQueue().setCheckEvery(1000)
            .build();

    static ExecutorService executorService = Executors.newCachedThreadPool();

    static final int status = 100_000_000;

    static final int sleepEvery = 1_000_000_000;

    static final int numReaders = 1;

    static final int numWriters = 5;

    static final int amountOfMessagesToSend = 100_000_000; //Each

    static final List<Future<Long>> receiverJobs = new ArrayList<>();

    static final List<Future<?>> writerJobs = new ArrayList<>();

    static AtomicBoolean stop = new AtomicBoolean();


    public static void sender(int workerId, int amount, int code) throws InterruptedException {

        final SendQueue<Integer> sendQueue = queue.sendQueue();
        try {

            for (int index = 0; index < amount; index++) {

                sendQueue.send(index);

            }
            sendQueue.flushSends();

            Sys.sleep(2000); //This avoids race condition adds ane extra two seconds on time only needed for multi reader

            for (int index = 0; index < 10_000_000; index++) {
                sendQueue.sendAndFlush(code);
            }

        } catch (Exception ex) {
            System.out.println("SENDER " + workerId);

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


        System.out.println(duration);


        executorService.shutdown();

        System.out.println("RATE " + ((numWriters * amountOfMessagesToSend) / (duration / 1000)) + " per second" );

        //12,500,000
        //25,000,000
        //35_714_285
        //142_857_142
        //39_215_686
        //100_000_000
        //42_553_191
        //100_000_000
        //117_647_058
        //133_333_333
        //166_666_666
        //181_818_181
        //200_000_000
        //222_222_222
        //125_000_000
        //50_000_000


    }

}

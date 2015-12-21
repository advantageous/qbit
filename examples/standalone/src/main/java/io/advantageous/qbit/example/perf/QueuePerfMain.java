package io.advantageous.qbit.example.perf;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.SendQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class QueuePerfMain {

    public static class Trade {
        final String name;
        final long amount;

        public Trade(String name, long amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public long getAmount() {
            return amount;
        }
    }

    public static void main(final String... args) throws Exception {

        final int runs = 73;
        final int tradeCount = 230_000;
        final int batchSize = 1600;
        final int checkEvery = 0;
        final int numThreads = 6;
        final int pollWait = 1_000;
        final int size = 1_000_000;


        for (int index = 0; index < 10; index++) {
            run(2, 1000, 10, 3, 2, 100, size);
        }

        for (int index = 0; index < 100; index++) {
            run(runs, tradeCount, batchSize, checkEvery, numThreads, pollWait, size);
        }
    }


    private static void run(int runs, int tradeCount, int batchSize, int checkEvery, int numThreads, int pollWait, int size) {

        final QueueBuilder queueBuilder = QueueBuilder
                .queueBuilder()
                    .setName("trades")
                    .setBatchSize(batchSize)
                    .setSize(size)
                    .setPollWait(pollWait);


        final int totalTrades = tradeCount * runs * numThreads;

        if (checkEvery > 0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
            queueBuilder.setBatchSize(batchSize);
        }

        final Queue<Trade> queue = queueBuilder.build();
        final AtomicLong tradeCounter = new AtomicLong();

        queue.startListener(item -> {
            item.getAmount();
            item.getName();
            tradeCounter.incrementAndGet();
        });


        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {
            runThreads(tradeCount, numThreads, queue, tradeCounter);
        }
        System.out.printf("DONE traded %,d in %d ms \nbatchSize = %,d, checkEvery = %,d, threads= %,d \n\n",
                totalTrades,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery,
                numThreads);
        queue.stop();

    }

    private static void runThreads(int tradeCount, int numThreads, Queue<Trade> queue, AtomicLong tradeCounter) {
        final List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < numThreads; t++) {

            final Thread thread = new Thread(() -> {
                sendMessages(queue, tradeCount);
            });
            thread.start();
            threads.add(thread);
        }
        for (int index = 0; index < 100000; index++) {
            Sys.sleep(10);
            if (tradeCounter.get() >= (tradeCount * numThreads)) {
                break;
            }
        }
    }

    private static void sendMessages(final Queue<Trade> queue, final int tradeCount) {
        final SendQueue<Trade> tradeSendQueue = queue.sendQueue();
        for (int c = 0; c < tradeCount; c++) {
            tradeSendQueue.send(new Trade("ibm", 100L));
        }
        tradeSendQueue.flushSends();
    }


}

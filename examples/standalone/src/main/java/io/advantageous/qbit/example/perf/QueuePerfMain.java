package io.advantageous.qbit.example.perf;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.SendQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created by rick on 12/12/15.
 */
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

    public interface TradeService {

        void trade(Trade trade);
    }

    public static class TradeServiceImpl {

        final AtomicLong tradeCounter = new AtomicLong();

        void trade(Trade trade) {
            trade.getAmount();
            tradeCounter.incrementAndGet();
        }

    }

    public static void main(final String... args) throws Exception {

        final int runs = 55;
        final int tradeCount = 450_000;
        final int batchSize = 10_000;
        final int checkEvery = 0;
        final int numThreads = 3;
        final int pollWait = 1_000;


        for (int index = 0; index < 10; index++) {
            run(2, 1000, 10, 3, 2, 100);
        }

        for (int index = 0; index < 100; index++) {
            run(runs, tradeCount, batchSize, checkEvery, numThreads, pollWait);
        }
    }


    private static void run(int runs, int tradeCount, int batchSize, int checkEvery, int numThreads, int pollWait) {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setSize(1_000_000).setPollWait(pollWait);


        final int totalTrades = tradeCount * runs * numThreads;

        if (checkEvery > 0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
            queueBuilder.setBatchSize(batchSize);
        }

        final Queue<Trade> queue = queueBuilder.build();
        final AtomicLong tradeCounter = new AtomicLong();

        queue.startListener(item -> {

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


        //final long startTime = System.currentTimeMillis();
        for (int c = 0; c < tradeCount; c++) {
            tradeSendQueue.send(new Trade("ibm", 100L));
        }
        tradeSendQueue.flushSends();
    }


}

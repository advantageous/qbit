package io.advantageous.qbit.example.perf;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.stream.QueueToStreamUnicast;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class ReactiveQueuePerfMain {

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



    public static void main(String... args) throws Exception {

        final int runs = 20;
        final int tradeCount = 1_800_000;
        final int batchSize = 10000;
        final int checkEvery = 0;
        final int numThreads = 2;
        final boolean forceLinkTransferQueue = true;


        for (int index=0; index< 20; index++) {

            run(runs, tradeCount, batchSize, checkEvery, numThreads, forceLinkTransferQueue);
        }

        System.out.println("DONE");
    }


    private static void run(int runs, int tradeCount, final int batchSize, int checkEvery,
                                               int numThreads, boolean forceLinkTransferQueue) throws InterruptedException {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setSize(1_000_000);

        if (checkEvery>0 || forceLinkTransferQueue) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
        }

        final Queue<Trade> queue = queueBuilder.build();


        final AtomicLong tradeCounterReceived = new AtomicLong();

        final AtomicLong tradeCounterSent = new AtomicLong();

        final AtomicBoolean empty = new AtomicBoolean();
        final int totalTradeCount = tradeCount  * numThreads;
        final QueueToStreamUnicast<Trade> stream = new QueueToStreamUnicast<>(queue, () ->
        {
            //System.out.println("EMPTY");
            empty.set(true);
        });


            stream.subscribe(new Subscriber<Trade>() {


                int count = batchSize/2;
                Subscription subscription;
                @Override
                public void onSubscribe(Subscription s) {
                    subscription = s;
                    s.request(count);
                }

                @Override
                public void onNext(Trade trade) {
                    tradeCounterReceived.incrementAndGet();
                    trade.getName();
                    trade.getAmount();

                    count--;
                    if (count == 0) {
                        count = batchSize/2;
                        subscription.request(count);
                    }

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {

                }
            });






        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {

            List<Thread> threads = new ArrayList<>(numThreads);

            tradeCounterReceived.set(0L);
            tradeCounterSent.set(0L);

            for (int t = 0; t < numThreads; t++) {


                Thread thread =  new Thread(() -> {

                    sendMessages(tradeCount, queue);

                });

                thread.start();
                threads.add(thread);
            }

            for (int index = 0; index < 100000; index++) {
                Sys.sleep(10);
                if (tradeCounterReceived.get() >= totalTradeCount) {
                    break;
                }
            }

        }


        System.out.printf("DONE PUB traded %,d in %d batchSize = %,d, checkEvery = %,d, threads=%d\n",
                tradeCount * runs * numThreads,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery,
                numThreads);
        queue.stop();


    }

    private static void sendMessages(int tradeCount, Queue<Trade> queue) {
        final SendQueue<Trade> tradeSendQueue = queue.sendQueue();


        for (int c = 1; c < tradeCount+1; c++) {
            tradeSendQueue.send(new Trade("ibm", 100L));
        }
        tradeSendQueue.flushSends();
    }

}

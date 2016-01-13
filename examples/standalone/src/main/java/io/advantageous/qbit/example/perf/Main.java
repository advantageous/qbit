package io.advantageous.qbit.example.perf;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.stream.QueueToStreamRoundRobin;
import io.advantageous.qbit.stream.QueueToStreamUnicast;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

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


    private static void runService(int runs, int tradeCount, int batchSize, int checkEvery, boolean dynamic, boolean noStub,
                                   int numThreads) {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setPollWait(1_000)
                .setSize(20_000_000);

        if (checkEvery>0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
        }

        final TradeServiceImpl tradeServiceImpl = new TradeServiceImpl();

        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder()
                .setRequestQueueBuilder(queueBuilder)
                .setInvokeDynamic(dynamic).setServiceObject(tradeServiceImpl);

        if (noStub) {
            serviceBuilder.setServiceMethodHandler(methodCall -> {
                final Trade trade = (Trade) methodCall.args()[0];
                tradeServiceImpl.trade(trade);
                return ServiceConstants.VOID;
            });
        }

        final ServiceQueue serviceQueue = serviceBuilder.build().startServiceQueue();




        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {

            final long startTime = System.currentTimeMillis();

            final List<Thread> threadList = new ArrayList<>();

            for (int th = 0; th < numThreads; th++) {
                final Thread thread = new Thread(() -> {


                    final SendQueue<MethodCall<Object>> requests = serviceQueue.requests();
                    final TradeService tradeService = noStub ? trade -> {
                        final MethodCall<Object> methodCall = MethodCallBuilder.methodCallBuilder().setName("trade")
                                .setLocal(true).setBodyArgs(new Object[]{trade}).build();
                        requests.send(methodCall);
                    } : serviceQueue.createProxy(TradeService.class);

                    for (int t = 0; t < tradeCount; t++) {
                        tradeService.trade(new Trade("ibm", 100L));
                    }
                    ServiceProxyUtils.flushServiceProxy(tradeService);
                    requests.flushSends();

                });
                thread.start();
                threadList.add(thread);

            }


            for (int index = 0; index < 1000; index++) {
                Sys.sleep(1);
                if (tradeServiceImpl.tradeCounter.get() >=
                        (tradeCount * numThreads)) {
                    break;
                }
            }

            threadList.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            System.out.printf("%d traded %,d in %d batchSize = %,d\n",
                    r,
                    tradeCount * numThreads,
                    System.currentTimeMillis() - startTime,
                    batchSize);
        }


        System.out.printf("DONE traded %,d in %d batchSize = %,d, checkEvery = %,d\n",
                tradeCount * runs * numThreads,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery);
        serviceQueue.stop();


        Sys.sleep(2_000);
        System.gc();
        Sys.sleep(2_000);
    }

    public static void mainService(String... args) throws Exception {

        final int runs = 20;
        final int tradeCount = 250_000;
        final int batchSize = 1_000;
        final boolean dynamic = false;

        //int currentBatchSize = batchSize;



        for (int index=0; index< 100; index++) {
            runService(runs, tradeCount, batchSize, 0, dynamic, true, 4);
            //currentBatchSize*=2;
        }


        Sys.sleep(Integer.MAX_VALUE);
        System.out.println("DONE");
    }



    //public static void mainArrayBlockingQueue(String... args) throws Exception {
    public static void mainArrayBlockingQueue(String... args) throws Exception {

        final int runs = 20;
        final int tradeCount = 5_000_000;
        final int batchSize = 125;

        int currentBatchSize = batchSize;


        run(runs, tradeCount, 1);

        run(runs, tradeCount, batchSize);

        for (int index=0; index< 10; index++) {
            run(runs, tradeCount, currentBatchSize);
            currentBatchSize*=2;
        }
    }


    public static void mainStream(String... args) throws Exception {

        final int runs = 10;
        final int tradeCount = 10_000_000;
        final int batchSize = 100_000;
        final int checkEvery = 0;


        run(runs, tradeCount, batchSize);
        run(runs, tradeCount, batchSize);
        run(runs, tradeCount, batchSize);


        for (int index=0; index< 10; index++) {
            runPublisher(runs, tradeCount, batchSize, checkEvery);
        }
    }


    public static void main(String... args) throws Exception {


        final int runs = 10;
        final int tradeCount = 710_000;
        final int batchSize = 100_000;
        final int checkEvery = 0;
        final int subscribers = 4;
        final int numThreads = 7;




        for (int index=0; index< 10; index++) {
            runPublisherMultiSubs(runs, tradeCount, batchSize, checkEvery, subscribers, numThreads);
        }
    }


    //public static void main(String... args) throws Exception {

    public static void mainTransferQueue(String... args) throws Exception {

        final int runs = 20;
        final int tradeCount = 5_000_000;
        final int batchSize = 50_000;
        final int checkEvery = 1000;

        int currentCheckEvery = checkEvery;

        run(runs, tradeCount, batchSize, 10);

        for (int index=0; index< 10; index++) {
            run(runs, tradeCount, batchSize, currentCheckEvery);
            currentCheckEvery*=2;
        }
    }

    private static void run(int runs, int tradeCount, int batchSize) {
        run(runs, tradeCount, batchSize, 0);
    }

    private static void run(int runs, int tradeCount, int batchSize, int checkEvery) {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setSize(1_000_000);

        if (checkEvery>0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
        }

        final Queue<Trade> queue = queueBuilder.build();
        final AtomicLong tradeCounter = new AtomicLong();

        queue.startListener(item -> {

            tradeCounter.incrementAndGet();
        });


        final SendQueue<Trade> tradeSendQueue = queue.sendQueue();


        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {

           //final long startTime = System.currentTimeMillis();
           for (int t =0; t < tradeCount; t++) {
               tradeSendQueue.send(new Trade("ibm", 100L));
           }
           tradeSendQueue.flushSends();

            for (int index = 0; index < 1000; index++) {
                Sys.sleep(10);
                if (tradeCounter.get() >= tradeCount) {
                    break;
                }
            }

//            System.out.printf("%d traded %,d in %d batchSize = %,d\n",
//                    r,
//                    tradeCount,
//                    System.currentTimeMillis() - startTime,
//                    batchSize);
        }


        System.out.printf("DONE traded %,d in %d batchSize = %,d, checkEvery = %,d\n",
                tradeCount * runs,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery);
        queue.stop();


        Sys.sleep(2_000);
        System.gc();
        Sys.sleep(2_000);
    }



    private static void runPublisher(int runs, int tradeCount, final int batchSize, int checkEvery) throws InterruptedException {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setSize(1_000_000);

        if (checkEvery>0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
        }

        final Queue<Trade> queue = queueBuilder.build();
        final AtomicLong tradeCounter = new AtomicLong();


        final QueueToStreamUnicast<Trade> stream = new QueueToStreamUnicast<>(queue);

        stream.subscribe(new Subscriber<Trade>() {


            int count = batchSize * 2;
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                s.request(100_000);
            }

            @Override
            public void onNext(Trade trade) {
                tradeCounter.incrementAndGet();

                count--;
                if (count <= batchSize) {
                    count = batchSize * 2;
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


        final SendQueue<Trade> tradeSendQueue = queue.sendQueue();


        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {

            Thread thread = new Thread(() -> {

                for (int t =0; t < tradeCount; t++) {
                    tradeSendQueue.send(new Trade("ibm", 100L));
                }
                tradeSendQueue.flushSends();

            });

            thread.start();

            for (int index = 0; index < 1000; index++) {
                Sys.sleep(10);
                if (tradeCounter.get() >= tradeCount) {
                    break;
                }
            }

            thread.join();

//            System.out.printf("%d traded %,d in %d batchSize = %,d\n",
//                    r,
//                    tradeCount,
//                    System.currentTimeMillis() - startTime,
//                    batchSize);
        }


        System.out.printf("DONE PUB traded %,d in %d batchSize = %,d, checkEvery = %,d\n",
                tradeCount * runs,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery);
        queue.stop();


        Sys.sleep(2_000);
        System.gc();
        Sys.sleep(2_000);
    }

    private static void runPublisherMultiSubs(int runs, int tradeCount, final int batchSize, int checkEvery,
                                              int subscribers, int numThreads) throws InterruptedException {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setSize(1_000_000);

        if (checkEvery>0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
        }

        final Queue<Trade> queue = queueBuilder.build();
        final AtomicLong tradeCounter = new AtomicLong();


        final QueueToStreamRoundRobin<Trade> stream = new QueueToStreamRoundRobin<>(queue);

        for (int s=0; s<subscribers; s++) {

            stream.subscribe(new Subscriber<Trade>() {


                int count = batchSize / numThreads;
                Subscription subscription;
                @Override
                public void onSubscribe(Subscription s) {
                    subscription = s;
                    s.request(count);
                }

                @Override
                public void onNext(Trade trade) {
                    tradeCounter.incrementAndGet();

                    count--;
                    if (count <= batchSize) {
                        count = batchSize  / numThreads;
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

        }




        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {

            List<Thread> threads = new ArrayList<>(numThreads);

            for (int t = 0; t < numThreads; t++) {

                final SendQueue<Trade> tradeSendQueue = queue.sendQueue();

                Thread thread = new Thread(() -> {

                    for (int c = 0; c < tradeCount; c++) {
                        tradeSendQueue.send(new Trade("ibm", 100L));
                    }
                    tradeSendQueue.flushSends();

                });

                thread.start();
                threads.add(thread);
            }

            for (int index = 0; index < 1000; index++) {
                Sys.sleep(10);
                if (tradeCounter.get() >= tradeCount) {
                    break;
                }
            }

            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

//            System.out.printf("%d traded %,d in %d batchSize = %,d\n",
//                    r,
//                    tradeCount,
//                    System.currentTimeMillis() - startTime,
//                    batchSize);
        }


        System.out.printf("DONE PUB traded %,d in %d batchSize = %,d, checkEvery = %,d\n",
                tradeCount * runs * numThreads,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery);
        queue.stop();


        Sys.sleep(2_000);
        System.gc();
        Sys.sleep(2_000);
    }

}

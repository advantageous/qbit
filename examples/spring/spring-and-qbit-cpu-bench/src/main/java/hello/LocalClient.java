package hello;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.ArrayList;
import java.util.List;

public class LocalClient {


    static volatile long sentCount = 0L;
    static volatile long count = 0L;

    public static void main(String... args) {
        List<Thread> threads = new ArrayList<>();


        final long start = System.currentTimeMillis();
        for (int numThreads = 0; numThreads < 5; numThreads++) {

            Sys.sleep((33 * numThreads) + 1);

            final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
            serviceBuilder.getRequestQueueBuilder().setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(100);
            serviceBuilder.getResponseQueueBuilder().setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(100);

            final ServiceQueue serviceQueue = serviceBuilder.setServiceObject(new MyServiceQBit()).buildAndStartAll();


            Thread thread = new Thread(new Runnable() {


                final SimpleServiceProxy myService = serviceQueue.createProxy(SimpleServiceProxy.class);

                @Override
                public void run() {

                    for (int index = 0; index < 100_000_000; index++) {

                        if (index % 1000 == 0) {
                            sentCount+=1000;
                        }
                        final int findex = index;
                        myService.ping(new Callback<List<String>>() {
                            @Override
                            public void accept(List<String> strings) {

                                if (findex % 1000 == 0) {
                                    count+= 1000;
                                }
                            }
                        });


                        if (index%333_000==0) {
                            Sys.sleep(3);
                        }

                        if (index%1_000_000==0) {

                            for (int i = 0; i < 100; i++) {
                                if (count < (sentCount - 990_000)) {
                                    Sys.sleep(111 * (index % 9));
                                }
                            }
                        }


                    }
                }
            });

            threads.add(thread);
            thread.start();
        }



        loop:
        for (int x =0; x < 10000; x++) {
            Sys.sleep(1000);
            if (count > 1_000_000_000) {
                break loop;
            }



            long now = System.currentTimeMillis();
            long c = count;
            long rate = (c / (now - start)) * 1000;
            System.out.println( rate + " " + c + " " + rate *2);
        }


        System.out.println("Done");
    }

    interface SimpleServiceProxy {
        void ping(Callback<List<String>> doubleReturn);
    }

}

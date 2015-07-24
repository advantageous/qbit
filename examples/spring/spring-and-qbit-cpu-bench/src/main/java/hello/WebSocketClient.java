package hello;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.reactive.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * created by rhightower on 2/3/15.
 */
@SuppressWarnings("ALL")
public class WebSocketClient {


    static volatile long sentCount = 0L;
    static volatile long count = 0L;

    public static void main(String... args) {

        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6060;


        List<Thread> threads = new ArrayList<>();


        final long start = System.currentTimeMillis();
        for (int numThreads = 0; numThreads < 20; numThreads++) {

            Sys.sleep((33 * numThreads) + 1);
            final Client client = new ClientBuilder()
                    .setPort(port)
                    .setHost(host)
                    .setProtocolBatchSize(1_000)
                    .build();

            client.start();


            Thread thread = new Thread(new Runnable() {


                final SimpleServiceProxy myService = client.createProxy(SimpleServiceProxy.class, "myServiceQBit");

                @Override
                public void run() {

                    for (int index = 0; index < 10_000_000; index++) {

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
                            Sys.sleep(333);
                        }

                        if (index%100_000==0) {

                            for (int i = 0; i < 100; i++) {
                                if (count < (sentCount - 99_000)) {
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
            if (count > 100_000_000) {
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

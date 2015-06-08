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

    static volatile long count = 0L;

    public static void main(String... args) {

        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6060;


        List<Thread> threads = new ArrayList<>();


        for (int numThreads = 0; numThreads < 10; numThreads++) {
            final Client client = new ClientBuilder()
                    .setPort(port)
                    .setHost(host)
                    .setRequestBatchSize(700)
                    .build();

            client.start();


            Thread thread = new Thread(new Runnable() {


                final SimpleServiceProxy myService = client.createProxy(SimpleServiceProxy.class, "myServiceQBit");

                @Override
                public void run() {

                    for (int index = 0; index < 2_000_000; index++) {

                        final int findex = index;
                        myService.ping(new Callback<List<String>>() {
                            @Override
                            public void accept(List<String> strings) {

                                if (findex % 1000 == 0) {
                                    count+= 1000;
                                }
                            }
                        });

                    }
                }
            });

            threads.add(thread);
            thread.start();
        }


        final double start = System.currentTimeMillis();
        while (count < 10_000_000) {
            for (int index = 0; index < 10; index++) {
                Sys.sleep(100);

                if (count > 10_000_000) {
                    break;
                }
            }
            double now = System.currentTimeMillis();
            double c = count;
            System.out.println((c / (now - start)) * 1000);
        }

        System.out.println("Done");
    }

    interface SimpleServiceProxy {
        void ping(Callback<List<String>> doubleReturn);
    }

}

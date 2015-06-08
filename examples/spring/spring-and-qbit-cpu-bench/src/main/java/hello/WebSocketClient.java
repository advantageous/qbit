package hello;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.reactive.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * created by rhightower on 2/3/15.
 */
@SuppressWarnings("ALL")
public class WebSocketClient {

    static LongAdder count = new LongAdder();

    public static void main(String... args) {

        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6060;


        List<Thread> threads = new ArrayList<>();


        for (int numThreads = 0; numThreads < 10; numThreads++) {
            final Client client = new ClientBuilder().setPoolSize(10).setPort(port).setHost(host).setRequestBatchSize(10_000)
                    .build();

            client.start();


            Thread thread = new Thread(new Runnable() {


                final SimpleServiceProxy myService = client.createProxy(SimpleServiceProxy.class, "myServiceQBit");

                @Override
                public void run() {

                    int key = 0;
                    for (int index = 0; index < 10_100_000; index++, key++) {


                        if (key > 10) {
                            key = -1;
                        }

                        myService.ping(new Callback<List<String>>() {
                            @Override
                            public void accept(List<String> strings) {
                                count.increment();
                            }
                        });

                        if (index % 15_000 == 0) {
                            Sys.sleep(50);
                        }
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }


        final double start = System.currentTimeMillis();
        while (count.longValue() < 10_000_000) {
            for (int index = 0; index < 10; index++) {
                Sys.sleep(100);

                if (count.longValue() > 10_000_000) {
                    break;
                }
            }
            double now = System.currentTimeMillis();
            double c = count.doubleValue();
            System.out.println((c / (now - start)) * 1000);
        }

        System.out.println("Done");
    }

    interface SimpleServiceProxy {
        void ping(Callback<List<String>> doubleReturn);
    }

}

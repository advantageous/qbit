package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;

import java.util.ArrayList;
import java.util.List;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/2/15.
 */
public class SimpleWebSocketClient {

    interface  SimpleServiceProxy {

        void ping(Callback<List<String>> callback);

    }

    static volatile int count=0;

    public static void main(String... args) {

        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6060;


        List<Thread> threads = new ArrayList<>();



        for (int numThreads=0; numThreads < 2; numThreads++) {
            final Client client = new ClientBuilder().setPoolSize(1).setPort(port).setHost(host).setRequestBatchSize(10_000)
                    .build();

            client.start();


            Thread thread = new Thread(new Runnable() {



                final SimpleServiceProxy myService = client.createProxy(SimpleServiceProxy.class, "myService");

                @Override
                public void run() {

                    for (int index = 0; index < 11_000_000; index++) {
                        myService.ping(strings -> {
                            count++;
                        });

                        if (index % 15_000 == 0) {
                            Sys.sleep(10);
                        }
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }



        double start = System.currentTimeMillis();
        while (count < 10_000_000) {
            for (int index = 0; index< 10 ; index++) {
                Sys.sleep(100);

                if (count > 10_000_000) {
                    break;
                }
            }
            double now = System.currentTimeMillis();
            double c = count;
            puts(count, (c / (now - start)*1000));
        }

        puts("Done");
    }
}

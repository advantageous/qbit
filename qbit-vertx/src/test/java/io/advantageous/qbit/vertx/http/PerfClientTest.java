package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.*;
import org.boon.core.Sys;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 11/12/14.
 */
public class PerfClientTest {


    private static volatile LongAdder errorCount = new LongAdder();

    private static volatile LongAdder receivedCount = new LongAdder();

    private static final  int REQUEST_COUNT = 5_000_000;


    private static final  int CLIENT_COUNT = 100;

    public static void main(String... args) throws InterruptedException {

        final long startTime;

        final HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder();

        final HttpRequest perfRequest = httpRequestBuilder
                                        .setContentType("application/json")
                                        .setMethod("GET").setUri("/perf/")
                                        .setResponse((code, mimeType, body) -> {
                                            if (code != 200 || !body.equals("\"ok\"")) {
                                                errorCount.increment();
                                                return;
                                            }

                                            receivedCount.increment();


                                        })
                                        .build();


        final int countPerThread = REQUEST_COUNT / CLIENT_COUNT;

        final List<Thread> threads = new ArrayList<>(CLIENT_COUNT);



        for (int threadNum=0; threadNum< CLIENT_COUNT; threadNum++) {
            final HttpClientVertx client = new HttpClientVertx(9090, "localhost", false);
            client.run();


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index<countPerThread; index++) {
                         client.sendHttpRequest(perfRequest);
                         if (index % 10 == 0) {
                             client.flush();
                         }

                    }


                    Sys.sleep(100_000);
                    client.stop();
                }
            });

            threads.add(thread);


        }




        for (Thread t : threads) {

            Sys.sleep(3);
            t.start();
        }


        startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            if (receivedCount.sum() + errorCount.sum() >= REQUEST_COUNT) {
                long duration = System.currentTimeMillis() - startTime;
                puts("DURATION", duration/1000, "Recieved Count", receivedCount);
                break;

            }

            if (i % 10 == 0) {
                long duration = System.currentTimeMillis() - startTime;
                puts("DURATION", duration/1000, "count", receivedCount, "errors", errorCount);
            }
            Sys.sleep(10_000);

        }


        for (Thread t : threads) {
            t.join();
        }





        Sys.sleep(1000);
        puts("\n\nerror count ", errorCount, "\nreceived count", receivedCount);


        Sys.sleep(1000);
        puts("\n\nerror count ", errorCount, "\nreceived count", receivedCount);

        Sys.sleep(2000);
        puts("\n\nerror count ", errorCount, "\nreceived count", receivedCount);

        long duration = System.currentTimeMillis() - startTime;
        puts("DURATION", duration/1000, "Recieved Count", receivedCount);

        System.exit(0);


    }
}

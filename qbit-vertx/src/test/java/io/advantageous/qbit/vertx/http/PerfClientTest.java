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

    private static final  int REQUEST_COUNT = 500_000;


    private static final  int CLIENT_COUNT = 2;

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


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    //final HttpClientVertx client = new HttpClientVertx(9090, "localhost", false);

                    final HttpClient client = new HttpClientBuilder().setPort(9090).setHost("localhost")
                            .setPoolSize(100).setRequestBatchSize(10).build();
                    client.run();

                    Sys.sleep(5000);

                    for (int index = 0; index<countPerThread; index++) {
                         client.sendHttpRequest(perfRequest);

                    }

                    client.flush();


                    Sys.sleep(100_000);
                    client.stop();
                }
            });

            threads.add(thread);


        }




        for (Thread t : threads) {

            t.start();
        }

        Sys.sleep(5000);

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            if (receivedCount.sum() + errorCount.sum() >= REQUEST_COUNT - 100) {
                long duration = System.currentTimeMillis() - startTime;
                puts("DURATION", duration/1000, "Recieved Count", receivedCount);
                break;

            }

            if (i % 10 == 0) {
                long duration = System.currentTimeMillis() - startTime;
                puts("DURATION", duration/1000, "count", receivedCount, "errors", errorCount);
            }
            Sys.sleep(100);

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

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
public class PerfTest {


    private static volatile LongAdder errorCount = new LongAdder();

    private static volatile LongAdder receivedCount = new LongAdder();

    private static final  int REQUEST_COUNT = 10_000_000;


    private static final  int CLIENT_COUNT = 100;

    public static void main(String... args) {

        final long startTime;

        final long duration;
        final HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder();
        List<HttpClient> clientList = new ArrayList<>(CLIENT_COUNT);

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

        for (int index=0; index< CLIENT_COUNT; index++) {
            clientList.add(new HttpClientVertx(9090, "localhost", false));
        }


        for (HttpClient client : clientList) {
            client.run();
        }

        Sys.sleep(1000);


        int index=0;


        startTime = System.currentTimeMillis();

        for (; index<REQUEST_COUNT; index++) {


            for (HttpClient client : clientList) {
                client.sendHttpRequest(perfRequest);

                if (index % 100 == 0) {
                    client.flush();
                }
            }



            if (index % 100_000 == 0) {
                puts("\n\nrequests sent ", index, "\nerror count ", errorCount, "\nreceived count", receivedCount,
                        "\nduration", System.currentTimeMillis() - startTime);
            }


            while (receivedCount.longValue() + 100_000 < index  ) {
                Sys.sleep(1);
            }
        }

        for (HttpClient client : clientList) {
                client.flush();
        }



        for (int i = 0; i < 100; i++) {
            if (receivedCount.sum() + errorCount.sum() >= REQUEST_COUNT) {
                duration = System.currentTimeMillis() - startTime;
                puts("DURATION", duration);
                break;

            }
            Sys.sleep(100);
            puts("\n\nRequests sent ", index, "\nerror count ", errorCount, "\nreceived count", receivedCount);

        }



        Sys.sleep(1000);
        puts("\n\nRequests sent ", index, "\nerror count ", errorCount, "\nreceived count", receivedCount);


        Sys.sleep(1000);
        puts("\n\nRequests sent ", index, "\nerror count ", errorCount, "\nreceived count", receivedCount);

        Sys.sleep(2000);
        puts("\n\nRequests sent ", index, "\nerror count ", errorCount, "\nreceived count", receivedCount);

    }
}

package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpTextResponse;
import org.boon.core.Sys;

import static io.advantageous.qbit.http.HttpClientBuilder.httpClientBuilder;
import static io.advantageous.qbit.http.HttpRequestBuilder.httpRequestBuilder;
import static org.boon.Boon.puts;
import static org.boon.core.Sys.sleep;

/**
 * Created by rhightower on 2/2/15.
 */
public class CPUIntensiveServerClient {


    static volatile int count = 0;

    public static void main(String... args) throws Exception {


        final HttpClient httpClient = httpClientBuilder()
                .setPort(6060).setPoolSize(500).setRequestBatchSize(100).setPipeline(true).setKeepAlive(true)
                .build().start();

        sleep(1_000);


        final long start = System.currentTimeMillis();





        for (int index =  0; index < 500_000; index++) {

            final String key = "" + (index % 10);

            final HttpRequest httpRequestCPUKey1 = httpRequestBuilder().setUri("/services/myservice/addkey/").setTextResponse(new HttpTextResponse() {
                @Override
                public void response(int code, String mimeType, String body) {

                    count++;
                }
            }).addParam("key", key).addParam("value", "hi mom ").build();

            httpClient.sendHttpRequest(httpRequestCPUKey1);
        }

        httpClient.flush();
        //while (true) sleep(1000);




        while (count < 490_000) {
            sleep(100);
            if (count > 100) {
                puts(count);
            }
            if (count > 500) {
                puts(count);
            }

            if (count > 750) {
                puts(count);
            }

            if (count > 950) {
                puts(count);
            }
            if (count > 2000) {
                puts(count);
            }

            if (count > 10_000) {
                puts(count);
            }

            if (count > 15_000) {
                puts(count);
            }


            if (count > 100_000) {
                puts(count);
            }


            if (count > 300_000) {
                puts(count);
            }


            if (count > 400_000) {
                puts(count);
            }


            if (count > 450_000) {
                puts(count);
            }

        }


        final long stop = System.currentTimeMillis();

        puts(count, stop - start);
    }
}


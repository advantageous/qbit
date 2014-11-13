package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.HttpServerBuilder;
import org.boon.core.Sys;

/**
 * Created by Richard on 11/12/14.
 */
public class PerfServerTest {





    public static void main(String... args) {



        final HttpServer server = new HttpServerBuilder()
                                    .setPort(9090)
                                    .setHost("localhost").setRequestBatchSize(50)
                                    .setFlushInterval(100)
        .setHttpRequestConsumer(request -> {

            if (request.getUri().equals("/perf/")) {
                request.getResponse().response(200, "application/json", "\"ok\"");
            }
        }).build();


        server.run();


        Sys.sleep(1000);



        Sys.sleep(1_000 * 1_000);
    }
}

package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.HttpServerBuilder;
import org.boon.Boon;


/**
 *

 engine1:wrk rhightower$ ./wrk -c 500 -d 10s http://localhost:9090/1 -H "X_RDIO_USER_ID: RICK"  --timeout 100000s -t 8
 */
public class WebServerSimpleApplication {


    public static void main(final String... args) throws Exception {


        final HttpServer httpServer = new HttpServerBuilder().setPort(9090)
                .setFlushInterval(500)
                .setPollTime(20)
                .setRequestBatchSize(40)
                .setManageQueues(true)
                .setHttpRequestConsumer(request -> {

                    request.getResponse().response(200, "application/json", "\"ok\"");
                })
                .build();

        httpServer.start();

        Boon.gets();
    }

}


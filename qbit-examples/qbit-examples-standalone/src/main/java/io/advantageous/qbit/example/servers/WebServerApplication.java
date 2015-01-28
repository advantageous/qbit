package io.advantageous.qbit.example.servers;


import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.HttpServerBuilder;
import org.boon.Boon;

import java.util.function.Consumer;


/*
engine1:wrk rhightower$ ./wrk -c 500 -d 10s http://localhost:9090/1 -H "X_RDIO_USER_ID: RICK"  --timeout 100000s -t 8
engine1:wrk rhightower$ ./wrk -c 500 -d 10s http://localhost:7070/1 -H "X_RDIO_USER_ID: RICK"  --timeout 100000s -t 8
 */

public class WebServerApplication {

    public static class HttpSampleVerticle  implements Consumer<HttpServer> {

        @Override
        public void accept(HttpServer httpServer) {


            httpServer.setHttpRequestConsumer(request -> request.getResponse().response(200, "application/json", "\"ok\""));


        }
    }


    public static void main(final String... args) throws Exception {




        final HttpServer httpServer = new HttpServerBuilder().setPort(8080)
                .setFlushInterval(500)
                .setPollTime(20)
                .setRequestBatchSize(40)
                .setManageQueues(false)
                .setWorkers(4).setHandlerClass(HttpSampleVerticle.class)
                .build();

        httpServer.start();

        Boon.gets();
    }


}
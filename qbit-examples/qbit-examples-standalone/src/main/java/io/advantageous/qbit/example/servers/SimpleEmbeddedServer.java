package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.vertx.builders.ServiceServerVertxEmbeddedBuilder;
import org.boon.Boon;

import java.util.function.Consumer;

/**
 * Created by rhightower on 1/27/15.
 *
 * ./wrk -c 4000 -d 10s http://localhost:5050/services/myservice/ping -H "X_USER_ID: RICK"  --timeout 100000s -t 8
 Running 10s test @ http://localhost:5050/services/myservice/ping
 8 threads and 4000 connections
 Thread Stats   Avg      Stdev     Max   +/- Stdev
 Latency    66.23ms   84.40ms 228.61ms   78.30%
 Req/Sec     5.26k     3.04k    9.03k    68.50%
 396551 requests in 10.03s, 35.17MB read
 Socket errors: connect 0, read 300, write 0, timeout 0
 Requests/sec:  39528.71
 Transfer/sec:      3.51MB
 */
public class SimpleEmbeddedServer {



    public static class MyService {

        /*
        curl http://localhost:6060/services/myservice/ping -H "X_USER_ID: RICK"
         */
        @RequestMapping
        public String ping() {
            return "ping";
        }
    }


    public static class BeforeHandler implements Consumer<ServiceBundle> {

        @Override
        public void accept(ServiceBundle serviceBundle) {

            serviceBundle.addService(new MyService());
        }
    }


    public static void main(String... args) throws Exception {


        final ServiceServer serviceServer = new ServiceServerVertxEmbeddedBuilder()
                .setBeforeStartHandler(BeforeHandler.class).setHttpWorkers(4).setPort(5050)
                .setRequestBatchSize(40).setMaxRequestBatches(10).build();

        serviceServer.start();

        Boon.gets();
    }

}

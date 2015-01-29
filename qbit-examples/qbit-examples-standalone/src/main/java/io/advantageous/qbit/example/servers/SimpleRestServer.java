package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import org.boon.Boon;

import java.util.Collections;
import java.util.List;

/**
 *
 * ./wrk -c 500 -d 10s http://localhost:6060/services/myservice/ping -H "X_USER_ID: RICK"  --timeout 100000s -t 8
 *
 * ./wrk -c 200 -d 10s http://localhost:6060/services/myservice/ping -H "X_USER_ID: RICK"  --timeout 100000s -t 8
 Running 10s test @ http://localhost:6060/services/myservice/ping
 8 threads and 200 connections
 Thread Stats   Avg      Stdev     Max   +/- Stdev
 Latency    28.48ms   70.52ms 222.81ms   88.36%
 Req/Sec     9.17k     3.66k   12.78k    86.13%
 689411 requests in 10.00s, 61.15MB read
 Requests/sec:  68944.98
 Transfer/sec:      6.11MB
 */
public class SimpleRestServer {


    public static class MyService {

        /*
        curl http://localhost:6060/services/myservice/ping -H "X_USER_ID: RICK"
         */
        @RequestMapping
        public List ping() {
            return Collections.singletonList("Hello World!");
        }
    }

    public static void main(String... args) throws Exception {


        final ServiceServer serviceServer = new ServiceServerBuilder().setPort(6060)
                .setRequestBatchSize(40).setMaxRequestBatches(10).build();

        serviceServer.initServices(new MyService());
        serviceServer.start();

        Boon.gets();
    }
}

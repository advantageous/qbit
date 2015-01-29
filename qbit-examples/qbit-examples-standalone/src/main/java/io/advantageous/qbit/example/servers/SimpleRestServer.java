package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import org.boon.Boon;

import java.util.Collections;
import java.util.List;

/**
 *

 $ ./wrk -c 200 -d 10s http://localhost:6060/services/myservice/ping -H "X_USER_ID: RICK"  --timeout 100000s -t 8
 Running 10s test @ http://localhost:6060/services/myservice/ping
 8 threads and 200 connections
 Thread Stats   Avg      Stdev     Max   +/- Stdev
 Latency     2.63ms  360.02us   4.41ms   68.14%
 Req/Sec    10.17k     1.32k   12.89k    55.97%
 766450 requests in 10.00s, 76.75MB read
 Requests/sec:  76651.84
 Transfer/sec:      7.68MB

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


        final ServiceServer serviceServer = new ServiceServerBuilder().setPort(6060).setQueueBuilder(
                new QueueBuilder().setLinkTransferQueue().setTryTransfer(true).setBatchSize(10).setPollWait(10)
        ).setNumberOfOutstandingRequests(1_000_000).setTimeoutSeconds(40)
                .build();

        serviceServer.initServices(new MyService());
        serviceServer.start();

        Boon.gets();
    }
}

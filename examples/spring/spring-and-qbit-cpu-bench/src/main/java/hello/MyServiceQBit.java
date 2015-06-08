package hello;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;

import java.util.Collections;
import java.util.List;

import static io.advantageous.qbit.server.EndpointServerBuilder.endpointServerBuilder;

/**
 * Example of a QBit Service
 * <p>
 * created by rhightower on 2/2/15.
 */
@RequestMapping("/myservice")
public class MyServiceQBit {

    final ActualService actualService = new ActualService();
    int count = 0;

    public static void main(String... args) throws Exception {


        final ServiceEndpointServer serviceEndpointServer = endpointServerBuilder()
                .setPort(6060).build();

        serviceEndpointServer.initServices(new MyServiceQBit());
        serviceEndpointServer.start();

    }

    @RequestMapping("/ping")
    public List<String> ping() {
        return Collections.singletonList("Hello World!");
    }
}

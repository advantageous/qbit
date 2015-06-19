package hello;


import io.advantageous.qbit.annotation.RequestMapping;

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

    @RequestMapping("/ping")
    public List<String> ping() {
        return Collections.singletonList("Hello World!");
    }

    public static void main(String... args) throws Exception {

//
//        System.setProperty("vertx.pool.worker.size", System.getProperty("vertx.pool.worker.size",
//                String.valueOf(200)));
//        System.setProperty("vertx.pool.eventloop.size", System.getProperty("vertx.pool.eventloop.size",
//                String.valueOf((Runtime.getRuntime().availableProcessors() * 4))));
//

        endpointServerBuilder()
                .setPort(6060)
                .build()
                .initServices(new MyServiceQBit()).startServer();
    }

}

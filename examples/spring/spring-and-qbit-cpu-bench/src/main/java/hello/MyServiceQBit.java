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
        endpointServerBuilder()
                .setPort(6060)
                .build()
                .initServices(new MyServiceQBit()).startServer();
    }

}

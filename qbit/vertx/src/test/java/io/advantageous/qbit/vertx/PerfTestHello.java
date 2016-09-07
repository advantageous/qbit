package io.advantageous.qbit.vertx;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.server.EndpointServerBuilder;

@RequestMapping("/")
public class PerfTestHello {


    @GET
    public String hello() {
        return "hello";
    }

    public static void main(final String... args) {
        final EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();
        endpointServerBuilder.getRequestQueueBuilder().setBatchSize(1);
        endpointServerBuilder.getResponseQueueBuilder().setBatchSize(1);

        endpointServerBuilder.setFlushResponseInterval(10)
                .setUri("/").setPort(9666)
                .addService(new PerfTestHello()).build().startServer();

    }
}

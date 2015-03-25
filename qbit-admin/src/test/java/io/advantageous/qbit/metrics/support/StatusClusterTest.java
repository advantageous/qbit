package io.advantageous.qbit.metrics.support;

import io.advantageous.boon.core.Sys;
import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.metrics.StatService;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;
import io.advantageous.qbit.util.PortUtils;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;
import static io.advantageous.qbit.metrics.support.StatServiceBuilder.statServiceBuilder;

/**
 * StatusClusterTest
 * Created by rhightower on 3/24/15.
 */
public class StatusClusterTest {

    public static void main(String... args) throws Exception {

        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder();

        final ServiceDiscoveryImpl serviceDiscovery = consulServiceDiscoveryBuilder.build();
        serviceDiscovery.start();


        final StatServiceBuilder statServiceBuilder = statServiceBuilder()
                .setServiceDiscovery(serviceDiscovery);

        int port = PortUtils.useOneOfThePortsInThisRange(8900, 9000);
        statServiceBuilder.getServiceServerBuilder().setPort(port);
        final ServiceServer serviceServer = statServiceBuilder.buildServiceServer();


        serviceServer.start();

        final StatService statService = serviceServer.serviceBundle().createLocalProxy(StatService.class, statServiceBuilder.getServiceName());



        for (int index = 0; index < 100; index++) {
            Sys.sleep(1000);
            System.out.print(".");
            statService.increment("foo");
            statService.currentMinuteCount(count -> System.out.println(sputs("count", count)), "foo");
            ServiceProxyUtils.flushServiceProxy(statService);
        }



    }


}

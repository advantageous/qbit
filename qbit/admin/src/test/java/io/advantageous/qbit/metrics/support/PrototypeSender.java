package io.advantageous.qbit.metrics.support;

import io.advantageous.boon.core.Sys;
import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.metrics.StatService;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.qbit.util.Timer;

import static io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;
import static io.advantageous.qbit.metrics.support.StatServiceBuilder.statServiceBuilder;

/**
 * StatusClusterTest
 * created by rhightower on 3/24/15.
 */
public class PrototypeSender {

    public static void main(String... args) throws Exception {

        final EventManager eventManager = QBit.factory().systemEventManager();

        final EventBusProxyCreator eventBusProxyCreator = QBit.factory().eventBusProxyCreator();

        final ServiceChangedEventChannel servicePoolUpdate = eventBusProxyCreator
                .createProxy(eventManager, ServiceChangedEventChannel.class);


        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder()
                .setServiceChangedEventChannel(servicePoolUpdate);

        final ServiceDiscoveryImpl serviceDiscovery = consulServiceDiscoveryBuilder.build();
        serviceDiscovery.start();


        final StatServiceBuilder statServiceBuilder = statServiceBuilder()
                .setServiceDiscovery(serviceDiscovery).setEventManager(eventManager);

        //Not getting service updates when we join a cluster.

        int port = PortUtils.useOneOfThePortsInThisRange(8900, 9000);
        statServiceBuilder.getEndpointServerBuilder().setPort(port);
        final ServiceEndpointServer serviceEndpointServer = statServiceBuilder.buildServiceServer();


        serviceEndpointServer.start();

        final StatService statService = serviceEndpointServer.serviceBundle().createOneWayLocalProxy(StatService.class,
                statServiceBuilder.getServiceName());


        for (int x = 0; x < 100; x++) {

            for (int z = 0; z < 10; z++) {

                //final Timer timer =
                long startTime = Timer.timer().time();
                for (int index = 0; index < 6_000_000; index++) {

                    if (index % 1_000_000 == 0) {
                        System.out.print("." + (Timer.timer().time() - startTime));
                    }

                    if (index % 10 == 0) {
                        statService.recordCount("foo", 10);
                        statService.recordCount("bar", 10);
                        statService.recordCount("baz", 10);
                    }


                }


                System.out.println("\nSENT A BUNCH");

                ServiceProxyUtils.flushServiceProxy(statService);
                Sys.sleep(5_000);

            }


        }


        ServiceProxyUtils.flushServiceProxy(statService);


    }


}

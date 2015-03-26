package io.advantageous.qbit.metrics.support;

import io.advantageous.boon.core.Sys;
import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.metrics.StatService;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.HealthStatus;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;
import io.advantageous.qbit.service.discovery.ServiceDefinition;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.qbit.util.Timer;

import java.util.List;
import java.util.function.Consumer;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;
import static io.advantageous.qbit.metrics.support.StatServiceBuilder.statServiceBuilder;

/**
 * StatusClusterTest
 * Created by rhightower on 3/24/15.
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
        statServiceBuilder.getServiceServerBuilder().setPort(port);
        final ServiceServer serviceServer = statServiceBuilder.buildServiceServer();


        serviceServer.start();

        final StatService statService = serviceServer.serviceBundle().createLocalProxy(StatService.class,
                statServiceBuilder.getServiceName());

        List<ServiceDefinition> serviceDefinitions = serviceDiscovery.loadServices(statServiceBuilder.getServiceName());

        serviceDefinitions.forEach(serviceDefinition -> puts(serviceDefinition));

        puts("Service statServiceBuilder.getLocalServiceId()", statServiceBuilder.getLocalServiceId());

        serviceDiscovery.checkIn(statServiceBuilder.getLocalServiceId(), HealthStatus.PASS);


        for (int x = 0; x < 100; x++) {

            for (int z = 0; z < 10; z++) {

                //final Timer timer =
                long startTime =  System.currentTimeMillis();
                for (int index = 0; index < 6_000_000; index++) {

                    if (index % 1_000_000 == 0) {
                        Sys.sleep(1);
                        System.out.print("." + (System.currentTimeMillis()-startTime));
                    }

                    if (index % 10 == 0) {
                        statService.recordCount("foo", 10);
                        statService.recordCount("bar", 10);
                        statService.recordCount("baz", 10);
                    }


                }


                System.out.println("SENT A BUNCH");

                ServiceProxyUtils.flushServiceProxy(statService);
                Sys.sleep(10_000);

            }


        }



        for (int index = 0; index < 10; index++) {

            final int fromIndex = index;
            Sys.sleep(1000);
            serviceDiscovery.checkIn(statServiceBuilder.getLocalServiceId(), HealthStatus.PASS);
            System.out.print(".");
            statService.increment("foo");

            statService.currentMinuteCount(count -> System.out.print(
                    "count " + count + " index " + fromIndex + "    "), "foo");

            if (index % 10 == 0) {
                ServiceProxyUtils.flushServiceProxy(statService);
                serviceDefinitions = serviceDiscovery.loadServices(statServiceBuilder.getServiceName());
                serviceDefinitions.forEach(serviceDefinition -> System.out.print(" " + serviceDefinition + " "));
            }


        }


        ServiceProxyUtils.flushServiceProxy(statService);
        serviceDefinitions = serviceDiscovery.loadServices(statServiceBuilder.getServiceName());
        serviceDefinitions.forEach(serviceDefinition -> puts(serviceDefinition));



        int index = 0;

        while (true) {

            index ++;


            statService.lastTenSecondCountExact(count -> System.out.println(
                    "count " + count + " index " +  "    "), "foo");

            ServiceProxyUtils.flushServiceProxy(statService);

            Sys.sleep(1000);

            if (index % 10 == 0)
                serviceDiscovery.checkIn(statServiceBuilder.getLocalServiceId(), HealthStatus.PASS);

        }

    }


}

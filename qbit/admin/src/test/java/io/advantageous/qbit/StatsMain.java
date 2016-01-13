package io.advantageous.qbit;

import io.advantageous.boon.core.Sys;
import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.metrics.StatService;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.metrics.support.StatsDReplicatorBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;

public class StatsMain {

    public static void main(String... args) {
        ServiceEndpointServer server = startStatService(11500);

        final ServiceBundle serviceBundle = server.serviceBundle();

        final StatService statService = serviceBundle.createLocalProxy(StatService.class, "stat-service");

        for (int index = 0; index < 100; index++) {
            statService.recordCount("abc.count", index);
            statService.recordLevel("abc.level", index);
            Sys.sleep(1000);

            serviceBundle.flushSends();
            statService.lastTenSecondCount(new Callback<Long>() {
                @Override
                public void accept(Long integer) {
                    puts("Last ten second count for abc.count", integer);
                }
            }, "abc.count");

            statService.averageLastLevel(new Callback<Long>() {
                @Override
                public void accept(Long integer) {
                    puts("Average level  second count for abc.level", integer);
                }
            }, "abc.level", 10);
        }
    }

    public static ServiceEndpointServer startStatService(int statsPort) {

        final Factory factory = QBit.factory();
        final EventManager eventManager = factory.systemEventManager();
        final EventBusProxyCreator eventBusProxyCreator = factory.eventBusProxyCreator();
        final ServiceChangedEventChannel serviceChangedEventChannel = eventBusProxyCreator.createProxy(eventManager, ServiceChangedEventChannel.class);

        final StatReplicator statReplicator = StatsDReplicatorBuilder.statsDReplicatorBuilder()
                .setHost("192.168.59.103")
                .buildAndStart();


        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder();

        final ServiceDiscoveryImpl serviceDiscovery = consulServiceDiscoveryBuilder
                .setConsulHost("localhost")
                .setDatacenter("api-proxy")
                .setServiceChangedEventChannel(serviceChangedEventChannel)
                .build();


        serviceDiscovery.start();


        final StatServiceBuilder statServiceBuilder = StatServiceBuilder.statServiceBuilder()
                .setTimeToLiveCheckInterval(1_000)
                .setEventManager(eventManager)

                .addReplicator(statReplicator)
                .setServiceDiscovery(serviceDiscovery);

        statServiceBuilder.getEndpointServerBuilder()
                .setPort(statsPort);

        statServiceBuilder.setServiceName("stat-service");


        final ServiceEndpointServer statServiceServer = statServiceBuilder.buildServiceServer();
        statServiceServer.start();

        return statServiceServer;

    }

}

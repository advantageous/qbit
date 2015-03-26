package io.advantageous.qbit.metrics;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.metrics.support.DebugReplicator;
import io.advantageous.qbit.service.discovery.ServiceDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.qbit.service.discovery.ServiceDefinition.serviceDefinition;
import static io.advantageous.qbit.service.discovery.ServiceDefinition.serviceDefinitions;
import static org.junit.Assert.*;

public class ClusteredStatReplicatorTest {



    private final String localServiceId = "fooBar-" + System.currentTimeMillis();
    private final String serviceName = "fooBar";

    ClusteredStatReplicator clusteredStatReplicator;

    AtomicReference<List<ServiceDefinition>> services;

    ConcurrentHashMap<String, DebugReplicator> statReplicatorMap;


    ServiceDiscovery serviceDiscovery = new ServiceDiscovery() {
        @Override
        public void watch(String serviceName) {

        }

        @Override
        public List<ServiceDefinition> loadServices(String serviceName) {
            return services.get() == null ? Collections.emptyList() : services.get();
        }
    };


    StatReplicatorProvider provider = new StatReplicatorProvider() {
        @Override
        public StatReplicator provide(ServiceDefinition serviceDefinition) {

            puts("Creating ", serviceDefinition);
            DebugReplicator debugReplicator =  new DebugReplicator(true);
            statReplicatorMap.put(serviceDefinition.getId(), debugReplicator);
            return debugReplicator;
        }
    };

    @Before
    public void setup() throws Exception {

        services = new AtomicReference<>();
        statReplicatorMap = new ConcurrentHashMap<>();

        clusteredStatReplicator = new ClusteredStatReplicator(serviceName, serviceDiscovery, provider, localServiceId);

    }

    @Test
    public void testDiscovery() {


        ServiceDefinition serviceDefinition1 = serviceDefinition(serviceName, "host1");
        ServiceDefinition serviceDefinition2 = serviceDefinition(serviceName, "host2");
        ServiceDefinition serviceDefinition3 = serviceDefinition(serviceName, "host3");


        List<ServiceDefinition> fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition2,
                serviceDefinition3
        );

        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        Sys.sleep(100);

        final DebugReplicator debugReplicator1 = statReplicatorMap.get(serviceDefinition1.getId());
        final DebugReplicator debugReplicator2 = statReplicatorMap.get(serviceDefinition1.getId());
        final DebugReplicator debugReplicator3 = statReplicatorMap.get(serviceDefinition1.getId());

        assertNotNull(debugReplicator1);
        assertNotNull(debugReplicator2);
        assertNotNull(debugReplicator3);

    }


    @Test
    public void testDiscoveryAndSends() {


        ServiceDefinition serviceDefinition1 = serviceDefinition(serviceName, "host1");
        ServiceDefinition serviceDefinition2 = serviceDefinition(serviceName, "host2");
        ServiceDefinition serviceDefinition3 = serviceDefinition(serviceName, "host3");


        List<ServiceDefinition> fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition2,
                serviceDefinition3
        );

        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        Sys.sleep(100);

        final DebugReplicator debugReplicator1 = statReplicatorMap.get(serviceDefinition1.getId());
        final DebugReplicator debugReplicator2 = statReplicatorMap.get(serviceDefinition1.getId());
        final DebugReplicator debugReplicator3 = statReplicatorMap.get(serviceDefinition1.getId());

        assertNotNull(debugReplicator1);
        assertNotNull(debugReplicator2);
        assertNotNull(debugReplicator3);

        clusteredStatReplicator.replicateCount("foo", 5, System.currentTimeMillis());

        assertEquals(5, debugReplicator1.count.get());
        assertEquals(5, debugReplicator2.count.get());
        assertEquals(5, debugReplicator3.count.get());


    }



    @Test
    public void testDiscoveryAndSendsAndRemove() {


        ServiceDefinition serviceDefinition1 = serviceDefinition(serviceName, "host1");
        ServiceDefinition serviceDefinition2 = serviceDefinition(serviceName, "host2");
        ServiceDefinition serviceDefinition3 = serviceDefinition(serviceName, "host3");
        ServiceDefinition localService = serviceDefinition(localServiceId, serviceName, "host3", 0);


        List<ServiceDefinition> fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition2,
                serviceDefinition3,
                localService
        );

        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        Sys.sleep(100);

        final DebugReplicator debugReplicator1 = statReplicatorMap.get(serviceDefinition1.getId());
        final DebugReplicator debugReplicator2 = statReplicatorMap.get(serviceDefinition2.getId());
        final DebugReplicator debugReplicator3 = statReplicatorMap.get(serviceDefinition3.getId());

        assertNotNull(debugReplicator1);
        assertNotNull(debugReplicator2);
        assertNotNull(debugReplicator3);

        clusteredStatReplicator.replicateCount("foo", 5, 100);

        assertEquals(5, debugReplicator1.count.get());
        assertEquals(5, debugReplicator2.count.get());
        assertEquals(5, debugReplicator3.count.get());


        fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition3
        );



        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        clusteredStatReplicator.servicePoolChanged("foo");


        Sys.sleep(200);



        clusteredStatReplicator.replicateCount("foo", 5, 200);
        Sys.sleep(100);


        assertEquals(10, debugReplicator1.count.get());
        assertEquals(5, debugReplicator2.count.get());
        assertEquals(10, debugReplicator3.count.get());

        clusteredStatReplicator.flush();


    }


}
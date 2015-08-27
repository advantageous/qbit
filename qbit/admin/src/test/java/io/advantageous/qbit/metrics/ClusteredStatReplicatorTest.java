package io.advantageous.qbit.metrics;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.metrics.support.DebugReplicator;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.service.discovery.EndpointDefinition.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClusteredStatReplicatorTest {

    private final String localServiceId = "fooBar-" + Clock.systemUTC().millis();
    private final String serviceName = "fooBar";
    private ClusteredStatReplicator clusteredStatReplicator;
    private AtomicReference<List<EndpointDefinition>> services;
    private ConcurrentHashMap<String, DebugReplicator> statReplicatorMap;

    private TestTimer timer = new TestTimer();
    private ServiceDiscovery serviceDiscovery = new ServiceDiscovery() {
        @Override
        public void watch(String serviceName) {

        }

        @Override
        public List<EndpointDefinition> loadServices(String serviceName) {
            return services.get() == null ? Collections.emptyList() : services.get();
        }
    };
    private StatReplicatorProvider provider = new StatReplicatorProvider() {
        @Override
        public StatReplicator provide(EndpointDefinition endpointDefinition) {

            puts("Creating ", endpointDefinition);
            DebugReplicator debugReplicator = new DebugReplicator(true);
            statReplicatorMap.put(endpointDefinition.getId(), debugReplicator);
            return debugReplicator;
        }
    };

    @Before
    public void setup() throws Exception {

        services = new AtomicReference<>();
        statReplicatorMap = new ConcurrentHashMap<>();

        timer.setTime();

        clusteredStatReplicator = new ClusteredStatReplicator(serviceName, serviceDiscovery,
                provider, localServiceId, timer, 100, 333);

    }

    @Test
    public void testDiscovery() {


        EndpointDefinition endpointDefinition1 = serviceDefinition(serviceName, "host1");
        EndpointDefinition endpointDefinition2 = serviceDefinition(serviceName, "host2");
        EndpointDefinition endpointDefinition3 = serviceDefinition(serviceName, "host3");


        List<EndpointDefinition> fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition2,
                endpointDefinition3
        );

        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        Sys.sleep(100);

        final DebugReplicator debugReplicator1 = statReplicatorMap.get(endpointDefinition1.getId());
        final DebugReplicator debugReplicator2 = statReplicatorMap.get(endpointDefinition1.getId());
        final DebugReplicator debugReplicator3 = statReplicatorMap.get(endpointDefinition1.getId());

        assertNotNull(debugReplicator1);
        assertNotNull(debugReplicator2);
        assertNotNull(debugReplicator3);

    }


    @Test
    public void testDiscoveryAndSends() {


        EndpointDefinition endpointDefinition1 = serviceDefinition(serviceName, "host1");
        EndpointDefinition endpointDefinition2 = serviceDefinition(serviceName, "host2");
        EndpointDefinition endpointDefinition3 = serviceDefinition(serviceName, "host3");


        List<EndpointDefinition> fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition2,
                endpointDefinition3
        );

        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        Sys.sleep(100);

        final DebugReplicator debugReplicator1 = statReplicatorMap.get(endpointDefinition1.getId());
        final DebugReplicator debugReplicator2 = statReplicatorMap.get(endpointDefinition1.getId());
        final DebugReplicator debugReplicator3 = statReplicatorMap.get(endpointDefinition1.getId());

        assertNotNull(debugReplicator1);
        assertNotNull(debugReplicator2);
        assertNotNull(debugReplicator3);

        clusteredStatReplicator.replicateCount("foo", 5, Clock.systemUTC().millis());

        timer.seconds(2);
        clusteredStatReplicator.process();

        assertEquals(5, debugReplicator1.count.get());
        assertEquals(5, debugReplicator2.count.get());
        assertEquals(5, debugReplicator3.count.get());


    }


    @Test
    public void testDiscoveryAndSendsAndRemove() {


        EndpointDefinition endpointDefinition1 = serviceDefinitionWithId(serviceName, "host1",
                UUID.randomUUID().toString());
        EndpointDefinition endpointDefinition2 = serviceDefinitionWithId(serviceName, "host2",
                UUID.randomUUID().toString());
        EndpointDefinition endpointDefinition3 = serviceDefinitionWithId(serviceName, "host3",
                UUID.randomUUID().toString());
        EndpointDefinition localService = serviceDefinition(localServiceId, serviceName, "host3", 0);


        List<EndpointDefinition> fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition2,
                endpointDefinition3,
                localService
        );

        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        Sys.sleep(100);

        final DebugReplicator debugReplicator1 = statReplicatorMap.get(endpointDefinition1.getId());
        final DebugReplicator debugReplicator2 = statReplicatorMap.get(endpointDefinition2.getId());
        final DebugReplicator debugReplicator3 = statReplicatorMap.get(endpointDefinition3.getId());

        assertNotNull(debugReplicator1);
        assertNotNull(debugReplicator2);
        assertNotNull(debugReplicator3);

        clusteredStatReplicator.replicateCount("foo", 5, 100);


        timer.seconds(2);
        clusteredStatReplicator.process();

        assertEquals(5, debugReplicator1.count.get());
        assertEquals(5, debugReplicator2.count.get());
        assertEquals(5, debugReplicator3.count.get());


        fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition3
        );


        services.set(fooServices);

        clusteredStatReplicator.servicePoolChanged(serviceName);

        clusteredStatReplicator.servicePoolChanged("foo");


        Sys.sleep(200);


        clusteredStatReplicator.replicateCount("foo", 5, 200);
        Sys.sleep(100);


        timer.seconds(2);
        clusteredStatReplicator.process();

        assertEquals(10, debugReplicator1.count.get());
        assertEquals(5, debugReplicator2.count.get());
        assertEquals(10, debugReplicator3.count.get());

        clusteredStatReplicator.flush();


    }


}
package io.advantageous.consul;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.service.discovery.HealthStatus;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;
import static org.junit.Assert.assertEquals;

public class ConsulServiceDiscoveryTest {


    ServiceDiscoveryImpl discovery1;
    ServiceDiscoveryImpl discovery2;
    ServiceDiscoveryImpl discovery3;

    private final String serviceName = "FOO-BAR-";

    @Before
    public void setup() {

        discovery1 = consulServiceDiscoveryBuilder().build();
        discovery2 = consulServiceDiscoveryBuilder().build();
        discovery3 = consulServiceDiscoveryBuilder().build();

        discovery1.start();
        discovery2.start();
        discovery3.start();
    }


    @Test
    public void test() {
        final String id1 = discovery1.register(serviceName, 7000).getId();
        final String id2 = discovery2.register(serviceName, 8000).getId();
        final String id3 = discovery3.register(serviceName, 9000).getId();


        for (int index=0; index< 10; index++) {
            Sys.sleep(100);
            puts(discovery1.loadServices(serviceName));

            Sys.sleep(100);
            discovery1.checkIn(id1, HealthStatus.PASS);
            discovery2.checkIn(id2, HealthStatus.PASS);
            discovery3.checkIn(id3, HealthStatus.PASS);
        }


        Sys.sleep(100);
        List<EndpointDefinition> endpointDefinitions = discovery1.loadServices(serviceName);
        assertEquals(3, endpointDefinitions.size());


        for (int index=0; index< 10; index++) {
            Sys.sleep(100);
            discovery1.checkIn(id1, HealthStatus.PASS);
            discovery2.checkIn(id2, HealthStatus.FAIL);
            discovery3.checkIn(id3, HealthStatus.PASS);
        }

        endpointDefinitions = discovery1.loadServices(serviceName);
        assertEquals(2, endpointDefinitions.size());
    }
}
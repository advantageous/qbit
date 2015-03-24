package io.advantageous.consul;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.service.discovery.HealthStatus;
import io.advantageous.qbit.service.discovery.ServiceDefinition;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.consul.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;
import static org.junit.Assert.*;

public class ConsulServiceDiscoveryTest {


    ConsulServiceDiscovery discovery1;
    ConsulServiceDiscovery discovery2;
    ConsulServiceDiscovery discovery3;

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
        final String id1 = discovery1.registerService(serviceName, 7000).getId();
        final String id2 = discovery2.registerService(serviceName, 8000).getId();
        final String id3 = discovery3.registerService(serviceName, 9000).getId();




        for (int index=0; index< 10; index++) {
            Sys.sleep(1000);
            puts(discovery1.loadServices(serviceName));

            Sys.sleep(100);
            discovery1.checkIn(id1, HealthStatus.PASS);
            discovery2.checkIn(id2, HealthStatus.PASS);
            discovery3.checkIn(id3, HealthStatus.PASS);
        }
    }
}
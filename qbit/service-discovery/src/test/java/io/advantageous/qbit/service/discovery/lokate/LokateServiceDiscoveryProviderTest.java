package io.advantageous.qbit.service.discovery.lokate;

import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;


public class LokateServiceDiscoveryProviderTest {
    @Test
    public void loadServices() throws Exception {

        final ServiceDiscovery lokateServiceDiscovery = LokateServiceDiscoveryProvider.createLokateServiceDiscovery();
        lokateServiceDiscovery.start();
        final List<EndpointDefinition> endpointDefinitions = lokateServiceDiscovery.loadServicesNow("discovery:echo:http://foo.com:8080");
        assertEquals(1, endpointDefinitions.size());
        assertEquals("foo.com", endpointDefinitions.get(0).getHost());
        assertEquals(8080, endpointDefinitions.get(0).getPort());
    }

}
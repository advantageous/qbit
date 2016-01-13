package io.advantageous.qbit.example.websocket.service;

import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;

import java.util.List;

import static io.advantageous.qbit.service.discovery.EndpointDefinition.serviceDefinition;
import static io.advantageous.qbit.service.discovery.EndpointDefinition.serviceDefinitions;

public class ServiceDiscoveryMock implements ServiceDiscovery{

    @Override
    public List<EndpointDefinition> loadServicesNow(final String serviceName) {
        return serviceDefinitions(
                serviceDefinition("echo1", "echo", "localhost", 8080),
                serviceDefinition("echo1", "echo", "localhost", 9090)
                );
    }

    @Override
    public List<EndpointDefinition> loadServices(String serviceName) {
        return loadServicesNow(serviceName);
    }
}

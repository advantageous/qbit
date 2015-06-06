package io.advantageous.qbit.service.discovery;

/**
 * ServicePoolListener
 * created by rhightower on 3/23/15.
 */
public interface ServicePoolListener {

    default void serviceAdded(String serviceName, EndpointDefinition endpointDefinition) {
    }

    default void serviceRemoved(String serviceName, EndpointDefinition endpointDefinition) {
    }

    default void servicesAdded(String serviceName, int count) {
    }

    default void servicesRemoved(String serviceName, int count) {
    }

    void servicePoolChanged(String serviceName);

}

package io.advantageous.qbit.metrics;

import io.advantageous.qbit.service.discovery.EndpointDefinition;

/**
 * Replicator Provider
 * created by rhightower on 3/24/15.
 */
public interface StatReplicatorProvider {

    StatReplicator provide(final EndpointDefinition endpointDefinition);

}

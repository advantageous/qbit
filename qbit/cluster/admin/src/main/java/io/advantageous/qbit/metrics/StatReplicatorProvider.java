package io.advantageous.qbit.metrics;

import io.advantageous.qbit.service.discovery.ServiceDefinition;

/**
 * Replicator Provider
 * Created by rhightower on 3/24/15.
 */
public interface StatReplicatorProvider {

    StatReplicator provide(final ServiceDefinition serviceDefinition);

}

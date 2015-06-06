package io.advantageous.qbit.service.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service pool
 * created by rhightower on 3/23/15.
 */
public class ServicePool {

    private final String serviceName;


    private final AtomicReference<Map<String, EndpointDefinition>> pool =
            new AtomicReference<>(new ConcurrentHashMap<>());
    private final ServicePoolListener servicePoolListener;

    public ServicePool(final String serviceName,
                       final ServicePoolListener servicePoolListener) {
        this.serviceName = serviceName;
        this.servicePoolListener = servicePoolListener == null ?
                serviceNameChanged -> {

                } : servicePoolListener;
    }

    public List<EndpointDefinition> services() {
        return new ArrayList<>(pool.get().values());
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean setHealthyNodes(final List<EndpointDefinition> services) {

        return setHealthyNodes(services, this.servicePoolListener);
    }

    /**
     * @param services            services
     * @param servicePoolListener listens to service pool events
     * @return true if services have changed
     */
    public synchronized boolean setHealthyNodes(final List<EndpointDefinition> services,
                                                final ServicePoolListener servicePoolListener) {

        final Map<String, EndpointDefinition> oldMap = pool.get();
        final Map<String, EndpointDefinition> newMap = new ConcurrentHashMap<>(services.size());
        int oldServicesRemoved = 0;
        int newServices = 0;

        for (EndpointDefinition service : services) {
            if (!oldMap.containsKey(service.getId())) {
                newServices++;
                servicePoolListener.serviceAdded(serviceName, service);
            }
            newMap.put(service.getId(), service);
        }

        for (EndpointDefinition service : oldMap.values()) {
            if (!newMap.containsKey(service.getId())) {
                oldServicesRemoved++;
                servicePoolListener.serviceRemoved(serviceName, service);
                //log an old service was removed
            }
        }

        pool.set(newMap);

        return handleEvents(servicePoolListener, newServices, oldServicesRemoved);
    }

    private boolean handleEvents(ServicePoolListener servicePoolListener, int newServices, int oldServicesRemoved) {
        if (oldServicesRemoved > 0) {
            servicePoolListener.servicesRemoved(serviceName, newServices);
        }

        if (newServices > 0) {
            servicePoolListener.servicesAdded(serviceName, newServices);
        }

        boolean changed = oldServicesRemoved > 0 || newServices > 0;

        if (changed) {

            servicePoolListener.servicePoolChanged(serviceName);
        }

        return changed;
    }

}

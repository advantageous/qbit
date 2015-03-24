package io.advantageous.qbit.service.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * Service pool
 * Created by rhightower on 3/23/15.
 */
public class ServicePool {

    private final String serviceName;


    private final AtomicReference<Map<String, ServiceDefinition>> pool =
            new AtomicReference<>(new ConcurrentHashMap<>());
    private final ServicePoolListener servicePoolListener;

    public ServicePool(final String serviceName,
                       final ServicePoolListener servicePoolListener) {
        this.serviceName = serviceName;
        this.servicePoolListener = servicePoolListener==null ?
                serviceNameChanged -> {

                } : servicePoolListener;
    }

    public List<ServiceDefinition> services() {
        return new ArrayList<>(pool.get().values());
    }

    public String getServiceName() {
        return serviceName;
    }

    public  boolean setHealthyNodes (final List<ServiceDefinition> services) {

        return setHealthyNodes(services, this.servicePoolListener);
    }
        /**
         *
         * @param services services
         * @return true if services have changed
         */
    public synchronized boolean setHealthyNodes (final List<ServiceDefinition> services,
                                                 final ServicePoolListener servicePoolListener) {

        final Map<String, ServiceDefinition> oldMap = pool.get();


        final Map<String, ServiceDefinition> newMap = new ConcurrentHashMap<>(services.size());

        int newServices = 0;
        for (ServiceDefinition service : services) {

            if (!oldMap.containsKey(service.getId())) {
                newServices++;
                servicePoolListener.serviceAdded(serviceName);
            }
            newMap.put(service.getId(), service);

        }


        if (newServices > 0) {
            servicePoolListener.servicesAdded(serviceName, newServices);
        }

        //log the number of new services


        int oldServicesRemoved = 0;
        for (ServiceDefinition service : oldMap.values()) {


            if (!newMap.containsKey(service.getId())) {
                oldServicesRemoved++;
                servicePoolListener.serviceRemoved(serviceName);
                //log an old service was removed
            }
        }


        if (oldServicesRemoved > 0) {
            servicePoolListener.servicesRemoved(serviceName, newServices);
        }



        if (!pool.compareAndSet(oldMap, newMap)) {
            //Log this

        }

        boolean changed =  oldServicesRemoved > 0 || newServices > 0;

        if (changed) {
            servicePoolListener.servicePoolChanged(serviceName);
        }

        return changed;
    }

}

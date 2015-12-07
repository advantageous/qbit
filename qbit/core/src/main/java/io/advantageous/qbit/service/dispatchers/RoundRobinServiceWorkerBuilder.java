package io.advantageous.qbit.service.dispatchers;

import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class RoundRobinServiceWorkerBuilder {

    private ServiceBuilder serviceBuilder;
    private ServiceWorkers serviceDispatcher;
    private int workerCount = -1;
    private int flushInterval = -1;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private Supplier<Object> serviceObjectSupplier;

    public static RoundRobinServiceWorkerBuilder roundRobinServiceWorkerBuilder() {
        return new RoundRobinServiceWorkerBuilder();
    }

    public Supplier<Object> getServiceObjectSupplier() {
        return serviceObjectSupplier;
    }

    public RoundRobinServiceWorkerBuilder setServiceObjectSupplier(Supplier<Object> serviceObjectSupplier) {
        this.serviceObjectSupplier = serviceObjectSupplier;
        return this;
    }

    public int getWorkerCount() {
        if (workerCount == -1) {
            workerCount = Runtime.getRuntime().availableProcessors();
        }
        return workerCount;
    }

    public RoundRobinServiceWorkerBuilder setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
        return this;
    }

    public ServiceBuilder getServiceBuilder() {
        if (serviceBuilder == null) {
            serviceBuilder = ServiceBuilder.serviceBuilder();
            return serviceBuilder;
        }


        return serviceBuilder.copy();
    }

    public RoundRobinServiceWorkerBuilder setServiceBuilder(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
        return this;
    }


    public ServiceWorkers getServiceDispatcher() {
        if (serviceDispatcher == null) {

            if (flushInterval == -1) {
                serviceDispatcher = ServiceWorkers.workers();
            } else {
                serviceDispatcher = ServiceWorkers.workers(flushInterval, timeUnit);
            }
        }
        return serviceDispatcher;
    }

    public RoundRobinServiceWorkerBuilder setServiceDispatcher(ServiceWorkers serviceDispatcher) {
        this.serviceDispatcher = serviceDispatcher;
        return this;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public RoundRobinServiceWorkerBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public RoundRobinServiceWorkerBuilder setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public ServiceMethodDispatcher build() {

        if (getServiceObjectSupplier() == null) {
            throw new IllegalStateException("serviceObjectSupplier must be set");
        }

        for (int index = 0; index < getWorkerCount(); index++) {

            final ServiceBuilder serviceBuilder = getServiceBuilder();
            final ServiceQueue serviceQueue = serviceBuilder
                    .setServiceObject(getServiceObjectSupplier().get()).build();
            getServiceDispatcher().addServices(serviceQueue);

        }
        return getServiceDispatcher();

    }
}

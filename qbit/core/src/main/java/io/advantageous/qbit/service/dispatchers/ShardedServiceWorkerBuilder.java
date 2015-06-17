package io.advantageous.qbit.service.dispatchers;

import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;

public class ShardedServiceWorkerBuilder {

    /**
     * Shard rule, if you don't set a shard rule, you get shard of first argument.
     * Shard rule by default.
     */
    private ShardRule shardRule;
    private ServiceBuilder serviceBuilder;
    private ServiceWorkers serviceDispatcher;
    private int workerCount = -1;
    private int flushInterval = -1;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private Supplier<Object> serviceObjectSupplier;

    public ShardedServiceWorkerBuilder shardedServiceWorkerBuilder() {
        return new ShardedServiceWorkerBuilder();
    }

    public Supplier<Object> getServiceObjectSupplier() {
        return serviceObjectSupplier;
    }

    public void setServiceObjectSupplier(Supplier<Object> serviceObjectSupplier) {
        this.serviceObjectSupplier = serviceObjectSupplier;
    }

    public int getWorkerCount() {
        if (workerCount == -1) {
            workerCount = Runtime.getRuntime().availableProcessors();
        }
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public ShardRule getShardRule() {
        if (shardRule == null) {
            final int workerCount = this.getWorkerCount();
            shardRule = (methodName, methodArgs, numWorkers) -> methodArgs[0].hashCode() % workerCount;
        }
        return shardRule;
    }

    public void setShardRule(ShardRule shardRule) {
        this.shardRule = shardRule;
    }

    public ServiceBuilder getServiceBuilder() {
        if (serviceBuilder == null) {
            serviceBuilder = ServiceBuilder.serviceBuilder();
        }
        return serviceBuilder;
    }

    public ShardedServiceWorkerBuilder setServiceBuilder(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
        return this;
    }


    public ServiceWorkers getServiceDispatcher() {
        if (serviceDispatcher == null) {

            if (this.flushInterval == -1) {
                serviceDispatcher = ServiceWorkers.shardedWorkers(getShardRule());
            } else {
                serviceDispatcher = ServiceWorkers.shardedWorkers(getFlushInterval(),
                        getTimeUnit(), getShardRule());
            }
        }
        return serviceDispatcher;
    }

    public ShardedServiceWorkerBuilder setServiceDispatcher(ServiceWorkers serviceDispatcher) {
        this.serviceDispatcher = serviceDispatcher;
        return this;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public ShardedServiceWorkerBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public ShardedServiceWorkerBuilder setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public ServiceMethodDispatcher build() {

        if (getServiceObjectSupplier() == null) {
            throw new IllegalStateException("serviceObjectSupplier must be set");
        }
        final ServiceBuilder serviceBuilder = getServiceBuilder();

        for (int index = 0; index < getWorkerCount(); index++) {
            final ServiceQueue serviceQueue = serviceBuilder
                    .setServiceObject(getServiceObjectSupplier().get()).build();
            getServiceDispatcher().addServices(serviceQueue);

        }
        return getServiceDispatcher();

    }
}

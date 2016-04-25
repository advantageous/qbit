package io.advantageous.qbit.admin;

import io.advantageous.qbit.service.health.ServiceHealthManager;
import io.advantageous.qbit.service.impl.ServiceHealthManagerDefault;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;
import io.advantageous.reakt.reactor.Reactor;

import java.time.Duration;
import java.util.Objects;

public class ServiceManagementBundleBuilder {

    private Reactor reactor;
    private StatsCollector statsCollector;
    private ServiceHealthManager serviceHealthManager;
    private String serviceName;
    private Timer timer;
    private String statKeyPrefix;
    private Runnable processHandler;
    private ManagedServiceBuilder managedServiceBuilder;
    private Runnable failCallback;
    private Runnable recoverCallback;
    private Duration timeoutDuration = Duration.ofSeconds(30);

    private Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public ServiceManagementBundleBuilder setTimeoutDuration(final Duration timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    private Runnable getFailCallback() {
        return failCallback;
    }

    public ServiceManagementBundleBuilder setFailCallback(Runnable failCallback) {
        this.failCallback = failCallback;
        return this;
    }

    private Runnable getRecoverCallback() {
        return recoverCallback;
    }

    public ServiceManagementBundleBuilder setRecoverCallback(Runnable recoverCallback) {
        this.recoverCallback = recoverCallback;
        return this;
    }

    private ManagedServiceBuilder getManagedServiceBuilder() {
        if (managedServiceBuilder == null) {
            managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder();
        }
        return managedServiceBuilder;
    }

    public ServiceManagementBundleBuilder setManagedServiceBuilder(ManagedServiceBuilder managedServiceBuilder) {
        this.managedServiceBuilder = managedServiceBuilder;
        return this;
    }

    public ServiceManagementBundleBuilder setReactor(Reactor reactor) {
        this.reactor = reactor;
        return this;
    }

    public ServiceManagementBundleBuilder setStatsCollector(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        return this;
    }

    public ServiceManagementBundleBuilder setServiceHealthManager(ServiceHealthManager serviceHealthManager) {
        this.serviceHealthManager = serviceHealthManager;
        return this;
    }

    public ServiceManagementBundleBuilder setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceManagementBundleBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public ServiceManagementBundleBuilder setStatKeyPrefix(String statKeyPrefix) {
        this.statKeyPrefix = statKeyPrefix;
        return this;
    }

    public ServiceManagementBundleBuilder setProcessHandler(Runnable processHandler) {
        this.processHandler = processHandler;
        return this;
    }

    public Runnable getProcessHandler() {
        return processHandler;
    }

    private Reactor getReactor() {
        if (reactor == null) {
            final Timer timer = Timer.timer();
            reactor = Reactor.reactor(timeoutDuration, timer::now);
        }
        return reactor;
    }

    private StatsCollector getStatsCollector() {
        if (managedServiceBuilder == null) Objects.requireNonNull(statsCollector, "Stats must be set");

        if (statsCollector == null) {
            statsCollector = getManagedServiceBuilder().createStatsCollector();
        }
        return statsCollector;
    }

    private ServiceHealthManager getServiceHealthManager() {
        if (managedServiceBuilder == null) Objects.requireNonNull(serviceHealthManager,
                "ServiceHealthManager must be set");

        if (serviceHealthManager == null) {
            serviceHealthManager = new ServiceHealthManagerDefault(getFailCallback(), getRecoverCallback());
        }
        return serviceHealthManager;
    }

    private String getServiceName() {

        Objects.requireNonNull(serviceName, "serviceName must be set");
        return serviceName;
    }

    private Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    private String getStatKeyPrefix() {
        if (statKeyPrefix == null) {
            statKeyPrefix = getServiceName() + ".";
        }
        return statKeyPrefix;
    }

    public ServiceManagementBundle build() {
        return new ServiceManagementBundle(getReactor(), getStatsCollector(), getServiceHealthManager(),
                getServiceName(), getTimer(), getStatKeyPrefix(), getProcessHandler());
    }

    public static ServiceManagementBundleBuilder serviceManagementBundleBuilder() {
        return new ServiceManagementBundleBuilder();
    }
}

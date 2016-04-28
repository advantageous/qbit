package io.advantageous.qbit.admin;

import io.advantageous.qbit.service.health.HealthServiceClient;
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
    private HealthServiceClient healthServiceClient;

    public static ServiceManagementBundleBuilder serviceManagementBundleBuilder() {
        return new ServiceManagementBundleBuilder();
    }

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

    public HealthServiceClient getHealthServiceClient() {
        return healthServiceClient;
    }

    public ServiceManagementBundleBuilder setHealthServiceClient(HealthServiceClient healthServiceClient) {
        this.healthServiceClient = healthServiceClient;
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

    public Runnable getProcessHandler() {
        return processHandler;
    }

    public ServiceManagementBundleBuilder setProcessHandler(Runnable processHandler) {
        this.processHandler = processHandler;
        return this;
    }

    private Reactor getReactor() {
        if (reactor == null) {
            final Timer timer = Timer.timer();
            reactor = Reactor.reactor(timeoutDuration, timer::now);
        }
        return reactor;
    }

    public ServiceManagementBundleBuilder setReactor(Reactor reactor) {
        this.reactor = reactor;
        return this;
    }

    private StatsCollector getStatsCollector() {
        if (managedServiceBuilder == null) Objects.requireNonNull(statsCollector, "Stats must be set");

        if (statsCollector == null) {
            statsCollector = getManagedServiceBuilder().createStatsCollector();
        }
        return statsCollector;
    }

    public ServiceManagementBundleBuilder setStatsCollector(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        return this;
    }

    private ServiceHealthManager getServiceHealthManager() {
        if (managedServiceBuilder == null) Objects.requireNonNull(serviceHealthManager,
                "ServiceHealthManager must be set");

        if (serviceHealthManager == null) {
            serviceHealthManager = new ServiceHealthManagerDefault(getFailCallback(), getRecoverCallback());
        }
        return serviceHealthManager;
    }

    public ServiceManagementBundleBuilder setServiceHealthManager(ServiceHealthManager serviceHealthManager) {
        this.serviceHealthManager = serviceHealthManager;
        return this;
    }

    private String getServiceName() {

        Objects.requireNonNull(serviceName, "serviceName must be set");
        return serviceName;
    }

    public ServiceManagementBundleBuilder setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    private Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public ServiceManagementBundleBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    private String getStatKeyPrefix() {
        if (statKeyPrefix == null) {
            statKeyPrefix = getServiceName() + ".";
        }
        return statKeyPrefix;
    }

    public ServiceManagementBundleBuilder setStatKeyPrefix(String statKeyPrefix) {
        this.statKeyPrefix = statKeyPrefix;
        return this;
    }

    public ServiceManagementBundle build() {
        return new ServiceManagementBundle(getReactor(), getStatsCollector(), getServiceHealthManager(),
                getServiceName(), getTimer(), getStatKeyPrefix(), getProcessHandler(), healthServiceClient);
    }
}

package io.advantageous.qbit.service.health;


import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HealthServiceBuilder {


    public static final String CONTEXT = "qbit.service.health.";
    private HealthServiceAsync proxy;
    private HealthService implementation;
    private ServiceBuilder serviceBuilder;
    private ServiceQueue serviceQueue;
    private Timer timer;
    private long recheckInterval = 10;
    private TimeUnit timeUnit;
    private boolean autoFlush;
    private Optional<Consumer<NodeHealthStat>> onFail = Optional.empty();
    private Optional<Consumer<NodeHealthStat>> onWarn = Optional.empty();
    private Optional<Consumer<NodeHealthStat>> onCheckIn = Optional.empty();
    private StatsCollector statsCollector;
    private Reactor reactor;
    private String statKeyPrefix = "health";
    private List<HealthCheckJob> healthCheckJobs;


    public HealthServiceBuilder(final PropertyResolver propertyResolver) {

        recheckInterval = propertyResolver.getLongProperty("recheckIntervalSeconds", recheckInterval);
        timeUnit = TimeUnit.SECONDS;
    }


    public HealthServiceBuilder() {

        this(PropertyResolver.createSystemPropertyResolver(CONTEXT));
    }

    public HealthServiceBuilder(Properties properties) {

        this(PropertyResolver.createPropertiesPropertyResolver(CONTEXT, properties));
    }

    public static HealthServiceBuilder healthServiceBuilder() {
        return new HealthServiceBuilder();
    }

    public StatsCollector getStatsCollector() {

        if (this.statsCollector == null) {
            statsCollector = new StatsCollector() {

            };
        }
        return statsCollector;
    }

    public HealthServiceBuilder setStatsCollector(StatsCollector statsCollector) {

        this.statsCollector = statsCollector;
        return this;
    }


    public List<HealthCheckJob> getHealthCheckJobs() {
        if (healthCheckJobs == null) {
            healthCheckJobs = new ArrayList<>();
        }
        return healthCheckJobs;
    }

    public HealthServiceBuilder setHealthCheckJobs(List<HealthCheckJob> healthCheckJobs) {
        this.healthCheckJobs = healthCheckJobs;
        return this;
    }

    public HealthServiceBuilder addJob(final HealthCheckJob healthCheckJob) {
        this.getHealthCheckJobs().add(healthCheckJob);
        return this;
    }

    public HealthServiceBuilder addJob(final String name, final Duration duration, final HealthCheck healthCheck) {
        this.getHealthCheckJobs().add(new HealthCheckJob(healthCheck, duration, name));
        return this;
    }

    public HealthServiceBuilder addJob(final String name, final long duration, final TimeUnit timeUnit,
                                       final HealthCheck healthCheck) {
        this.getHealthCheckJobs().add(new HealthCheckJob(healthCheck, new Duration(duration, timeUnit), name));
        return this;
    }


    public Reactor getReactor() {
        if (reactor == null) {
            reactor = ReactorBuilder.reactorBuilder().build();
        }
        return reactor;
    }

    public HealthServiceBuilder setReactor(Reactor reactor) {
        this.reactor = reactor;
        return this;
    }

    public String getStatKeyPrefix() {
        return statKeyPrefix;
    }

    public HealthServiceBuilder setStatKeyPrefix(String statKeyPrefix) {
        this.statKeyPrefix = statKeyPrefix;
        return this;
    }

    public long getRecheckInterval() {
        return recheckInterval;
    }

    public HealthServiceBuilder setRecheckInterval(long recheckInterval) {
        this.recheckInterval = recheckInterval;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public HealthServiceBuilder setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public HealthServiceBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public ServiceQueue getServiceQueue() {
        if (serviceQueue == null) {
            serviceQueue = getServiceBuilder().setServiceObject(getImplementation()).build();
        }
        return serviceQueue;
    }

    public HealthServiceBuilder setServiceQueue(final ServiceQueue serviceQueue) {
        this.serviceQueue = serviceQueue;
        return this;
    }

    public HealthServiceAsync getProxy() {
        if (proxy == null) {
            if (autoFlush) {
                proxy = getServiceQueue().createProxyWithAutoFlush(HealthServiceAsync.class, 50, TimeUnit.MILLISECONDS);
            } else {
                proxy = getServiceQueue().createProxy(HealthServiceAsync.class);
            }
        }
        return proxy;
    }

    public HealthServiceBuilder setProxy(HealthServiceAsync proxy) {
        this.proxy = proxy;
        return this;
    }

    public HealthService getImplementation() {
        if (implementation == null) {
            implementation = new HealthServiceImpl(getStatKeyPrefix(),
                    getReactor(),
                    getTimer(),
                    getStatsCollector(),
                    getRecheckInterval(),
                    getTimeUnit(),
                    getHealthCheckJobs(),
                    getOnFail(),
                    getOnWarn(), getOnCheckIn());
        }
        return implementation;
    }

    public HealthServiceBuilder setImplementation(HealthService implementation) {
        this.implementation = implementation;
        return this;
    }

    public ServiceBuilder getServiceBuilder() {
        if (serviceBuilder == null) {
            serviceBuilder = ServiceBuilder.serviceBuilder();
            return serviceBuilder;
        }
        return serviceBuilder.copy();
    }

    public HealthServiceBuilder setServiceBuilder(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
        return this;
    }


    public Optional<Consumer<NodeHealthStat>> getOnFail() {
        return onFail;
    }

    public HealthServiceBuilder setOnFail(Consumer<NodeHealthStat> onFail) {
        this.onFail = Optional.of(onFail);
        return this;
    }

    public Optional<Consumer<NodeHealthStat>> getOnWarn() {
        return onWarn;
    }

    public HealthServiceBuilder setOnWarn(Consumer<NodeHealthStat> onWarn) {
        this.onWarn = Optional.of(onWarn);
        return this;
    }

    public Optional<Consumer<NodeHealthStat>> getOnCheckIn() {
        return onCheckIn;
    }

    public HealthServiceBuilder setOnCheckIn(Consumer<NodeHealthStat> onCheckIn) {
        this.onCheckIn = Optional.of(onCheckIn);
        return this;
    }

    public HealthServiceAsync build() {
        return getProxy();
    }

    public HealthServiceAsync buildAndStart() {
        HealthServiceAsync proxy = getProxy();
        getServiceQueue().start();
        getServiceQueue().startCallBackHandler();
        return proxy;
    }

    public HealthServiceBuilder setAutoFlush() {
        autoFlush = true;
        return this;
    }


    public HealthServiceAsync buildHealthSystemReporterWithAutoFlush() {
        return getServiceQueue().createProxyWithAutoFlush(HealthServiceAsync.class, 1, TimeUnit.SECONDS);
    }


    public HealthServiceAsync buildHealthSystemReporter() {

        return getServiceQueue().createProxy(HealthServiceAsync.class);
    }
}

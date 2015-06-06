package io.advantageous.qbit.service.health;


import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.util.Timer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class HealthServiceBuilder {


    public static final String CONTEXT = "qbit.service.health.";
    private HealthServiceAsync proxy;
    private HealthService implementation;
    private ServiceBuilder serviceBuilder;
    private ServiceQueue serviceQueue;
    private Timer timer;
    private long recheckInterval;
    private TimeUnit timeUnit;
    private boolean autoFlush;

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
            implementation = new HealthServiceImpl(getTimer(),
                    getRecheckInterval(), getTimeUnit());
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
        }
        return serviceBuilder;
    }

    public HealthServiceBuilder setServiceBuilder(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
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
}

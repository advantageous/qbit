package io.advantageous.qbit.metrics.support;

import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.TimeUnit;

public class LocalStatsCollectorBuilder {


    private LocalStatsCollector localStatsCollector;
    private int calculateEveryNSeconds = 60;
    private ServiceBuilder serviceBuilder;
    private ServiceQueue serviceQueue;
    private Timer timer;


    public static LocalStatsCollectorBuilder localStatsCollectorBuilder() {
        return new LocalStatsCollectorBuilder();
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public LocalStatsCollectorBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public LocalStatsCollector getLocalStatsCollector() {
        if (localStatsCollector == null) {
            localStatsCollector = new LocalStatsCollector(getCalculateEveryNSeconds(), getTimer());
        }
        return localStatsCollector;
    }

    public LocalStatsCollectorBuilder setLocalStatsCollector(LocalStatsCollector localStatsCollector) {
        this.localStatsCollector = localStatsCollector;
        return this;
    }

    public int getCalculateEveryNSeconds() {
        return calculateEveryNSeconds;
    }

    public LocalStatsCollectorBuilder setCalculateEveryNSeconds(int calculateEveryNSeconds) {
        this.calculateEveryNSeconds = calculateEveryNSeconds;
        return this;
    }

    public ServiceBuilder getServiceBuilder() {
        if (serviceBuilder == null) {
            serviceBuilder = ServiceBuilder.serviceBuilder().setServiceObject(getLocalStatsCollector());
            return serviceBuilder;
        }

        return serviceBuilder.copy().setServiceObject(getLocalStatsCollector());
    }

    public LocalStatsCollectorBuilder setServiceBuilder(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
        return this;
    }

    public ServiceQueue getServiceQueue() {
        if (serviceQueue == null) {
            serviceQueue = getServiceBuilder().build();
        }
        return serviceQueue;
    }

    public LocalStatsCollectorBuilder setServiceQueue(ServiceQueue serviceQueue) {
        this.serviceQueue = serviceQueue;
        return this;
    }

    public LocalStatsCollectorAsync build() {
        LocalStatsCollectorAsync proxyWithAutoFlush = getServiceQueue().createProxyWithAutoFlush(LocalStatsCollectorAsync.class, 100, TimeUnit.MILLISECONDS);
        return proxyWithAutoFlush;
    }


    public LocalStatsCollectorAsync buildAndStart() {

        getServiceQueue().startServiceQueue().startCallBackHandler();

        return build();
    }
}

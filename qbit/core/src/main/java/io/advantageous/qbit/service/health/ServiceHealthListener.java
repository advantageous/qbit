package io.advantageous.qbit.service.health;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.service.ServiceContext;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.TimeUnit;

public class ServiceHealthListener implements QueueCallBackHandler {

    private final String serviceName;
    private final HealthServiceAsync healthServiceAsync;
    private final long checkInIntervalMS;
    private final Timer timer;
    private final long ttlMS;
    private long now;
    private long lastCheckTime;

    public ServiceHealthListener(final String serviceName,
                                 final HealthServiceAsync healthServiceAsync,
                                 final Timer timer,
                                 final long checkInInterval,
                                 final long ttL,
                                 final TimeUnit timeUnit) {
        this.serviceName = serviceName;
        this.healthServiceAsync = healthServiceAsync;
        this.checkInIntervalMS = timeUnit.toMillis(checkInInterval);
        this.timer = timer;
        this.ttlMS = timeUnit.toMillis(ttL);
        lastCheckTime = now;
    }

    @Override
    public void queueLimit() {
        check();
    }

    @Override
    public void queueEmpty() {
        check();
    }

    private void check() {
        now = timer.now();

        long duration = now - lastCheckTime;

        if (duration > checkInIntervalMS) {
            lastCheckTime = now;

            final ServiceQueue serviceQueue = ServiceContext.serviceContext().currentService();

            boolean failing = serviceQueue.failing();

            if (!failing) {
                healthServiceAsync.checkInOk(serviceName);
            } else {
                healthServiceAsync.checkIn(serviceName, HealthStatus.FAIL);
            }
            healthServiceAsync.clientProxyFlush();
        }
    }

    @Override
    public void queueInit() {
        healthServiceAsync.register(serviceName, ttlMS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void queueIdle() {
        check();
    }

    @Override
    public void queueShutdown() {

        healthServiceAsync.unregister(serviceName);
    }

}

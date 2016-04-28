package io.advantageous.qbit.service.health;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.TimeUnit;

/**
 * Checks for health status for a service.
 * A service could be marked as healthy or unhealthy.
 */
public class ServiceHealthListener implements QueueCallBackHandler {

    /**
     * Name of service in the health system.
     */
    private final String serviceName;

    /**
     * Async interface to the health system.
     */
    private final HealthServiceAsync healthServiceAsync;

    /**
     * How often should we check in with the health system?
     */
    private final long checkInIntervalMS;

    /**
     * QBit timer.
     */
    private final Timer timer;

    /**
     * TTL for service in MS.
     */
    private final long ttlMS;

    /**
     * Health manager
     */
    private final ServiceHealthManager healthManager;

    /**
     * Current time.
     */
    private long now;

    /**
     * Last check in time.
     */
    private long lastCheckTime;

    public ServiceHealthListener(final String serviceName,
                                 final HealthServiceAsync healthServiceAsync,
                                 final Timer timer,
                                 final long checkInInterval,
                                 final long ttL,
                                 final TimeUnit timeUnit,
                                 final ServiceHealthManager healthManager) {
        this.serviceName = serviceName;
        this.healthServiceAsync = healthServiceAsync;
        this.checkInIntervalMS = timeUnit.toMillis(checkInInterval);
        this.timer = timer;
        this.ttlMS = timeUnit.toMillis(ttL);
        lastCheckTime = now;
        this.healthManager = healthManager;
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

            final boolean failing = healthManager.isFailing();

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

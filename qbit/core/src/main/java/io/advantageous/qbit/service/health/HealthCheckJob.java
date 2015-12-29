package io.advantageous.qbit.service.health;

import io.advantageous.qbit.time.Duration;

public class HealthCheckJob {

    private final HealthCheck healthCheck;
    private final Duration duration;
    private String name;


    public HealthCheckJob(HealthCheck healthCheck, Duration duration, String name) {
        this.healthCheck = healthCheck;
        this.duration = duration;
        this.name = name;
    }

    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public HealthCheckJob setName(String name) {
        this.name = name;
        return this;
    }
}

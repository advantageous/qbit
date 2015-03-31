package io.advantageous.qbit.service.discovery.impl;

import io.advantageous.qbit.service.discovery.HealthStatus;

/**
* Created by rhightower on 3/24/15.
*/
public class ServiceHealthCheckIn {
    final private String serviceId;
    final private HealthStatus healthStatus;

    public ServiceHealthCheckIn(String serviceId, HealthStatus healthStatus) {
        this.serviceId = serviceId;
        this.healthStatus = healthStatus;
    }

    public String getServiceId() {
        return serviceId;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceHealthCheckIn)) return false;

        ServiceHealthCheckIn checkIn = (ServiceHealthCheckIn) o;

        if (healthStatus != checkIn.healthStatus) return false;
        if (serviceId != null ? !serviceId.equals(checkIn.serviceId) : checkIn.serviceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId != null ? serviceId.hashCode() : 0;
        result = 31 * result + (healthStatus != null ? healthStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "serviceId='" + serviceId + '\'' +
                ", healthStatus=" + healthStatus +
                '}';
    }
}

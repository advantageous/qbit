package io.advantageous.qbit.service.health;

import io.advantageous.qbit.annotation.JsonIgnore;

import java.util.Optional;

/**
 * Internal class to hold health status.
 */
public class NodeHealthStat implements Cloneable {
    private final String name;
    private final long ttlInMS;
    private long lastCheckIn;
    private HealthFailReason reason = HealthFailReason.NONE;
    private HealthStatus status = HealthStatus.UNKNOWN;

    private
    @JsonIgnore
    Optional<Throwable> error = Optional.empty();

    public NodeHealthStat(final String name, final long ttlInMS) {
        this.name = name;
        this.ttlInMS = ttlInMS;
    }


    public NodeHealthStat(final String name) {
        this.name = name;
        this.ttlInMS = Long.MIN_VALUE;
    }

    public String getName() {
        return name;
    }

    public long getTtlInMS() {
        return ttlInMS;
    }

    public long getLastCheckIn() {
        return lastCheckIn;
    }

    public void setLastCheckIn(long lastCheckIn) {
        this.lastCheckIn = lastCheckIn;
    }

    public HealthFailReason getReason() {
        return reason;
    }

    public void setReason(HealthFailReason reason) {
        this.reason = reason;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public void setStatus(HealthStatus status) {

        this.status = status;

        if (status == HealthStatus.PASS) {
            clearError();
        }
    }


    @JsonIgnore
    public boolean isForever() {
        return this.ttlInMS == Long.MIN_VALUE;
    }

    @JsonIgnore
    public boolean isOk() {
        return this.status == HealthStatus.PASS;
    }


    @JsonIgnore
    public Optional<Throwable> getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = Optional.of(error);
    }


    public void clearError() {
        this.error = Optional.empty();
        this.reason = HealthFailReason.NONE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        NodeHealthStat node = new NodeHealthStat(name, ttlInMS);
        node.reason = this.reason;
        node.error = this.error;
        node.status = this.status;
        return node;
    }

    @Override
    public String toString() {
        return "NodeHealthStat{" +
                "name='" + name + '\'' +
                ", ttlInMS=" + ttlInMS +
                ", lastCheckIn=" + lastCheckIn +
                ", reason=" + reason +
                ", status=" + status +
                ", error=" + error +
                '}';
    }
}

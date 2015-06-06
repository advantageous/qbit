package io.advantageous.qbit.service.health;

/**
 * Internal class to hold health status.
 */
class NodeHealthStat {
    private final String name;
    private final long ttlInMS;
    private long lastCheckIn;
    private HealthServiceImpl.HealthFailReason reason;
    private HealthStatus status = HealthStatus.UNKNOWN;

    public NodeHealthStat(final String name, final long ttlInMS) {
        this.name = name;
        this.ttlInMS = ttlInMS;
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

    public HealthServiceImpl.HealthFailReason getReason() {
        return reason;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public void setLastCheckIn(long lastCheckIn) {
        this.lastCheckIn = lastCheckIn;
    }

    public void setReason(HealthServiceImpl.HealthFailReason reason) {
        this.reason = reason;
    }

    public void setStatus(HealthStatus status) {
        this.status = status;
    }
}

package io.advantageous.qbit.eventbus;

import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.impl.EventConnectorHub;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class EventBusClusterBuilder {

    private final Logger logger = LoggerFactory.getLogger(EventBusClusterBuilder.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private String eventBusName = "event-bus";
    private EventConnectorHub eventConnectorHub = null;
    private PeriodicScheduler periodicScheduler = null;
    private int peerCheckTimeInterval = 7;
    private TimeUnit peerCheckTimeUnit = TimeUnit.SECONDS;
    private int longPollTimeSeconds = 5;
    private int replicationPortLocal = 9090;
    private String replicationHostLocal = null;
    private EventManager eventManager = null;
    private int replicationServerCheckInInterval = 5;
    private TimeUnit replicationServerCheckInTimeUnit = TimeUnit.SECONDS;

    private ServiceDiscovery serviceDiscovery;


    public static EventBusClusterBuilder eventBusClusterBuilder() {
        return new EventBusClusterBuilder();
    }

    public EventBusCluster build() {

        return new EventBusCluster(getEventManager(), getEventBusName(), getEventConnectorHub(), getPeriodicScheduler(),
                getPeerCheckTimeInterval(), getPeerCheckTimeUnit(), getReplicationServerCheckInInterval(),
                getReplicationServerCheckInTimeUnit(), getServiceDiscovery(), getReplicationPortLocal(),
                getReplicationHostLocal());

    }

    public String getEventBusName() {
        return eventBusName;
    }

    public EventBusClusterBuilder setEventBusName(String eventBusName) {
        this.eventBusName = eventBusName;
        return this;
    }

    public EventConnectorHub getEventConnectorHub() {
        return eventConnectorHub;
    }

    public EventBusClusterBuilder setEventConnectorHub(EventConnectorHub eventConnectorHub) {
        this.eventConnectorHub = eventConnectorHub;
        return this;
    }

    public PeriodicScheduler getPeriodicScheduler() {
        return periodicScheduler;
    }

    public EventBusClusterBuilder setPeriodicScheduler(PeriodicScheduler periodicScheduler) {
        this.periodicScheduler = periodicScheduler;
        return this;
    }

    public int getPeerCheckTimeInterval() {
        return peerCheckTimeInterval;
    }

    public EventBusClusterBuilder setPeerCheckTimeInterval(int peerCheckTimeInterval) {
        this.peerCheckTimeInterval = peerCheckTimeInterval;
        return this;
    }

    public TimeUnit getPeerCheckTimeUnit() {
        return peerCheckTimeUnit;
    }

    public EventBusClusterBuilder setPeerCheckTimeUnit(TimeUnit peerCheckTimeUnit) {
        this.peerCheckTimeUnit = peerCheckTimeUnit;
        return this;
    }


    public int getLongPollTimeSeconds() {
        return longPollTimeSeconds;
    }

    public EventBusClusterBuilder setLongPollTimeSeconds(int longPollTimeSeconds) {
        this.longPollTimeSeconds = longPollTimeSeconds;
        return this;
    }


    public int getReplicationPortLocal() {
        return replicationPortLocal;
    }

    public EventBusClusterBuilder setReplicationPortLocal(int replicationPortLocal) {
        this.replicationPortLocal = replicationPortLocal;
        return this;
    }

    public String getReplicationHostLocal() {
        return replicationHostLocal;
    }

    public EventBusClusterBuilder setReplicationHostLocal(String replicationHostLocal) {
        this.replicationHostLocal = replicationHostLocal;
        return this;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public EventBusClusterBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    public int getReplicationServerCheckInInterval() {
        return replicationServerCheckInInterval;
    }

    public EventBusClusterBuilder setReplicationServerCheckInInterval(int replicationServerCheckInInterval) {
        this.replicationServerCheckInInterval = replicationServerCheckInInterval;
        return this;
    }

    public TimeUnit getReplicationServerCheckInTimeUnit() {
        return replicationServerCheckInTimeUnit;
    }

    public EventBusClusterBuilder setReplicationServerCheckInTimeUnit(TimeUnit replicationServerCheckInTimeUnit) {
        this.replicationServerCheckInTimeUnit = replicationServerCheckInTimeUnit;
        return this;
    }

    public ServiceDiscovery getServiceDiscovery() {
        if (serviceDiscovery == null) {
            serviceDiscovery = ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder().build();
            serviceDiscovery.start();
        }
        return serviceDiscovery;
    }

    public EventBusClusterBuilder setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        return this;
    }
}

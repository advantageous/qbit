package io.advantageous.qbit.eventbus;

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
    private String eventBusName = "eventBus";
    private EventConnectorHub eventConnectorHub = null;
    private PeriodicScheduler periodicScheduler = null;
    private int peerCheckTimeInterval = 7;
    private TimeUnit peerCheckTimeUnit = TimeUnit.SECONDS;
    private String consulHost = null;
    private int consulPort = 8500;
    private String datacenter = null;
    private String tag = null;
    private int longPollTimeSeconds = 5;
    private String localEventBusId;
    private int replicationPortLocal = 9090;
    private String replicationHostLocal = null;
    private EventManager eventManager = null;
    private int replicationServerCheckInIntervalInSeconds = 5;

    public static EventBusClusterBuilder eventBusRingBuilder() {
        return new EventBusClusterBuilder();
    }

    public EventBusCluster build() {

        if (consulHost == null) {
            consulHost = "localhost";
        }

        if (localEventBusId == null) {
            localEventBusId = eventBusName + "-" + ServiceDiscovery.uniqueString(replicationPortLocal);
        }

        return new EventBusCluster(getEventManager(), getEventBusName(), getLocalEventBusId(),
                getEventConnectorHub(), getPeriodicScheduler(),
                getPeerCheckTimeInterval(), getPeerCheckTimeUnit(), getConsulHost(),
                getConsulPort(), getLongPollTimeSeconds(), getReplicationPortLocal(),
                getReplicationHostLocal(), getDatacenter(), getTag(),
                getReplicationServerCheckInIntervalInSeconds());
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

    public String getConsulHost() {
        return consulHost;
    }

    public EventBusClusterBuilder setConsulHost(String consulHost) {
        this.consulHost = consulHost;
        return this;
    }

    public int getConsulPort() {
        return consulPort;
    }

    public EventBusClusterBuilder setConsulPort(int consulPort) {
        this.consulPort = consulPort;
        return this;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public EventBusClusterBuilder setDatacenter(String datacenter) {
        this.datacenter = datacenter;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public EventBusClusterBuilder setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public int getLongPollTimeSeconds() {
        return longPollTimeSeconds;
    }

    public EventBusClusterBuilder setLongPollTimeSeconds(int longPollTimeSeconds) {
        this.longPollTimeSeconds = longPollTimeSeconds;
        return this;
    }

    public String getLocalEventBusId() {
        return localEventBusId;
    }

    public EventBusClusterBuilder setLocalEventBusId(String localEventBusId) {
        this.localEventBusId = localEventBusId;
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

    public int getReplicationServerCheckInIntervalInSeconds() {
        return replicationServerCheckInIntervalInSeconds;
    }

    public EventBusClusterBuilder setReplicationServerCheckInIntervalInSeconds(int replicationServerCheckInIntervalInSeconds) {
        this.replicationServerCheckInIntervalInSeconds = replicationServerCheckInIntervalInSeconds;
        return this;
    }
}

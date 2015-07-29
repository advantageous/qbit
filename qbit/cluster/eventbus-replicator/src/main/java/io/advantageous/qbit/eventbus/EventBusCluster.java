package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Str;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.client.RemoteTCPClientProxy;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.impl.EventConnectorHub;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.ServicePool;
import io.advantageous.qbit.service.discovery.ServicePoolListener;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.advantageous.qbit.eventbus.EventBusRemoteReplicatorBuilder.eventBusRemoteReplicatorBuilder;
import static io.advantageous.qbit.eventbus.EventBusReplicationClientBuilder.eventBusReplicationClientBuilder;
import static io.advantageous.qbit.events.EventManagerBuilder.eventManagerBuilder;
import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;

public class EventBusCluster implements Startable, Stoppable {

    private final String eventBusName;
    private final EventConnectorHub eventConnectorHub;
    private final PeriodicScheduler periodicScheduler;
    private final int peerCheckTimeInterval;
    private final TimeUnit peerCheckTimeUnit;
    private final EventManager eventManager;
    private final int replicationServerCheckInIntervalInSeconds;

    private final Logger logger = LoggerFactory.getLogger(EventBusCluster.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean info = logger.isInfoEnabled();

    private final int replicationPortLocal;
    private final String replicationHostLocal;
    private ScheduledFuture healthyNodeMonitor;
    private ScheduledFuture consulCheckInMonitor;
    private ServiceEndpointServer serviceEndpointServerForReplicator;
    private ServiceQueue eventServiceQueue;
    private EventManager eventManagerImpl;
    private ServicePool servicePool;
    private final ServiceDiscovery serviceDiscovery;
    private final EndpointDefinition endpointDefinition;

    public EventBusCluster(final EventManager eventManager,
                           final String eventBusName,
                           final EventConnectorHub eventConnectorHub,
                           final PeriodicScheduler periodicScheduler,
                           final int peerCheckTimeInterval,
                           final TimeUnit peerCheckTimeTimeUnit,
                           final int replicationServerCheckInInterval,
                           final TimeUnit replicationServerCheckInTimeUnit,
                           final ServiceDiscovery serviceDiscovery,
                           final int replicationPortLocal,
                           final String replicationHostLocal) {

        this.eventBusName = eventBusName;
        this.eventConnectorHub = eventConnectorHub == null ? new EventConnectorHub() : eventConnectorHub;
        this.periodicScheduler = periodicScheduler == null ?
                QBit.factory().periodicScheduler() : periodicScheduler;
        this.peerCheckTimeInterval = peerCheckTimeInterval;
        this.peerCheckTimeUnit = peerCheckTimeTimeUnit;
        this.eventManager = eventManager == null ? createEventManager() : wrapEventManager(eventManager);
        this.replicationServerCheckInIntervalInSeconds = (int)replicationServerCheckInTimeUnit.toSeconds(replicationServerCheckInInterval);
        this.serviceDiscovery = serviceDiscovery;

        this.replicationPortLocal = replicationPortLocal;
        this.replicationHostLocal = replicationHostLocal;

        this.servicePool = new ServicePool(eventBusName, null);


        endpointDefinition = serviceDiscovery.registerWithTTL(eventBusName, replicationPortLocal,
                (int) peerCheckTimeUnit.toSeconds(peerCheckTimeInterval));
    }

    private EventManager wrapEventManager(final EventManager eventManager) {
        if (eventManager instanceof ClientProxy) {
            return eventManager;
        } else {
            eventManagerImpl = eventManager;
            eventServiceQueue = serviceBuilder().setServiceObject(eventManager).build();
            return eventServiceQueue.createProxyWithAutoFlush(EventManager.class, periodicScheduler,
                    100, TimeUnit.MILLISECONDS);
        }
    }

    public EventManager eventManager() {
        return eventManager;
    }


    /** Do we need these? */
    public EventManager eventManagerImpl() {
        return eventManagerImpl;
    }

    public ServiceQueue eventServiceQueue() {
        return eventServiceQueue;
    }

    private EventManager createEventManager() {
        eventManagerImpl = eventManagerBuilder().setEventConnector(eventConnectorHub).build(eventBusName);
        eventServiceQueue = serviceBuilder().setServiceObject(eventManagerImpl).build();

        return eventServiceQueue.createProxyWithAutoFlush(
                EventManager.class, periodicScheduler, 100, TimeUnit.MILLISECONDS);
    }


    @Override
    public void start() {


        if (eventServiceQueue != null) {
            eventServiceQueue.start();
        }

        startServerReplicator();


        healthyNodeMonitor = periodicScheduler.repeat(
                this::healthyNodeMonitor, peerCheckTimeInterval, peerCheckTimeUnit);

        if (replicationServerCheckInIntervalInSeconds > 2) {
            consulCheckInMonitor = periodicScheduler.repeat(this::checkInWithServiceDiscoveryHealth,
                    replicationServerCheckInIntervalInSeconds / 2, TimeUnit.SECONDS);
        } else {
            consulCheckInMonitor = periodicScheduler.repeat(this::checkInWithServiceDiscoveryHealth, 100, TimeUnit.MILLISECONDS);
        }
    }

    private void checkInWithServiceDiscoveryHealth() {

        serviceDiscovery.checkInOk(endpointDefinition.getId());
    }



    private void startServerReplicator() {


        final EventBusRemoteReplicatorBuilder replicatorBuilder = eventBusRemoteReplicatorBuilder();
        replicatorBuilder.setName(this.eventBusName);
        replicatorBuilder.serviceServerBuilder().setPort(replicationPortLocal);

        if (replicationHostLocal != null) {
            replicatorBuilder.serviceServerBuilder().setHost(replicationHostLocal);
        }
        replicatorBuilder.setEventManager(eventManager);
        serviceEndpointServerForReplicator = replicatorBuilder.build();
        serviceEndpointServerForReplicator.start();
    }


    private void healthyNodeMonitor() {


        final List<EndpointDefinition> endpointDefinitions = serviceDiscovery.loadServices(eventBusName);


        final List<EndpointDefinition> removeNodes = new ArrayList<>();

        servicePool.setHealthyNodes(endpointDefinitions, new ServicePoolListener() {
            @Override
            public void servicePoolChanged(String serviceName) {

            }

            @Override
            public void serviceAdded(String serviceName, EndpointDefinition endpointDefinition) {
                if (serviceName.equals(eventBusName)) {
                    addEventConnector(endpointDefinition.getHost(), endpointDefinition.getPort());
                }
            }

            @Override
            public void serviceRemoved(String serviceName, EndpointDefinition endpointDefinition) {
                if (serviceName.equals(eventBusName)) {

                    removeNodes.add(endpointDefinition);
                }

            }

        });


        removeBadServices(removeNodes);


    }



    private void addEventConnector(final String newHost, final int newPort) {

        if (info) logger.info(Str.sputs("Adding new event connector for",
                eventBusName, "host",
                newHost, "port", newPort));

        /* A client replicator */
        final EventBusReplicationClientBuilder clientReplicatorBuilder = eventBusReplicationClientBuilder();
        clientReplicatorBuilder.setName(this.eventBusName);
        clientReplicatorBuilder.clientBuilder().setPort(newPort).setHost(newHost);
        final Client client = clientReplicatorBuilder.build();
        final EventConnector eventConnector = clientReplicatorBuilder.build(client);
        client.start();
        eventConnectorHub.add(eventConnector);
    }

    private int removeBadServices(List<EndpointDefinition> services) {
        final ListIterator<EventConnector> listIterator = eventConnectorHub.listIterator();

        int removeCount = 0;
        final List<EventConnector> badConnectors = new ArrayList<>();

        while (listIterator.hasNext()) {
            final EventConnector connector = listIterator.next();


            /** Remove bad ones. */
            if (connector instanceof RemoteTCPClientProxy) {

                final RemoteTCPClientProxy remoteTCPClientProxy = (RemoteTCPClientProxy) connector;

                if (!remoteTCPClientProxy.connected()) {
                    removeCount++;
                    badConnectors.add(connector);
                    if (info) logger.info(Str.sputs("Removing event connector from",
                            eventBusName, "host",
                            remoteTCPClientProxy.host(),
                            "port", remoteTCPClientProxy.port(),
                            "connected", remoteTCPClientProxy.connected()));

                    continue;
                }

                final String host = remoteTCPClientProxy.host();
                final int port = remoteTCPClientProxy.port();
                boolean found = false;
                for (EndpointDefinition serviceHealth : services) {
                    final int healthyPort = serviceHealth.getPort();
                    final String healthyHost = serviceHealth.getHost();

                    if (healthyPort == port && healthyHost.equals(host)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    removeCount++;
                    badConnectors.add(connector);
                }
            }
        }

        badConnectors.forEach(eventConnectorHub::remove);

        return removeCount;
    }


    @Override
    public void stop() {

        try {

            this.serviceEndpointServerForReplicator.stop();
        } catch (Exception ex) {
            logger.warn("EventBusCluster is unable to stop end point server");
        }

        try {
            if (healthyNodeMonitor != null) {
                healthyNodeMonitor.cancel(true);
            }
        } catch (Exception ex) {
            logger.warn("EventBusCluster is unable to stop healthyNodeMonitor");
        }

        try {
            if (consulCheckInMonitor != null) {
                consulCheckInMonitor.cancel(true);
            }
        } catch (Exception ex) {
            logger.warn("EventBusCluster is unable to stop consulCheckInMonitor");
        }

        try {
            if (eventServiceQueue != null) {
                eventServiceQueue.stop();
            }
        } catch (Exception ex) {
            logger.warn("EventBusCluster is unable to stop eventServiceQueue");
        }


    }
}

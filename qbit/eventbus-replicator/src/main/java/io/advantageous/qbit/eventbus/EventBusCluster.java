package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Str;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final ServiceDiscovery serviceDiscovery;
    private final EndpointDefinition endpointDefinition;
    private ScheduledFuture healthyNodeMonitor;
    private ScheduledFuture consulCheckInMonitor;
    private ServiceEndpointServer serviceEndpointServerForReplicator;
    private ServiceQueue eventServiceQueue;
    private EventManager eventManagerImpl;
    private ServicePool servicePool;

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
        this.replicationServerCheckInIntervalInSeconds = (int) replicationServerCheckInTimeUnit.toSeconds(replicationServerCheckInInterval);
        this.serviceDiscovery = serviceDiscovery;

        this.replicationPortLocal = replicationPortLocal;

        this.replicationHostLocal = getHost(replicationHostLocal);

        this.servicePool = new ServicePool(eventBusName, null);


        endpointDefinition = serviceDiscovery.registerWithTTL(eventBusName, replicationPortLocal,
                (int) replicationServerCheckInTimeUnit.toSeconds(replicationServerCheckInInterval));

        serviceDiscovery.checkInOk(endpointDefinition.getId());
    }

    private String getHost(String replicationHostLocal) {
        try {
            return replicationHostLocal == null ? InetAddress.getLocalHost().getHostAddress() : replicationHostLocal;
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /* These are used for spring integration. Do not delete. */
    public EventManager eventManagerImpl() {
        return eventManagerImpl;
    }

    /* These are used for spring integration. Do not delete. */
    public ServiceQueue eventServiceQueue() {
        return eventServiceQueue;
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

    private EventManager createEventManager() {
        eventManagerImpl = eventManagerBuilder().setEventConnector(eventConnectorHub).build(eventBusName);
        eventServiceQueue = serviceBuilder().setServiceObject(eventManagerImpl).build();

        return eventServiceQueue.createProxyWithAutoFlush(
                EventManager.class, periodicScheduler, 100, TimeUnit.MILLISECONDS);
    }


    public EventManager createClientEventManager() {
        return eventServiceQueue().createProxy(EventManager.class);
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

        if (!replicationHostLocal.equals("localhost")) {
            replicatorBuilder.serviceServerBuilder().setHost(replicationHostLocal);
        }
        replicatorBuilder.setEventManager(eventManager);
        serviceEndpointServerForReplicator = replicatorBuilder.build();
        serviceEndpointServerForReplicator.start();
    }


    private void healthyNodeMonitor() {


        if (debug) logger.debug("EventBusCluster::healthyNodeMonitor " + eventConnectorHub.size());
        final List<EndpointDefinition> endpointDefinitions = serviceDiscovery.loadServices(eventBusName);
        final List<EndpointDefinition> removeNodes = new ArrayList<>();

        final AtomicBoolean change = new AtomicBoolean();

        servicePool.setHealthyNodes(endpointDefinitions, new ServicePoolListener() {
            @Override
            public void servicePoolChanged(final String serviceName) {

                if (serviceName.equals(eventBusName)) {

                    change.set(true);
                    logger.info("EventBusCluster:: Service pool changed " + eventBusName);
                } else if (debug) {
                    logger.debug("EventBusCluster:: some other pool changed - Service pool changed " + eventBusName);

                }
            }

            @Override
            public void serviceAdded(String serviceName, EndpointDefinition endpointDefinition) {
                if (serviceName.equals(eventBusName)) {


                    if (replicationHostLocal.equals(endpointDefinition.getHost()) &&
                            replicationPortLocal == endpointDefinition.getPort()) {
                        if (debug)
                            logger.debug("EventBusCluster:: Add event for self " + eventBusName + " " + endpointDefinition);
                    } else {
                        change.set(true);
                        addEventConnector(endpointDefinition.getHost(), endpointDefinition.getPort());
                        logger.info("EventBusCluster:: Adding event connector " + eventBusName + " " + endpointDefinition);
                    }
                }
            }

            @Override
            public void serviceRemoved(String serviceName, EndpointDefinition endpointDefinition) {
                if (serviceName.equals(eventBusName)) {

                    if (replicationHostLocal.equals(endpointDefinition.getHost()) &&
                            replicationPortLocal == endpointDefinition.getPort()) {
                        if (debug)
                            logger.debug("EventBusCluster:: Remove event for self " + eventBusName + " " + endpointDefinition);
                    } else {
                        change.set(true);
                        removeNodes.add(endpointDefinition);
                        logger.info("EventBusCluster:: Removing event connector " + eventBusName + " " + endpointDefinition);

                    }
                }

            }

        });


        if (change.get()) {
            if (removeNodes.size() > 0) {
                removeServices(removeNodes);
            }
        } else {
            removeBadServices();
        }


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

    private int removeServices(List<EndpointDefinition> removeServicesList) {
        final ListIterator<EventConnector> listIterator = eventConnectorHub.listIterator();

        int removeCount = 0;
        final List<EventConnector> connectorsToRemove = new ArrayList<>();

        while (listIterator.hasNext()) {
            final EventConnector connector = listIterator.next();


            /** Remove ones in the removeServicesList. */
            if (connector instanceof RemoteTCPClientProxy) {

                final RemoteTCPClientProxy remoteTCPClientProxy = (RemoteTCPClientProxy) connector;
                final String host = remoteTCPClientProxy.host();
                final int port = remoteTCPClientProxy.port();
                boolean found = false;
                for (EndpointDefinition serviceHealth : removeServicesList) {
                    final int healthyPort = serviceHealth.getPort();
                    final String healthyHost = serviceHealth.getHost();

                    if (healthyPort == port && healthyHost.equals(host)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    removeCount++;
                    connectorsToRemove.add(connector);
                }
            }
        }

        connectorsToRemove.forEach(eventConnectorHub::remove);


        return removeCount;
    }


    private int removeBadServices() {
        final ListIterator<EventConnector> listIterator = eventConnectorHub.listIterator();

        int removeCount = 0;
        final List<EventConnector> badConnectors = new ArrayList<>();

        while (listIterator.hasNext()) {
            final EventConnector connector = listIterator.next();


            /** Remove connections that are closed. */
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


            }

        }


        /* Remove the closed ones. */
        badConnectors.forEach(eventConnectorHub::remove);

        /* Now add them back again. */
        badConnectors.forEach(eventConnector -> {
            final RemoteTCPClientProxy remoteTCPClientProxy = (RemoteTCPClientProxy) eventConnector;
            addEventConnector(remoteTCPClientProxy.host(), remoteTCPClientProxy.port());
        });
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

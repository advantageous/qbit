package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Str;
import io.advantageous.consul.Consul;
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.NotRegisteredException;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.consul.domain.option.RequestOptionsBuilder;
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
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
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
    private final String consulHost;
    private final int consulPort;
    private final String datacenter;
    private final String tag;
    private final int longPollTimeSeconds;
    private final String localEventBusId;
    private final int replicationPortLocal;
    private final String replicationHostLocal;
    private final EventManager eventManager;
    private final int replicationServerCheckInIntervalInSeconds;

    private final Logger logger = LoggerFactory.getLogger(EventBusCluster.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean info = logger.isInfoEnabled();

    private AtomicInteger lastIndex = new AtomicInteger();
    private RequestOptions requestOptions;
    private AtomicReference<Consul> consul = new AtomicReference<>();
    private ScheduledFuture healthyNodeMonitor;
    private ScheduledFuture consulCheckInMonitor;
    private ServiceEndpointServer serviceEndpointServerForReplicator;
    private ServiceQueue eventServiceQueue;

    private EventManager eventManagerImpl;


    /* Used to manage consul retry logic. */
    private int consulRetryCount = 0;
    private long lastResetTimestamp = Timer.clockTime();

    public EventBusCluster(final EventManager eventManager,
                           final String eventBusName,
                           final String localEventBusId,
                           final EventConnectorHub eventConnectorHub,
                           final PeriodicScheduler periodicScheduler,
                           final int peerCheckTimeInterval,
                           final TimeUnit timeunit,
                           final String consulHost,
                           final int consulPort,
                           final int longPollTimeSeconds,
                           final int replicationPortLocal,
                           final String replicationHostLocal,
                           final String datacenter,
                           final String tag,
                           final int replicationServerCheckInIntervalInSeconds) {

        this.eventBusName = eventBusName;
        this.eventConnectorHub = eventConnectorHub == null ? new EventConnectorHub() : eventConnectorHub;
        this.periodicScheduler = periodicScheduler == null ?
                QBit.factory().periodicScheduler() : periodicScheduler;
        this.peerCheckTimeInterval = peerCheckTimeInterval;
        this.peerCheckTimeUnit = timeunit;
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.consul.set(Consul.consul(consulHost, consulPort));
        this.datacenter = datacenter;
        this.tag = tag;
        this.longPollTimeSeconds = longPollTimeSeconds;
        this.localEventBusId = localEventBusId;
        this.replicationPortLocal = replicationPortLocal;
        this.replicationHostLocal = replicationHostLocal;
        this.eventManager = eventManager == null ? createEventManager() : wrapEventManager(eventManager);
        this.replicationServerCheckInIntervalInSeconds = replicationServerCheckInIntervalInSeconds;

        buildRequestOptions();
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

    public EventManager eventManagerImpl() {
        return eventManagerImpl;
    }

    private EventManager createEventManager() {
        eventManagerImpl = eventManagerBuilder().setEventConnector(eventConnectorHub).build("foo");
        eventServiceQueue = serviceBuilder().setServiceObject(eventManagerImpl).build();

        return eventServiceQueue.createProxyWithAutoFlush(
                EventManager.class, periodicScheduler, 100, TimeUnit.MILLISECONDS);
    }

    public ServiceQueue eventServiceQueue() {
        return eventServiceQueue;
    }

    @Override
    public void start() {

        consul.get().start();

        if (eventServiceQueue != null) {
            eventServiceQueue.start();
        }

        startServerReplicator();

        registerLocalBusInConsul();

        healthyNodeMonitor = periodicScheduler.repeat(
                this::healthyNodeMonitor, peerCheckTimeInterval, peerCheckTimeUnit);

        if (replicationServerCheckInIntervalInSeconds > 2) {
            consulCheckInMonitor = periodicScheduler.repeat(this::checkInWithConsul,
                    replicationServerCheckInIntervalInSeconds / 2, TimeUnit.SECONDS);
        } else {
            consulCheckInMonitor = periodicScheduler.repeat(this::checkInWithConsul, 100, TimeUnit.MILLISECONDS);
        }
    }

    private void checkInWithConsul() {
        try {
            consul.get().agent().pass(localEventBusId, "still running");
        } catch (NotRegisteredException ex) {
            registerLocalBusInConsul();
        } catch (Exception ex) {
            consulRetryCount++;
            logger.warn("Unable to check-in with consul", ex);
            if (consulRetryCount > 10) {

                logger.info("Exceeded retry count with consul");
                final long now = Timer.clockTime();
                final long duration = now - lastResetTimestamp;

                if (duration > 180_000) {

                    logger.info("Resetting retry count");
                    lastResetTimestamp = now;
                    consulRetryCount = 0;
                }

            } else {
                Consul oldConsul = consul.get();
                consul.compareAndSet(oldConsul, startNewConsul(oldConsul));
            }
        }
    }

    private Consul startNewConsul(final Consul oldConsul) {

        if (oldConsul != null) {
            try {
                oldConsul.stop();
            } catch (Exception ex) {
                logger.debug("Unable to stop old consul", ex);
            }
        }

        final Consul consul = Consul.consul(consulHost, consulPort);
        consul.start();
        return consul;
    }

    private void registerLocalBusInConsul() {
        consul.get().agent().registerService(replicationPortLocal,
                replicationServerCheckInIntervalInSeconds, eventBusName, localEventBusId, tag);
    }

    private void startServerReplicator() {
        final List<ServiceHealth> healthyServices = getHealthyServices();

        final List<ServiceHealth> newServices = findNewServices(healthyServices);
        addNewServicesToHub(newServices);


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

    private void showHealthyServices(List<ServiceHealth> healthyServices) {
        puts("SHOW HEALTHY SERVICES");

        healthyServices.forEach(serviceHealth -> {
            puts("----------------------------");
            puts("node", eventBusName, serviceHealth.getService().getPort(), serviceHealth.getNode().getAddress());
            puts("----------------------------");

        });
    }

    private List<ServiceHealth> getHealthyServices() {
        final ConsulResponse<List<ServiceHealth>> consulResponse = consul.get().health()
                .getHealthyServices(eventBusName, datacenter, tag, requestOptions);
        this.lastIndex.set(consulResponse.getIndex());

        final List<ServiceHealth> healthyServices = consulResponse.getResponse();

        if (debug) {
            showHealthyServices(healthyServices);
        }
        buildRequestOptions();
        return healthyServices;
    }

    private void buildRequestOptions() {
        this.requestOptions = new RequestOptionsBuilder()
                .consistency(Consistency.CONSISTENT)
                .blockSeconds(longPollTimeSeconds, lastIndex.get()).build();
    }

    private void healthyNodeMonitor() {

        try {
            rebuildHub(getHealthyServices());
        } catch (Exception ex) {
            logger.error("unable to contact consul or problems rebuilding event hub", ex);
            Consul oldConsul = consul.get();
            consul.compareAndSet(oldConsul, startNewConsul(oldConsul));
        }

    }

    private void rebuildHub(List<ServiceHealth> services) {

        if (debug) logger.debug(
                String.format("Number of services before %s ",
                        eventConnectorHub.size()));

        int removeCount = removeBadServices(services);

        if (info && removeCount > 1) logger.info(
                String.format("Number of services AFTER remove bad service called %s remove count %s",
                        eventConnectorHub.size(), removeCount));

        List<ServiceHealth> newServices = findNewServices(services);


        if (newServices.size() > 0) {
            addNewServicesToHub(newServices);
            if (info) logger.info(
                    String.format("Number of services found %s total connectors %s",
                            newServices.size(), eventConnectorHub.size()));
        }


    }

    private void addNewServicesToHub(final List<ServiceHealth> newServices) {
        for (ServiceHealth serviceHealth : newServices) {

            final int newPort = serviceHealth.getService().getPort();
            final String newHost = serviceHealth.getNode().getAddress();
            addEventConnector(newHost, newPort);

        }
    }

    private List<ServiceHealth> findNewServices(List<ServiceHealth> services) {
        List<ServiceHealth> newServices = new ArrayList<>();


        for (ServiceHealth serviceHealth : services) {
            final int healthyPort = serviceHealth.getService().getPort();
            final String healthyHost = serviceHealth.getNode().getAddress();

            /* Don't return yourself. */
            if (serviceHealth.getService().getId().equals(localEventBusId)) {
                continue;
            }

            boolean found = false;

            for (EventConnector connector : eventConnectorHub) {
                if (connector instanceof RemoteTCPClientProxy) {
                    final String host = ((RemoteTCPClientProxy) connector).host();
                    final int port = ((RemoteTCPClientProxy) connector).port();

                    if (healthyPort == port && healthyHost.equals(host)) {
                        found = true;
                        break;
                    }

                }
            }

            if (!found) {
                newServices.add(serviceHealth);
            }

        }
        return newServices;
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

    private int removeBadServices(List<ServiceHealth> services) {
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
                for (ServiceHealth serviceHealth : services) {
                    final int healthyPort = serviceHealth.getService().getPort();
                    final String healthyHost = serviceHealth.getNode().getAddress();

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
            consul.get().stop();
        } catch (Exception ex) {
            logger.warn("EventBusCluster is unable to stop consul");
        }

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

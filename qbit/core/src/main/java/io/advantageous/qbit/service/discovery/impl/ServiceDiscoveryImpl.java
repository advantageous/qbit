package io.advantageous.qbit.service.discovery.impl;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service Discovery using consul
 * created by rhightower on 3/23/15.
 */
public class ServiceDiscoveryImpl implements ServiceDiscovery {

    private final PeriodicScheduler periodicScheduler;
    private final BlockingQueue<String> doneQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<ServiceHealthCheckIn> checkInsQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<EndpointDefinition> registerQueue = new LinkedTransferQueue<>();
    private final ServiceChangedEventChannel serviceChangedEventChannel;
    private final ServicePoolListener servicePoolListener;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, ServicePool> servicePoolMap = new ConcurrentHashMap<>();

    private final ConcurrentHashSet<EndpointDefinition> endpointDefinitions = new ConcurrentHashSet<>();
    private final ServiceDiscoveryProvider provider;
    private final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryImpl.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean trace = logger.isTraceEnabled();
    private final int pollForServicesInterval;
    private final ServiceDiscoveryProvider backupProvider;
    private final ConcurrentHashSet<String> serviceNamesBeingLoaded = new ConcurrentHashSet<>();
    private final AtomicBoolean stop = new AtomicBoolean();
    private final Set<String> serviceNames = new TreeSet<>();


    public ServiceDiscoveryImpl(
            final PeriodicScheduler periodicScheduler,
            final ServiceChangedEventChannel serviceChangedEventChannel,
            final ServiceDiscoveryProvider provider,
            final ServiceDiscoveryProvider backupProvider,
            final ServicePoolListener servicePoolListener,
            final ExecutorService executorService,
            final int pollForServicesInterval) {


        this.backupProvider = backupProvider;
        this.provider = provider;
        this.pollForServicesInterval = pollForServicesInterval;

        this.periodicScheduler =
                periodicScheduler == null ? QBit.factory().periodicScheduler() : periodicScheduler;

        this.serviceChangedEventChannel = serviceChangedEventChannel == null ?
                serviceName -> {

                } : serviceChangedEventChannel;

        this.servicePoolListener = servicePoolListener == null ? serviceName -> {
        } : servicePoolListener;

        this.executorService = executorService == null ?
                Executors.newFixedThreadPool(10) :
                executorService;//Mostly sleeping threads doing long polls


        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl created" + provider
            );
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!stop.get()) stop();
        }));

    }

    public EndpointDefinition registerWithTTL(
            final String serviceName,
            final int port,
            final int timeToLiveSeconds) {


        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::registerWithTTL() " + serviceName + " " + port
            );
        }

        watch(serviceName);


        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceName + "-" + ServiceDiscovery.uniqueString(port),
                serviceName, null, port, timeToLiveSeconds);

        return doRegister(endpointDefinition);
    }

    public EndpointDefinition registerWithIdAndTimeToLive(
            final String serviceName, final String serviceId, final int port, final int timeToLiveSeconds) {


        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::registerWithIdAndTimeToLive() " + serviceName + " " + port
            );
        }

        watch(serviceName);
        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, null, port, timeToLiveSeconds);


        return doRegister(endpointDefinition);
    }

    private EndpointDefinition doRegister(EndpointDefinition endpointDefinition) {

        endpointDefinitions.add(endpointDefinition);
        registerQueue.offer(endpointDefinition);

        return endpointDefinition;
    }

    @Override
    public EndpointDefinition register(final String serviceName, final int port) {

        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::register()" + serviceName + " " + port
            );
        }

        watch(serviceName);


        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceName + "-" + ServiceDiscovery.uniqueString(port),
                serviceName, null, port);

        return doRegister(endpointDefinition);

    }

    @Override
    public EndpointDefinition registerWithId(final String serviceName, final String serviceId, final int port) {

        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::registerWithId()" + serviceName + " " + port
            );
        }

        watch(serviceName);


        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, null, port);

        return doRegister(endpointDefinition);

    }

    @Override
    public void watch(String serviceName) {
        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::watch()" + serviceName
            );
        }
        if (!serviceNames.contains(serviceName)) {
            serviceNames.add(serviceName);
            doneQueue.offer(serviceName);
        }

    }

    @Override
    public void checkIn(final String serviceId, final HealthStatus healthStatus) {


        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::checkIn()" + serviceId, healthStatus
            );
        }

        checkInsQueue.offer(new ServiceHealthCheckIn(serviceId, healthStatus));


    }

    @Override
    public void checkInOk(final String serviceId) {


        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::checkInOk()" + serviceId
            );
        }

        checkInsQueue.offer(new ServiceHealthCheckIn(serviceId, HealthStatus.PASS));


    }

    public ServicePool servicePool(final String serviceName) {
        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool == null) {
            servicePool = new ServicePool(serviceName, this.servicePoolListener);
            servicePoolMap.put(serviceName, servicePool);
        }
        return servicePool;
    }

    @Override
    public List<EndpointDefinition> loadServices(final String serviceName) {


        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::loadServices()" + serviceName
            );
        }

        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool == null) {
            servicePool = new ServicePool(serviceName, this.servicePoolListener);
            servicePoolMap.put(serviceName, servicePool);
            watch(serviceName);
            return Collections.emptyList();
        }
        return servicePool.services();
    }

    public List<EndpointDefinition> loadServicesNow(final String serviceName) {


        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::loadServices()" + serviceName
            );
        }

        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool == null) {
            servicePool = new ServicePool(serviceName, this.servicePoolListener);
            servicePoolMap.put(serviceName, servicePool);
            try {
                final List<EndpointDefinition> healthyServices = provider.loadServices(serviceName);
                servicePool.setHealthyNodes(healthyServices, this.servicePoolListener);
            } catch (Exception ex) {
                logger.warn("Unable to load healthy nodes from primary service discovery provider", ex);
                final List<EndpointDefinition> healthyServices = backupProvider.loadServices(serviceName);
                servicePool.setHealthyNodes(healthyServices, this.servicePoolListener);

            }
            watch(serviceName);
        }
        return servicePool.services();


    }

    @Override
    public void start() {

        if (debug) {
            logger.debug("Starting Service Discovery " + provider);
        }

        this.periodicScheduler.repeat(() -> {
            try {
                monitor();
            } catch (InterruptedException e) {
                logger.debug("ServiceDiscoveryImpl::" +
                        "Error while running monitor", e);
            }
        }, pollForServicesInterval, TimeUnit.MILLISECONDS);
    }

    public void monitor() throws InterruptedException {

        while (!stop.get()) {
            loadHealthyServices();
            provider.registerServices(registerQueue);
            provider.checkIn(checkInsQueue);
        }
    }

    private void loadHealthyServices() throws InterruptedException {
        String serviceName = doneQueue.poll(50, TimeUnit.MILLISECONDS);

        while (serviceName != null) {

            final String serviceNameToFetch = serviceName;

            if (!serviceNamesBeingLoaded.contains(serviceNameToFetch)) {
                serviceNamesBeingLoaded.add(serviceNameToFetch);
                executorService.submit(() -> {
                    try {
                        final List<EndpointDefinition> healthyServices = provider.loadServices(serviceNameToFetch);
                        populateServiceMap(serviceNameToFetch, healthyServices);
                        serviceNamesBeingLoaded.remove(serviceNameToFetch);
                    } catch (Exception ex) {

                        Sys.sleep(10_000); //primary is down so slow it down
                        if (backupProvider != null) {

                            if (debug) logger.debug("ServiceDiscoveryImpl::loadHealthyServices " +
                                    "Error while loading healthy" +
                                    " services for " + serviceNameToFetch, ex);

                            final List<EndpointDefinition> healthyServices = backupProvider.loadServices(serviceNameToFetch);
                            populateServiceMap(serviceNameToFetch, healthyServices);
                            serviceNamesBeingLoaded.remove(serviceNameToFetch);


                        } else {
                            logger.error("ServiceDiscoveryImpl::loadHealthyServices " +
                                    "Error while loading healthy" +
                                    " services for " + serviceNameToFetch, ex);
                        }
                    } finally {
                        doneQueue.offer(serviceNameToFetch);
                    }
                });
            }
            serviceName = doneQueue.poll();
        }
    }


    private void populateServiceMap(final String serviceName, final List<EndpointDefinition> healthyServices) {
        final ServicePool servicePool = servicePool(serviceName);
        if (servicePool.setHealthyNodes(healthyServices)) {
            serviceChangedEventChannel.servicePoolChanged(serviceName);
            serviceChangedEventChannel.flushEvents();
        }
    }


    @Override
    public void stop() {

        if (debug) {
            logger.debug("Stopping Service Discovery");
        }

        provider.unregisterServices(endpointDefinitions);
        this.periodicScheduler.stop();
        this.stop.set(true);

    }

    public Set<EndpointDefinition> localDefinitions() {
        return endpointDefinitions;
    }
}

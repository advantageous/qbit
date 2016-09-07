package io.advantageous.qbit.service.discovery.impl;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.util.ConcurrentHashSet;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Service Discovery. This is a generic service discovery class.
 * It has two providers. If the primary provider fails, it uses the secondary provider.
 * <p>
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
    private final int pollForServicesIntervalMS;
    private final int checkInIntervalInMS;

    private final ServiceDiscoveryProvider backupProvider;
    private final ConcurrentHashSet<String> serviceNamesBeingLoaded = new ConcurrentHashSet<>();
    private final AtomicBoolean stop = new AtomicBoolean();
    private final Set<String> serviceNames = new TreeSet<>();
    private final ConcurrentHashMap<String, BlockingQueue<Callback<List<EndpointDefinition>>>>
            callbackMap = new ConcurrentHashMap<>();
    private long lastCheckIn;

    public ServiceDiscoveryImpl(
            final PeriodicScheduler periodicScheduler,
            final ServiceChangedEventChannel serviceChangedEventChannel,
            final ServiceDiscoveryProvider provider,
            final ServiceDiscoveryProvider backupProvider,
            final ServicePoolListener servicePoolListener,
            final ExecutorService executorService,
            final int pollForServicesIntervalSeconds,
            final int checkInIntervalInSeconds) {

        this.backupProvider = backupProvider;
        this.checkInIntervalInMS = checkInIntervalInSeconds * 1000;
        this.provider = provider;
        this.pollForServicesIntervalMS = pollForServicesIntervalSeconds * 1000;

        this.periodicScheduler =
                periodicScheduler == null ? QBit.factory().periodicScheduler() : periodicScheduler;

        this.serviceChangedEventChannel = serviceChangedEventChannel == null ?
                serviceName -> {

                } : serviceChangedEventChannel;

        this.servicePoolListener = servicePoolListener == null ? serviceName -> {
        } : servicePoolListener;

        this.executorService = executorService == null ?
                Executors.newCachedThreadPool(runnable -> new Thread(runnable, "ServiceDiscovery")) :
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

    @Override
    public EndpointDefinition registerWithTTL(
            final String serviceName,
            final String host,
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
                serviceName, host, port, timeToLiveSeconds);

        return doRegister(endpointDefinition);
    }

    @Override
    public EndpointDefinition registerWithIdAndTimeToLive(
            final String serviceName, final String serviceId, String host, final int port, final int timeToLiveSeconds) {

        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::registerWithIdAndTimeToLive() " + serviceName + " " + port
            );
        }

        watch(serviceName);
        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, host, port, timeToLiveSeconds);

        return doRegister(endpointDefinition);
    }

    @Override
    public EndpointDefinition registerWithIdAndTTLAndTags(
            final String serviceName, final String serviceId, String host, final int port, final int timeToLiveSeconds, List<String> tags) {

        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::registerWithIdAndTimeToLive() " + serviceName + " " + port
            );
        }

        watch(serviceName);
        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, host, port, timeToLiveSeconds, tags);

        return doRegister(endpointDefinition);
    }

    private EndpointDefinition doRegister(EndpointDefinition endpointDefinition) {

        endpointDefinitions.add(endpointDefinition);
        registerQueue.offer(endpointDefinition);

        return endpointDefinition;
    }

    @Override
    public EndpointDefinition register(final String serviceName, String host, final int port) {

        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::register()" + serviceName + " " + host + ":" + port
            );
        }

        watch(serviceName);

        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceName + "-" + ServiceDiscovery.uniqueString(port),
                serviceName, host, port);

        return doRegister(endpointDefinition);

    }

    @Override
    public EndpointDefinition registerWithId(final String serviceName, final String serviceId, String host, final int port) {

        if (trace) {
            logger.trace(
                    "ServiceDiscoveryImpl::registerWithId()" + serviceName + " " + host + ":" + port
            );
        }

        watch(serviceName);

        EndpointDefinition endpointDefinition = new EndpointDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, host, port);

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

    public void loadServicesAsync(Callback<List<EndpointDefinition>> callback, final String serviceName) {
        final List<EndpointDefinition> endpointDefinitions = loadServices(serviceName);
        if (endpointDefinitions.size() > 0) {
            callback.accept(endpointDefinitions);
            return;
        }

        BlockingQueue<Callback<List<EndpointDefinition>>> callbacks
                = callbackMap.get(serviceName);

        if (callbacks == null) {
            callbacks = new ArrayBlockingQueue<>(200);
            try {
                callbacks.put(callback);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            callbackMap.put(serviceName, callbacks);
        } else {
            try {
                if (callbacks.size() == 0) {
                    callbacks.put(callback);
                    serviceNamesBeingLoaded.remove(serviceName);
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
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

        if (debug) {
            logger.debug("ServiceDiscoveryImpl::loadServicesNow {}", serviceName);
        }

        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool == null || servicePool.services() == null || servicePool.services().size() == 0) {
            servicePool = new ServicePool(serviceName, this.servicePoolListener);
            servicePoolMap.put(serviceName, servicePool);
            try {
                final List<EndpointDefinition> healthyServices = provider.loadServices(serviceName);
                if (debug) {
                    logger.debug("ServiceDiscoveryImpl::loadServicesNow {} healthyServices {}", serviceName, healthyServices);
                }
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
                logger.info("Starting Service Discovery monitor");
                monitor();
            } catch (Exception e) {
                logger.error("ServiceDiscoveryImpl::" +
                        "Error while running monitor", e);
            }
        }, pollForServicesIntervalMS, TimeUnit.MILLISECONDS);
    }

    public void monitor() throws Exception {

        while (!stop.get()) {

            if (registerQueue.size() > 0) {
                provider.registerServices(registerQueue);
            }

            if (doneQueue.size() > 0) {
                executorService.submit(() -> {

                    /* There is no rush, we are periodically checking in.
                    * Protect the service registry from too aggressive config. */
                    loadHealthyServices();
                });
            }

            if (checkInsQueue.size() > 0) {
                provider.checkIn(checkInsQueue);
            }

            if (registerQueue.size() == 0) {
                Sys.sleep(pollForServicesIntervalMS);
            }

            if (doneQueue.size() == 0) {
                long now = Timer.timer().now();
                long duration = now - lastCheckIn;
                if (duration > checkInIntervalInMS) {
                    lastCheckIn = now;
                    doneQueue.addAll(serviceNames);
                }
            }
        }
    }

    /**
     * Iterate through the health service queue and load the services.
     */
    private void loadHealthyServices() {

        try {
            String serviceName = doneQueue.poll();

            while (serviceName != null) {

                final String serviceNameToFetch = serviceName;

                /* Don't load the service if it is already being loaded. */
                if (!serviceNamesBeingLoaded.contains(serviceNameToFetch)) {
                    serviceNamesBeingLoaded.add(serviceNameToFetch);
                    executorService.submit(() -> {
                     /*
                       Loading a service pool might take a while so
                       the actual load operation happens in its own thread.
                      */
                        doLoadHealthServices(serviceNameToFetch);
                    });
                }
                serviceName = doneQueue.poll();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads the service from the remote service registry (i.e., consul).
     *
     * @param serviceNameToFetch service that we are loading a pool for.
     */
    private void doLoadHealthServices(final String serviceNameToFetch) {

        try {
            final List<EndpointDefinition> healthyServices = provider.loadServices(serviceNameToFetch);
            populateServiceMap(serviceNameToFetch, healthyServices);
        } catch (Exception ex) {
            doFailOverHealthServicesLoad(serviceNameToFetch, ex);
        } finally {
            /*  Remove the service from the serviceNamesBeingLoaded
                SET and add it back to the work pool
                to get loaded again.
                 We are constantly loading services through long polling for changes.
             */
            serviceNamesBeingLoaded.remove(serviceNameToFetch);
        }

    }

    /**
     * If the primary load failed, we could have a backup provider registered.
     *
     * @param serviceNameToFetch service pool to fetch
     * @param ex
     */
    private void doFailOverHealthServicesLoad(final String serviceNameToFetch, Exception ex) {

        /* If there is a backup provider, load from there. */
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

        Sys.sleep(10_000); //primary is down so slow it down so we don't flow the system with updates of service pools.

    }

    /**
     * Populate the service map.
     * Look up the service pool.
     * Apply the healthy services so the pool can see if there were changes (additions, removal, etc.)
     *
     * @param serviceName     service name
     * @param healthyServices list of healthy services that we just loaded.
     */
    private void populateServiceMap(final String serviceName,
                                    final List<EndpointDefinition> healthyServices) {

        final ServicePool servicePool = servicePool(serviceName);

        /* If there were changes then send a service pool change event on the event channel. */
        if (servicePool.setHealthyNodes(healthyServices)) {
            serviceChangedEventChannel.servicePoolChanged(serviceName);
            serviceChangedEventChannel.flushEvents();
        }

        final BlockingQueue<Callback<List<EndpointDefinition>>> callbacks = callbackMap.get(serviceName);

        if (callbacks != null) {
            Callback<List<EndpointDefinition>> callback = callbacks.poll();
            while (callback != null) {
                callback.reply(healthyServices);
                callback = callbacks.poll();
            }
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

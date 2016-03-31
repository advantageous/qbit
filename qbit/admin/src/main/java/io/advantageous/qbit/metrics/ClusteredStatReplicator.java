package io.advantageous.qbit.metrics;

import io.advantageous.boon.core.Pair;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.core.Str.sputs;


/**
 * Clustered Stat Replicator
 * created by rhightower on 3/24/15.
 */
public class ClusteredStatReplicator implements StatReplicator, ServiceChangedEventChannel {


    private final ServiceDiscovery serviceDiscovery;
    private final StatReplicatorProvider statReplicatorProvider;
    private final ConcurrentHashMap<String, Pair<EndpointDefinition, StatReplicator>>
            replicatorsMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ClusteredStatReplicator.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean trace = logger.isTraceEnabled();
    private final String serviceName;
    private final ServicePool servicePool;
    private final String localServiceId;
    private final Timer timer;
    private final int tallyInterval;
    private final int flushInterval;
    private final ConcurrentHashMap<String, LocalCount> countMap = new ConcurrentHashMap<>();
    private long currentTime;
    private long lastReconnectTime;
    private long lastSendTime;
    private long lastReplicatorFlush = 0;
    private List<Pair<EndpointDefinition, StatReplicator>> statReplicators = new ArrayList<>();


    public ClusteredStatReplicator(final String serviceName,
                                   final ServiceDiscovery serviceDiscovery,
                                   final StatReplicatorProvider statReplicatorProvider,
                                   final String localServiceId,
                                   final Timer timer,
                                   final int tallyInterval,
                                   final int flushInterval) {
        this.serviceDiscovery = serviceDiscovery;
        this.statReplicatorProvider = statReplicatorProvider;
        this.serviceName = serviceName;
        this.localServiceId = localServiceId;
        this.servicePool = new ServicePool(serviceName, null);
        this.timer = timer;
        this.tallyInterval = tallyInterval;
        this.flushInterval = flushInterval;

    }

    @Override
    public void replicateCount(final String name, final long count, final long now) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::replicateCount()",
                serviceName, name, count, now));

        if (debug) {
            if (statReplicators.size() == 0) {
                logger.debug(sputs("ClusteredStatReplicator::replicateCount", name, count, now));
            }
        }

        LocalCount localCount = countMap.get(name);

        if (localCount == null) {
            localCount = new LocalCount();
            localCount.name = name;
            countMap.put(name, localCount);
        }
        localCount.count += count;


    }

    @Override
    public void replicateLevel(final String name, long level, long time) {

        LocalCount localCount = countMap.get(name);

        if (localCount == null) {
            localCount = new LocalCount();
            localCount.name = name;
            countMap.put(name, localCount);
        }
        localCount.count = level;
    }

    @Override
    public void replicateTiming(String name, long level, long time) {

        LocalCount localCount = countMap.get(name);

        if (localCount == null) {
            localCount = new LocalCount();
            localCount.name = name;
            countMap.put(name, localCount);
        }
        localCount.count = level;
    }

    private void doRecordCount(Pair<EndpointDefinition, StatReplicator> statReplicator,
                               final String name, final long count, final long now) {

        try {
            statReplicator.getSecond().replicateCount(name, count, now);
        } catch (Exception ex) {
            logger.error(sputs("ClusteredStatReplicator::Replicator failed", statReplicator), ex);
        }
    }

    @QueueCallback({QueueCallbackType.IDLE,
            QueueCallbackType.EMPTY,
            QueueCallbackType.LIMIT})
    void process() {


        currentTime = timer.now();

        sendIfNeeded();
        checkForReconnect();
    }

    private void sendIfNeeded() {

        long duration = currentTime - lastSendTime;

        if (duration > tallyInterval) {
            this.lastSendTime = currentTime;

            final Collection<LocalCount> countCollection = this.countMap.values();


            for (LocalCount localCount : countCollection) {

                if (localCount.count > 0) {
                    statReplicators.forEach(
                            statReplicator -> doRecordCount(statReplicator, localCount.name, localCount.count, currentTime)
                    );
                }
                localCount.count = 0;
            }
            if (countMap.size() > 10_000_000) {
                countMap.clear();
            }
            flushReplicatorsAll();
        }


    }

    private void flushReplicatorsAll() {

        if (currentTime - lastReplicatorFlush > flushInterval) {
            lastReplicatorFlush = currentTime;

            final List<Pair<EndpointDefinition, StatReplicator>> badReplicators = new ArrayList<>();
            statReplicators.forEach(
                    statReplicator -> flushReplicator(statReplicator, badReplicators)
            );
            badReplicators.forEach(statReplicator -> {

                        try {
                            statReplicator.getSecond().stop();
                        } catch (Exception ex) {
                            logger.info("Failed to stop failed node", ex);
                        }
                        statReplicators.remove(statReplicator);
                        replicatorsMap.remove(statReplicator.getFirst().getId());
                    }
            );

            if (trace) {

                logger.trace(sputs("ClusteredStatReplicator::flushReplicatorsAll()",
                        badReplicators.size()));
                badReplicators.forEach(statReplicator -> logger.debug(sputs(statReplicator)));
            }
        }
    }

    private void checkForReconnect() {
        long duration = currentTime - lastReconnectTime;
        if (duration > 60_000) {
            doCheckReconnect();
        }

    }

    public void doCheckReconnect() {

        lastReconnectTime = currentTime;
        final List<EndpointDefinition> services = servicePool.services();


        if ((services.size() - 1) != this.statReplicators.size()) {
            logger.info(sputs("DOING RECONNECT", services.size() - 1,
                    this.statReplicators.size()));

            shutDownReplicators();
            services.forEach(this::addService);
        }
    }


    private void shutDownReplicators() {
        if (debug) logger.debug("Shutting down replicators");
        for (Pair<EndpointDefinition, StatReplicator> statReplicator : statReplicators) {
            try {
                statReplicator.getSecond().stop();

            } catch (Exception ex) {
                logger.debug("Shutdown replicator failed", ex);
            }

            if (debug) logger.debug("Shutting down replicator");
        }
        statReplicators.clear();
        replicatorsMap.clear();
    }

    private void flushReplicator(final Pair<EndpointDefinition, StatReplicator> statReplicator,
                                 final List<Pair<EndpointDefinition, StatReplicator>> badReplicators) {


        try {
            ServiceProxyUtils.flushServiceProxy(statReplicator.getSecond());
        } catch (Exception exception) {
            badReplicators.add(statReplicator);
            logger.info("Replicator failed " + statReplicator, exception);
        }
    }

    /**
     * Event handler
     *
     * @param serviceName service name
     */
    @Override
    public void servicePoolChanged(final String serviceName) {
        if (this.serviceName.equals(serviceName)) {
            logger.info("ClusteredStatReplicator::servicePoolChanged({})", serviceName);
            updateServicePool(serviceName);
        } else if (debug) {
            logger.debug("ClusteredStatReplicator::servicePoolChanged({})",
                    "got event for another service", serviceName);
        }
    }

    private void updateServicePool(final String serviceName) {


        try {
            final List<EndpointDefinition> nodes = serviceDiscovery.loadServices(serviceName);
            servicePool.setHealthyNodes(nodes, new ServicePoolListener() {
                @Override
                public void servicePoolChanged(String serviceName) {
                }

                @Override
                public void serviceAdded(String serviceName, EndpointDefinition endpointDefinition) {
                    addService(endpointDefinition);
                }

                @Override
                public void serviceRemoved(String serviceName, EndpointDefinition endpointDefinition) {
                    removeService(endpointDefinition);
                }

            });
        } catch (Exception ex) {
            logger.error("Error updating service pool");
        }
    }

    private void removeService(final EndpointDefinition endpointDefinition) {

        logger.info(sputs("ClusteredStatReplicator::removeService()",
                serviceName, endpointDefinition, " replicator count ", replicatorsMap.size()));


        final Pair<EndpointDefinition, StatReplicator> statReplicatorPair = this.replicatorsMap.get(endpointDefinition.getId());

        if (statReplicatorPair == null) {

            logger.error(sputs("ClusteredStatReplicator::removeService() Trying to remove a service that we are not managing",
                    serviceName, "END POINT ID", endpointDefinition.getId(), " replicator count ", replicatorsMap.size()));

            return;

        }


        if (statReplicatorPair.getSecond() == null) {

            logger.error(sputs("ClusteredStatReplicator::removeService() Trying to remove a service that we are nto managing" +
                            "and the getSecond() is null",
                    serviceName, "END POINT ID", endpointDefinition.getId(), " replicator count ", replicatorsMap.size()));

            return;

        }

        try {
            statReplicatorPair.getSecond().stop();
        } catch (Exception ex) {
            logger.error("Unable to stop service endpoint that was removed " + endpointDefinition, ex);
        }
        this.replicatorsMap.remove(endpointDefinition.getId());
        this.statReplicators = new ArrayList<>(replicatorsMap.values());

        logger.info(sputs("ClusteredStatReplicator::removeService() removed",
                serviceName, endpointDefinition, " replicator count ", replicatorsMap.size()));

    }

    private void addService(final EndpointDefinition endpointDefinition) {


        if (endpointDefinition.getId().equals(localServiceId)) {
            return;
        }
        logger.info(sputs("ClusteredStatReplicator::addService()", endpointDefinition,
                " replicator count ", replicatorsMap.size()));

        final StatReplicator statReplicator = statReplicatorProvider.provide(endpointDefinition);
        this.replicatorsMap.put(endpointDefinition.getId(), Pair.pair(endpointDefinition, statReplicator));
        this.statReplicators = new ArrayList<>(replicatorsMap.values());

        logger.info(sputs("ClusteredStatReplicator::addService() added",
                serviceName, endpointDefinition, " replicator count ", replicatorsMap.size()));


    }

    @Override
    public void flush() {
        process();
    }

    final static class LocalCount {

        long count;
        String name;

    }
}

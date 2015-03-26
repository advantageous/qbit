package io.advantageous.qbit.metrics;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;

/**
 * Clustered Stat Replicator
 * Created by rhightower on 3/24/15.
 */
public class ClusteredStatReplicator implements StatReplicator, ServiceChangedEventChannel {


    private final ServiceDiscovery serviceDiscovery;
    private final StatReplicatorProvider statReplicatorProvider;
    private final ConcurrentHashMap<String, StatReplicator> replicatorsMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ClusteredStatReplicator.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean trace = logger.isTraceEnabled();
    private final String serviceName;
    private final ServicePool servicePool;
    private final String localServiceId;

    private List<StatReplicator> statReplicators = new ArrayList<>();

    public ClusteredStatReplicator(final String serviceName,
                                   final ServiceDiscovery serviceDiscovery,
                                   final StatReplicatorProvider statReplicatorProvider,
                                   final String localServiceId) {
        this.serviceDiscovery = serviceDiscovery;
        this.statReplicatorProvider = statReplicatorProvider;
        this.serviceName = serviceName;
        this.localServiceId=localServiceId;
        this.servicePool = new ServicePool(serviceName, null);

    }

    @Override
    public void recordCount(final String name, final int count, final long now) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::recordCount()",
                serviceName, name, count, now));

        if (debug) {
            if (statReplicators.size() == 0) {
                puts("WARNING.............. ######### NO REPLICATORS");
            }
        }
        statReplicators.forEach(
                statReplicator -> doRecordCount(statReplicator, name, count, now)
        );
    }

    private void doRecordCount(StatReplicator statReplicator,
                               final String name, final int count, final long now) {

        try {
            statReplicator.recordCount(name, count, now);
        } catch (Exception ex) {
            if (debug) logger.debug(sputs("Replicator failed"), ex);
            if (debug) logger.debug(sputs("Replicator failed", statReplicator ));

        }
    }


    @QueueCallback({QueueCallbackType.IDLE,
            QueueCallbackType.EMPTY,
            QueueCallbackType.LIMIT})
    void process() {

        currentTime = timer.now();
        final List<StatReplicator> badReplicators = new ArrayList<>();

        statReplicators.forEach(
                statReplicator -> flushReplicator(statReplicator, badReplicators)
        );

        badReplicators.forEach(statReplicator -> statReplicators.remove(statReplicator));
        checkForReconnect();
    }


    Timer timer = Timer.timer();
    long currentTime;
    long lastReconnectTime;
    private void checkForReconnect() {
        long duration = currentTime - lastReconnectTime;
        if (duration > 10_000) {
            doReconnect();
        }

    }

    public void doReconnect() {
        lastReconnectTime = currentTime;
        final List<ServiceDefinition> services = servicePool.services();
        if (services.size() != this.statReplicators.size()) {
            shutDownReplicators();
            services.forEach(this::addService);
        }
    }

    private void shutDownReplicators() {
        logger.debug("Shutting down replicators");
        for (StatReplicator statReplicator : statReplicators) {

            try {
                statReplicator.stop();

            } catch (Exception ex) {
                logger.debug("Shutdown replicator failed", ex);
            }

            logger.debug("Shutting down replicator");
        }

        statReplicators.clear();
        replicatorsMap.clear();
    }


    private void flushReplicator(final StatReplicator statReplicator,
                                 final List<StatReplicator> badReplicators) {


        try {
            ServiceProxyUtils.flushServiceProxy(statReplicator);
        } catch (Exception exception) {
            badReplicators.add(statReplicator);
            logger.info("Replicator failed" + statReplicator, exception);
        }
    }


    /**
     * Event handler
     *
     * @param serviceName service name
     */
    @Override
    public void servicePoolChanged(final String serviceName) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::servicePoolChanged()", serviceName));

        puts("SERVICE POOL CHANGED \n\n\n #################");

        if (this.serviceName.equals(serviceName)) {
            updateServicePool(serviceName);

        } else {
            if (debug) logger.debug(sputs("ClusteredStatReplicator::servicePoolChanged()",
                    "got event for another service", serviceName));
        }


    }

    private void updateServicePool(String serviceName) {

        final List<ServiceDefinition> nodes = serviceDiscovery.loadServices(serviceName);
        servicePool.setHealthyNodes(nodes, new ServicePoolListener() {
            @Override
            public void servicePoolChanged(String serviceName) {
            }

            @Override
            public void serviceAdded(String serviceName, ServiceDefinition serviceDefinition) {
                addService(serviceDefinition);
            }

            @Override
            public void serviceRemoved(String serviceName, ServiceDefinition serviceDefinition) {
                removeService(serviceDefinition);
            }

        });
    }

    private void removeService(final ServiceDefinition serviceDefinition) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::removeService()",
                serviceName, serviceDefinition));

        this.replicatorsMap.remove(serviceDefinition.getId());
        this.statReplicators = new ArrayList<>(replicatorsMap.values());
    }

    private void addService(final ServiceDefinition serviceDefinition) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::addService()", serviceDefinition));

        if (serviceDefinition.getId().equals(localServiceId)) {
            return;
        }

        final StatReplicator statReplicator = statReplicatorProvider.provide(serviceDefinition);
        this.replicatorsMap.put(serviceDefinition.getId(), statReplicator);
        this.statReplicators = new ArrayList<>(replicatorsMap.values());

    }

    @Override
    public void flush() {

        //if (trace) logger.trace(sputs("ClusteredStatReplicator::flush()", serviceName));
        process();
    }
}

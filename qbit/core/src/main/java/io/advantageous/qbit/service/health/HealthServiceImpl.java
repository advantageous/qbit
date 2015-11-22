package io.advantageous.qbit.service.health;


import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.service.Stoppable;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Manages health status of internal nodes/services.
 */
public class HealthServiceImpl implements HealthService, Stoppable {

    /**
     * Timer.
     */
    private final Timer timer;
    /**
     * How often should we check TTLs.
     */
    private final long recheckIntervalMS;
    /**
     * Internal map to check health.
     */
    private final Map<String, NodeHealthStat> serviceHealthStatMap
            = new ConcurrentHashMap<>();
    /**
     * Last Check in time.
     */
    private long lastCheckIn;
    /**
     * Current time.
     */
    private long now;
    /**
     * logger.
     */
    private final Logger logger = LoggerFactory.getLogger(HealthServiceImpl.class);

    private final boolean debug = logger.isDebugEnabled();

    private static int healthServiceCount;




    /**
     * Constructor.
     *
     * @param timer           timer
     * @param recheckInterval recheck interval
     * @param timeUnit        time unit for interval
     */
    public HealthServiceImpl(final Timer timer,
                             final long recheckInterval,
                             final TimeUnit timeUnit) {
        this.timer = timer;
        recheckIntervalMS = timeUnit.toMillis(recheckInterval);
        now = timer.now();
        lastCheckIn = now;
        healthServiceCount ++;

        if (logger.isDebugEnabled()) {
            Exception ex = new Exception("Health Service init called");
            ex.fillInStackTrace();
            logger.debug("Health Service CREATED", ex);
        }

        if (healthServiceCount > 1) {
            logger.info("More than ONE Health Service created {}, if that is not intended turn on debugging", healthServiceCount);
        }


        logger.info("Health Service CREATED {}", this.hashCode());
    }

    /**
     * Register method to register services / internal nodes.
     *
     * @param name     name
     * @param ttl      ttl
     * @param timeUnit timeUnit
     */
    @Override
    public void register(final String name, final long ttl, final TimeUnit timeUnit) {

        logger.info("HealthService::register() {} {} {}", name, ttl, timeUnit);
        serviceHealthStatMap.put(name, new NodeHealthStat(name, timeUnit.toMillis(ttl)));
    }

    /**
     * Check in the service.
     *
     * @param name name
     */
    @Override
    public void checkInOk(final String name) {


        if (debug) logger.debug("HealthService::checkInOk() {} ", name);
        final NodeHealthStat nodeHealthStat = getServiceHealthStat(name);


        nodeHealthStat.setLastCheckIn(now);
        nodeHealthStat.setReason(null);
        nodeHealthStat.setStatus(HealthStatus.PASS);

    }

    /**
     * Check in the service with a specific status.
     *
     * @param name   name
     * @param status status
     */
    @Override
    public void checkIn(final String name, final HealthStatus status) {

        if (status == HealthStatus.FAIL) {
            logger.error("HealthService::checkIn() {} {}", name, status);
        } else if  (status == HealthStatus.WARN) {
            logger.warn("HealthService::checkIn() {} {}", name, status);
        } else {
            if (debug) logger.debug("HealthService::checkIn() {} {}", name, status);
        }

        final NodeHealthStat nodeHealthStat = getServiceHealthStat(name);

        nodeHealthStat.setStatus(status);
        nodeHealthStat.setReason(null);
        nodeHealthStat.setLastCheckIn(now);
    }

    @Override
    public boolean ok() {
        if (debug) logger.debug("HealthService::ok()");

        boolean ok = serviceHealthStatMap.values()
                .stream()
                .allMatch(serviceHealthStat -> serviceHealthStat.getStatus() == HealthStatus.PASS);


        if (!ok) {
            logger.error("HealthService::ok() was ok? {}", ok);
        } else {
           if (debug) logger.debug("HealthService::ok() was ok? {}", ok);
        }
        return ok;
    }

    @Override
    public List<String> findHealthyNodes() {


        logger.info("HealthService::findHealthyNodes() called");
        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .filter(serviceHealthStat -> serviceHealthStat.getStatus() == HealthStatus.PASS)
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.getName()));


        logger.info("HealthService::findHealthyNodes() called returns {}", names);
        return names;
    }

    @Override
    public List<String> findAllNodes() {


        logger.info("HealthService::findAllNodes() called");
        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.getName()));


        logger.info("HealthService::findAllNodes() called returns {}", names);
        return names;
    }

    @Override
    public List<String> findAllNodesWithStatus(final HealthStatus queryStatus) {

        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .filter(serviceHealthStat -> serviceHealthStat.getStatus() == queryStatus)
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.getName()));

        return names;
    }

    @Override
    public List<String> findNotHealthyNodes() {


        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .filter(serviceHealthStat -> serviceHealthStat.getStatus() != HealthStatus.PASS)
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.getName()));

        return names;
    }

    @Override
    public List<NodeHealthStat> loadNodes() {

        logger.info("HealthService::loadNodes() called");
        return Lists.deepCopy(this.serviceHealthStatMap.values());
    }

    @Override
    public void unregister(String nodeName) {
        serviceHealthStatMap.remove(nodeName);
    }

    @QueueCallback({QueueCallbackType.IDLE, QueueCallbackType.LIMIT})
    public void process() {

        now = timer.now();

        final long duration = now - lastCheckIn;
        if (duration > recheckIntervalMS) {
            lastCheckIn = now;
            checkTTLs();
        }

    }

    private void checkTTLs() {
        final Collection<NodeHealthStat> services = serviceHealthStatMap.values();

        //noinspection Convert2MethodRef
        services.forEach(serviceHealthStat -> checkTTL(serviceHealthStat));
    }

    private void checkTTL(final NodeHealthStat nodeHealthStat) {


        if (debug) logger.debug("HealthService::checkTTL() {}", nodeHealthStat.getName());

        /* proceed to check the ttl if the status is pass. */
        boolean proceed = nodeHealthStat.getStatus() == HealthStatus.PASS;


        if (!proceed) {
            return;
        }


        final long duration = now - nodeHealthStat.getLastCheckIn();

        /* If the duration is greater than the ttl interval, then mark it as failed. */
        if (duration > nodeHealthStat.getTtlInMS()) {


            logger.error("HealthService::checkTTL() {} FAILED TTL check, duration {}",
                    nodeHealthStat.getName(), duration);

            nodeHealthStat.setReason(HealthFailReason.FAILED_TTL);
            nodeHealthStat.setStatus(HealthStatus.FAIL);

        }
    }

    private NodeHealthStat getServiceHealthStat(final String name) {
        final NodeHealthStat nodeHealthStat = serviceHealthStatMap.get(name);

        if (nodeHealthStat == null) {

            throw new IllegalStateException("Trying to manage a service that you have not registered");
        }

        return nodeHealthStat;

    }

    public enum HealthFailReason {
        FAILED_TTL,
        NONE,
        OTHER
    }


    @Override
    public void stop() {
        logger.info("Health Service stopped");
    }
}

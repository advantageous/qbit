package io.advantageous.qbit.service.health;


import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.Stoppable;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * Manages health status of internal nodes/services.
 */
public class HealthServiceImpl extends BaseService implements HealthService, Stoppable {

    private static int healthServiceCount;
    /**
     * Internal map to check health.
     */
    private final Map<String, NodeHealthStat> serviceHealthStatMap
            = new ConcurrentHashMap<>();
    /**
     * logger.
     */
    private final Logger logger = LoggerFactory.getLogger(HealthServiceImpl.class);
    private final boolean debug = logger.isDebugEnabled();
    private final Optional<Consumer<NodeHealthStat>> onFail;
    private final Optional<Consumer<NodeHealthStat>> onWarn;
    private final Optional<Consumer<NodeHealthStat>> onCheckIn;

    private final List<HealthCheckJob> healthCheckJobs;


    /**
     * Constructor.
     *
     * @param timer           timer
     * @param recheckInterval recheck interval
     * @param timeUnit        time unit for interval
     * @param onFail          onFail
     * @param onWarn          onWarn
     * @param onCheckIn       onCheckIn
     */
    public HealthServiceImpl(final String healthPrefix,
                             final Reactor reactor,
                             final Timer timer,
                             final StatsCollector statsCollector,
                             final long recheckInterval,
                             final TimeUnit timeUnit,
                             final List<HealthCheckJob> healthCheckJobs,
                             final Optional<Consumer<NodeHealthStat>> onFail,
                             final Optional<Consumer<NodeHealthStat>> onWarn,
                             final Optional<Consumer<NodeHealthStat>> onCheckIn) {

        super(healthPrefix, reactor, timer, statsCollector);
        this.onFail = onFail;
        this.onWarn = onWarn;
        this.onCheckIn = onCheckIn;


        reactor.addRepeatingTask(recheckInterval, timeUnit, () -> checkTTLs());

        this.healthCheckJobs = Collections.unmodifiableList(healthCheckJobs);

        healthCheckJobs.forEach(healthCheckJob -> reactor.addRepeatingTask(healthCheckJob.getDuration(),
                () -> runHealthCheck(healthCheckJob)));

        healthServiceCount++;


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

    private void runHealthCheck(final HealthCheckJob healthCheckJob) {
        final NodeHealthStat currentHealth = healthCheckJob.getHealthCheck().check();

        reportStatus(healthCheckJob.getName(), currentHealth);
        serviceHealthStatMap.put(healthCheckJob.getName(), currentHealth);
    }

    private void reportStatus(String name, NodeHealthStat currentHealth) {
        switch (currentHealth.getStatus()) {
            case PASS:
                if (debug) logger.debug("HEALTH PASS :: {} status check and got status {} ", name, currentHealth);
                onCheckIn.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(currentHealth)));
                super.incrementCount("pass");
                break;

            case FAIL:
                logger.error("HEALTH FAIL :: {} status check and got status {} ", name, currentHealth);
                onFail.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(currentHealth)));
                super.incrementCount("fail");
                break;

            case WARN:
                logger.warn("HEALTH WARNING :: {} status check and got status {} ", name, currentHealth);
                onWarn.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(currentHealth)));
                super.incrementCount("warn");
                break;
            default:
                onCheckIn.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(currentHealth)));
        }
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
        final NodeHealthStat nodeHealthStat = new NodeHealthStat(name, timeUnit.toMillis(ttl));
        serviceHealthStatMap.put(name, nodeHealthStat);
        super.incrementCount("nodes");
        super.recordLevel("nodes", serviceHealthStatMap.size());


    }


    /**
     * Register method to register services / internal nodes.
     *
     * @param name name
     */
    @Override
    public void registerNoTtl(final String name) {

        logger.info("HealthService::register() {} ", name);
        final NodeHealthStat nodeHealthStat = new NodeHealthStat(name);
        nodeHealthStat.setStatus(HealthStatus.PASS);
        serviceHealthStatMap.put(name, nodeHealthStat);
        super.incrementCount("nodes");
        super.recordLevel("nodes", serviceHealthStatMap.size());


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


        nodeHealthStat.setLastCheckIn(super.time);
        nodeHealthStat.setReason(null);
        nodeHealthStat.setStatus(HealthStatus.PASS);


        super.incrementCount("checkin");


        onCheckIn.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(nodeHealthStat)));

    }

    /**
     * Check in the service with a specific status.
     *
     * @param name   name
     * @param status status
     */
    @Override
    public void checkIn(final String name, final HealthStatus status) {

        super.incrementCount("checkin");


        final NodeHealthStat nodeHealthStat = getServiceHealthStat(name);

        nodeHealthStat.setStatus(status);
        nodeHealthStat.setReason(null);
        nodeHealthStat.setLastCheckIn(super.time);

        reportStatus(name, nodeHealthStat);
    }

    /**
     * Check in the service with a specific status.
     *
     * @param name   name
     * @param reason reason
     */
    @Override
    public void failWithReason(final String name, final HealthFailReason reason) {
        logger.error("HealthService::fail() {}", name);


        super.incrementCount("fail");

        final NodeHealthStat nodeHealthStat = getServiceHealthStat(name);

        nodeHealthStat.setStatus(HealthStatus.FAIL);
        nodeHealthStat.setReason(reason);
        nodeHealthStat.setLastCheckIn(super.time);

        onFail.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(nodeHealthStat)));
    }


    /**
     * Fail the node for the service with a specific status.
     *
     * @param name  name
     * @param error error
     */
    @Override
    public void failWithError(final String name, final Throwable error) {
        logger.error("HealthService::fail() {}", name);


        super.incrementCount("fail");

        final NodeHealthStat nodeHealthStat = getServiceHealthStat(name);

        nodeHealthStat.setStatus(HealthStatus.FAIL);
        nodeHealthStat.setReason(HealthFailReason.ERROR);
        nodeHealthStat.setLastCheckIn(super.time);
        nodeHealthStat.setError(error);

        onFail.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(nodeHealthStat)));

    }

    @Override
    public void warnWithReason(final String name, final HealthFailReason reason) {
        logger.warn("HealthService::warn() {}", name);

        super.incrementCount("warn");

        final NodeHealthStat nodeHealthStat = getServiceHealthStat(name);

        nodeHealthStat.setStatus(HealthStatus.WARN);
        nodeHealthStat.setReason(reason);
        nodeHealthStat.setLastCheckIn(super.time);

        onWarn.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(nodeHealthStat)));

    }

    @Override
    public void warnWithError(final String name, final Throwable error) {
        logger.warn("HealthService::warn() {}", name);


        super.incrementCount("warn");

        final NodeHealthStat nodeHealthStat = getServiceHealthStat(name);

        nodeHealthStat.setStatus(HealthStatus.WARN);
        nodeHealthStat.setReason(HealthFailReason.ERROR);
        nodeHealthStat.setLastCheckIn(super.time);
        nodeHealthStat.setError(error);

        onWarn.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(nodeHealthStat)));


    }

    @Override
    public boolean ok() {
        if (debug) logger.debug("HealthService::ok()");

        boolean ok = serviceHealthStatMap.values()
                .stream()
                .allMatch(serviceHealthStat -> serviceHealthStat.getStatus() == HealthStatus.PASS);


        logger.error("HealthService::ok() was ok? {}", ok);
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

        super.incrementCount("unregister");
        serviceHealthStatMap.remove(nodeName);
    }

    private void checkTTLs() {
        super.incrementCount("ttlcheck");

        final Collection<NodeHealthStat> services = serviceHealthStatMap.values();

        //noinspection Convert2MethodRef
        services.forEach(serviceHealthStat -> checkTTL(serviceHealthStat));
    }

    private void checkTTL(final NodeHealthStat nodeHealthStat) {

        if (debug) logger.debug("HealthService::checkTTL() {}", nodeHealthStat.getName());
        if (!nodeHealthStat.isOk()) {
            return;
        }

        if (nodeHealthStat.isForever()) {
            return;
        }

        final long duration = super.time - nodeHealthStat.getLastCheckIn();

        /* If the duration is greater than the ttl interval, then mark it as failed. */
        if (duration > nodeHealthStat.getTtlInMS()) {


            logger.error("HealthService::checkTTL() {} FAILED TTL check, duration {}",
                    nodeHealthStat.getName(), duration);

            nodeHealthStat.setReason(HealthFailReason.FAILED_TTL);
            nodeHealthStat.setStatus(HealthStatus.FAIL);

            onFail.ifPresent(checkIn -> checkIn.accept(BeanUtils.copy(nodeHealthStat)));

        }
    }

    private NodeHealthStat getServiceHealthStat(final String name) {
        NodeHealthStat nodeHealthStat = serviceHealthStatMap.get(name);

        if (nodeHealthStat == null) {

            logger.warn("Node {} not registered but you are checking in, we will register with NO TTL");
            this.registerNoTtl(name);
            nodeHealthStat = serviceHealthStatMap.get(name);
        }

        return nodeHealthStat;

    }

    void callProcess() {
        super.doProcess();
    }

    @Override
    public void stop() {
        logger.info("Health Service stopped");
    }
}

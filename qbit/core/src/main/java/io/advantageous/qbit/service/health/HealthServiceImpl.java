package io.advantageous.qbit.service.health;


import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
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
public class HealthServiceImpl implements HealthService {

    /**
     * Timer.
     */
    private final Timer timer;
    /**
     * How often should we check TTLs.
     */
    private final long recheckIntervalMS;
    /**
     * Last Check in time.
     */
    private  long lastCheckIn;
    /**
     * Current time.
     */
    private long now;

    /**
     * Internal map to check health.
     */
    private final Map<String, ServiceHealthStat> serviceHealthStatMap
            = new ConcurrentHashMap<>();

    /**
     * logger.
     */
    private Logger logger = LoggerFactory.getLogger(HealthServiceImpl.class);


    /**
     * Constructor.
     * @param timer timer
     * @param recheckInterval recheck interval
     * @param timeUnit time unit for interval
     */
    public HealthServiceImpl(final Timer timer,
                             final long recheckInterval,
                             final TimeUnit timeUnit) {
        this.timer = timer;
        recheckIntervalMS = timeUnit.toMillis(recheckInterval);
        now = timer.now();
        lastCheckIn = now;
    }

    public enum  HealthFailReason {
        FAILED_TTL,
        OTHER
    }


    /** Internal class to hold health status. */
    private static class ServiceHealthStat {
        private final String name;
        private long lastCheckIn;
        private final long ttlInMS;
        private HealthFailReason reason;
        private HealthStatus status = HealthStatus.UNKNOWN;

        public ServiceHealthStat(final String name, final long ttlInMS) {
            this.name = name;
            this.ttlInMS = ttlInMS;
        }
    }


    /**
     * Register method to register services / internal nodes.
     * @param name name
     * @param ttl ttl
     * @param timeUnit timeUnit
     */
    @Override
    public void register(final String name, final long ttl, final TimeUnit timeUnit) {

        logger.info("HealthService::register() {} {} {}", name, ttl, timeUnit);
        serviceHealthStatMap.put(name, new ServiceHealthStat(name,timeUnit.toMillis(ttl)));
    }

    /**
     * Check in the service.
     * @param name name
     */
    @Override
    public void checkInOk(final String name) {


        logger.info("HealthService::checkInOk() {} ", name);
        final ServiceHealthStat serviceHealthStat = getServiceHealthStat(name);


        serviceHealthStat.lastCheckIn = now;
        serviceHealthStat.status = HealthStatus.PASS;

    }


    /**
     * Check in the service with a specific status.
     * @param name name
     * @param status status
     */
    @Override
    public void checkIn(final String name, final HealthStatus status) {

        logger.info("HealthService::checkIn() {} {}", name, status);

        final ServiceHealthStat serviceHealthStat = getServiceHealthStat(name);

        serviceHealthStat.status = status;
        serviceHealthStat.lastCheckIn = now;
    }

    @Override
    public boolean ok() {
        logger.info("HealthService::ok()");

        boolean ok = serviceHealthStatMap.values()
                        .stream()
                        .allMatch(serviceHealthStat -> serviceHealthStat.status == HealthStatus.PASS);


        logger.info("HealthService::ok() was ok? {}", ok);
        return ok;
    }




    @Override
    public List<String> findHealthyNodes() {

        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .filter(serviceHealthStat -> serviceHealthStat.status == HealthStatus.PASS)
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.name));

        return names;
    }

    @Override
    public List<String> findAllNodes() {

        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.name));

        return names;
    }


    @Override
    public List<String> findAllNodesWithStatus(final HealthStatus queryStatus) {

        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .filter(serviceHealthStat -> serviceHealthStat.status == queryStatus)
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.name));

        return names;
    }

    @Override
    public List<String> findNotHealthyNodes() {


        final List<String> names = new ArrayList<>();

        serviceHealthStatMap.values()
                .stream()
                .filter(serviceHealthStat -> serviceHealthStat.status != HealthStatus.PASS)
                .forEach(serviceHealthStat -> names.add(serviceHealthStat.name));

        return names;
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
        Collection<ServiceHealthStat> services = serviceHealthStatMap.values();

        services.forEach(serviceHealthStat -> checkTTL(serviceHealthStat));
    }

    private void checkTTL(final ServiceHealthStat serviceHealthStat) {


        logger.info("HealthService::checkTTL() {}", serviceHealthStat.name);

        /* proceed to check the ttl if the status is pass. */
        boolean proceed = serviceHealthStat.status == HealthStatus.PASS;


        if (!proceed) {
            return;
        }


        final long duration = now - serviceHealthStat.lastCheckIn;

        /* If the duration is greater than the ttl interval, then mark it as failed. */
        if (duration > serviceHealthStat.ttlInMS) {


            logger.info("HealthService::checkTTL() {} FAILED TTL check, duration {}",
                    serviceHealthStat.name, duration);

            serviceHealthStat.reason = HealthFailReason.FAILED_TTL;
            serviceHealthStat.status = HealthStatus.FAIL;

        }
    }



    private ServiceHealthStat getServiceHealthStat(final String name) {
        final ServiceHealthStat serviceHealthStat = serviceHealthStatMap.get(name);

        if (serviceHealthStat == null) {

            throw new IllegalStateException("Trying to manage a service that you have not registered");
        }

        return serviceHealthStat;

    }
}

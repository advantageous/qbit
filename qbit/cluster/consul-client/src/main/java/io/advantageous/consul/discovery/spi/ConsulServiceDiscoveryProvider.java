package io.advantageous.consul.discovery.spi;

import io.advantageous.consul.Consul;
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.NotRegisteredException;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.consul.domain.Status;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.consul.domain.option.RequestOptionsBuilder;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.client.HttpClientClosedConnectionException;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.impl.ServiceHealthCheckIn;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.util.ConcurrentHashSet;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static io.advantageous.boon.core.Str.sputs;


/**
 * Consul Service Discovery Provider
 * created by rhightower on 3/24/15.
 */
public class ConsulServiceDiscoveryProvider implements ServiceDiscoveryProvider {

    private final String consulHost;
    private final int consulPort;
    private final String datacenter;
    private final String[] tags;
    private final int longPollTimeSeconds;
    private final Logger logger = LoggerFactory.getLogger(ConsulServiceDiscoveryProvider.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean trace = logger.isTraceEnabled();
    private final AtomicInteger lastIndex = new AtomicInteger();
    /* Used to manage consul retry logic. */
    private final AtomicInteger consulRetryCount = new AtomicInteger();
    private final AtomicLong lastResetTimestamp = new AtomicLong(Timer.clockTime());

    private final Map<String,EndpointDefinition> registrations = new ConcurrentHashMap<>();



    public ConsulServiceDiscoveryProvider(final String consulHost,
                                          final int consulPort,
                                          final String datacenter,
                                          final String tag,
                                          final int longPollTimeSeconds) {
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.datacenter = datacenter;

        if (tag == null || "".equals(tag)) {
            this.tags = new String[]{};
        } else {
            this.tags = new String[]{tag};
        }
        this.longPollTimeSeconds = longPollTimeSeconds;

        if (trace) {
            logger.trace(sputs(
                    "ConsulServiceDiscoveryProvider",
                    consulHost, consulPort, datacenter, tag, longPollTimeSeconds
            ));
        }
    }


    @Override
    public void unregisterServices(final ConcurrentHashSet<EndpointDefinition> endpointDefinitions) {


        for (EndpointDefinition definition : endpointDefinitions) {
            Consul consul = consul();

            try {

                registrations.remove(definition.getId());
                consul.agent().deregister(definition.getId());
            } catch (Exception ex) {
                handleConsulRecovery(consul, ex);
            } finally {
                shutDownConsul(consul);
            }
        }


    }

    @Override
    public void registerServices(final Queue<EndpointDefinition> registerQueue) {


        if (trace) {
            logger.trace(sputs(
                    "ConsulServiceDiscoveryProvider::registerServices",
                    registerQueue
            ));
        }

        EndpointDefinition endpointDefinition = registerQueue.poll();
        if (endpointDefinition != null) {
            final Consul consul = consul();
            try {
                while (endpointDefinition != null) {
                    registrations.put(endpointDefinition.getId(), endpointDefinition);
                    try {
                        consul.agent().registerService(endpointDefinition.getPort(),
                                endpointDefinition.getTimeToLive(),
                                endpointDefinition.getName(), endpointDefinition.getId(), tags);
                    } catch (Exception ex) {
                        handleConsulRecovery(consul, ex);
                    }
                    endpointDefinition = registerQueue.poll();
                }
            } finally {
                shutDownConsul(consul);
            }
        }
    }

    private void handleConsulRecovery(final Consul consul, final Exception ex) {
        logger.warn("Unable to use consul", ex);
        if (consulRetryCount.incrementAndGet() > 10) {

            logger.info("Exceeded retry count with consul");
            final long now = Timer.clockTime();
            final long duration = now - lastResetTimestamp.get();

            if (duration > 180_000) {

                logger.info("Resetting retry count");
                lastResetTimestamp.set(now);
                consulRetryCount.set(0);
            }

        } else {

            logger.debug("problem running consul register service", ex);
            shutDownConsul(consul);
        }

        if (ex instanceof RuntimeException) {
            throw ((RuntimeException) ex);
        }
    }

    @Override
    public void checkIn(final Queue<ServiceHealthCheckIn> checkInsQueue) {

        if (trace) {
            logger.trace(sputs(
                    "ConsulServiceDiscoveryProvider::checkIn",
                    checkInsQueue
            ));
        }

        /* This was added to remove duplicate check-ins. */
        final Set<ServiceHealthCheckIn> uniqueCheckIns = createUniqueSetOfCheckins(checkInsQueue);

        if (uniqueCheckIns.size() > 0) {
            Consul consul = consul();

            try {
                for (ServiceHealthCheckIn checkIn : uniqueCheckIns) {
                    checkInWithConsul(consul, checkIn);
                }
            } catch (Exception ex) {
                handleConsulRecovery(consul, ex);
            } finally {
                shutDownConsul(consul);
            }

        }
    }

    private void checkInWithConsul(final Consul consul, final ServiceHealthCheckIn checkIn) {

        final Status status = convertStatus(checkIn.getHealthStatus());

        try {
            consul.agent().checkTtl(checkIn.getServiceId(), status, "" + checkIn.getHealthStatus());
        } catch (NotRegisteredException notRegisteredException) {
            final EndpointDefinition endpointDefinition = registrations.get(checkIn.getServiceId());
            if (endpointDefinition!=null) {
                consul.agent().registerService(endpointDefinition.getPort(),
                        endpointDefinition.getTimeToLive(),
                        endpointDefinition.getName(), endpointDefinition.getId(), tags);
            }
        }

    }

    private Set<ServiceHealthCheckIn> createUniqueSetOfCheckins(final Queue<ServiceHealthCheckIn> checkInsQueue) {
        LinkedHashSet<ServiceHealthCheckIn> set = new LinkedHashSet<>(checkInsQueue.size());

        checkInsQueue.forEach(serviceHealthCheckIn -> set.add(serviceHealthCheckIn));
        return set;
    }

    private void shutDownConsul(Consul consul) {
        try {
            if (consul!=null) {
                consul.stop();
            }
        } catch (Exception ex) {
            logger.warn("Shutting down consul", ex);
        }
    }

    @Override
    public List<EndpointDefinition> loadServices(final String serviceName) {

        if (trace) {
            logger.trace(sputs(
                    "ConsulServiceDiscoveryProvider::loadServices",
                    serviceName
            ));
        }


        if (debug) logger.debug(sputs("Fetching healthy nodes for", serviceName));

        final List<ServiceHealth> healthyServices = getHealthyServices(serviceName);


        if (debug) logger.debug(sputs("Fetched healthy nodes for", serviceName,
                "node count fetched", healthyServices.size()));


        return convertToServiceDefinitions(healthyServices);

    }


    private List<EndpointDefinition> convertToServiceDefinitions(
            final List<ServiceHealth> healthyServices) {

        final List<EndpointDefinition> endpointDefinitions = new ArrayList<>(healthyServices.size());

        healthyServices.forEach(serviceHealth -> {
            EndpointDefinition endpointDefinition =
                    convertToServiceDefinition(serviceHealth);
            endpointDefinitions.add(endpointDefinition);
        });

        return endpointDefinitions;
    }

    private EndpointDefinition convertToServiceDefinition(final ServiceHealth serviceHealth) {

        final String host = serviceHealth.getNode().getAddress();
        final int port = serviceHealth.getService().getPort();
        final String id = serviceHealth.getService().getId();
        final String name = serviceHealth.getService().getService();
        final EndpointDefinition endpointDefinition =
                new EndpointDefinition(HealthStatus.PASS, id, name, host, port);

        if (debug) logger.debug(sputs("convertToServiceDefinition \nserviceHealth",
                serviceHealth, "\nserviceDefinition", endpointDefinition));

        return endpointDefinition;
    }


    private RequestOptions buildRequestOptions() {
        return new RequestOptionsBuilder()
                .consistency(Consistency.CONSISTENT)
                .blockSeconds(longPollTimeSeconds, lastIndex.get()).build();
    }


    private List<ServiceHealth> getHealthyServices(final String serviceName) {
        Consul consul = consul();

        try {

            String tag = tags.length > 1 ? tags[0] : null;
            final ConsulResponse<List<ServiceHealth>> consulResponse = consul.health()
                    .getHealthyServices(serviceName, datacenter, tag, buildRequestOptions());


            this.lastIndex.set(consulResponse.getIndex());

            //noinspection UnnecessaryLocalVariable
            final List<ServiceHealth> healthyServices = consulResponse.getResponse();

            return healthyServices;

        } catch (HttpClientClosedConnectionException ex) {

            handleConsulRecovery(consul, ex);
            return Collections.emptyList();
        } finally {
            shutDownConsul(consul);
        }
    }


    private Status convertStatus(final HealthStatus healthStatus) {
        switch (healthStatus) {
            case PASS:
                return Status.PASS;
            case FAIL:
                return Status.FAIL;
            case WARN:
                return Status.UNKNOWN;
            case UNKNOWN:
                return Status.UNKNOWN;
            default:
                return Status.UNKNOWN;
        }

    }


    private Consul consul()  {
        final Consul consul =  Consul.consul(consulHost, consulPort);
        consul.start();
        return consul;
    }

}

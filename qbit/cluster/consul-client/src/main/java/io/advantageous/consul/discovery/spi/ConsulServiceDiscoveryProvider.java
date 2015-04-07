package io.advantageous.consul.discovery.spi;

import io.advantageous.consul.Consul;
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.consul.domain.Status;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.consul.domain.option.RequestOptionsBuilder;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.service.discovery.HealthStatus;
import io.advantageous.qbit.service.discovery.ServiceDefinition;
import io.advantageous.qbit.service.discovery.impl.ServiceHealthCheckIn;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static io.advantageous.boon.core.Str.sputs;


/**
 * Consul Service Discovery Provider
 * Created by rhightower on 3/24/15.
 */
public class ConsulServiceDiscoveryProvider implements ServiceDiscoveryProvider {

    private final String consulHost;
    private final int consulPort;
    private final String datacenter;
    private final String tag;
    private final int longPollTimeSeconds;
    private AtomicInteger lastIndex = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(ConsulServiceDiscoveryProvider.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean trace = logger.isTraceEnabled();


    private final ThreadLocal<Consul> consulThreadLocal = new ThreadLocal<Consul>(){
        @Override
        protected Consul initialValue() {
            final Consul consul = Consul.consul(consulHost, consulPort);
            consul.start();
            return consul;
        }
    };




    public ConsulServiceDiscoveryProvider(final String consulHost,
                                          final int consulPort,
                                          final String datacenter,
                                          final String tag,
                                          final int longPollTimeSeconds) {
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.datacenter = datacenter;
        this.tag = tag;
        this.longPollTimeSeconds = longPollTimeSeconds;

        if (trace) {
            logger.trace(sputs(
                    "ConsulServiceDiscoveryProvider",
                    consulHost, consulPort, datacenter, tag, longPollTimeSeconds
                    ));
        }
    }

    @Override
    public void registerServices(final Queue<ServiceDefinition> registerQueue) {


        if (trace) {
            logger.trace(sputs(
                    "ConsulServiceDiscoveryProvider::registerServices",
                    registerQueue
            ));
        }

        ServiceDefinition serviceDefinition = registerQueue.poll();
        if (serviceDefinition!=null) {
            Consul consul = consulThreadLocal.get();

            while(serviceDefinition!=null) {
                    try {
                        consul.agent().registerService(serviceDefinition.getPort(),
                                serviceDefinition.getTimeToLive(),
                                serviceDefinition.getName(), serviceDefinition.getId(), tag);
                    } catch (Exception ex) {

                        logger.debug("problem running consul register service", ex);
                        shutDownConsul(consul);
                        Consul consulNew = Consul.consul(consulHost, consulPort);
                        consulNew.start();
                        consulThreadLocal.set(consulNew);
                    }
                    serviceDefinition = registerQueue.poll();
            }
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

        ServiceHealthCheckIn checkIn = checkInsQueue.poll();

        if (checkIn!=null) {
            Consul consul = consulThreadLocal.get();

            while (checkIn != null) {
                    Status status = convertStatus(checkIn.getHealthStatus());

                    try {
                        consul.agent().checkTtl(checkIn.getServiceId(), status, "" + checkIn.getHealthStatus());
                    }catch (Exception ex) {
                        logger.debug("problem running consul agent checkTtl", ex);
                        shutDownConsul(consul);
                        Consul consulNew = Consul.consul(consulHost, consulPort);
                        consulNew.start();
                        consulThreadLocal.set(consulNew);
                    }
                    checkIn = checkInsQueue.poll();
            }

        }
    }

    private void shutDownConsul(Consul consul) {
        try {
            consul.stop();
        } catch (Exception ex) {

        }
    }

    @Override
    public List<ServiceDefinition> loadServices(final String serviceName) {

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


        final List<ServiceDefinition> serviceDefinitions =
                convertToServiceDefinitions(healthyServices);

        return serviceDefinitions;

    }



    private List<ServiceDefinition> convertToServiceDefinitions(
            final List<ServiceHealth> healthyServices) {

        final List<ServiceDefinition> serviceDefinitions = new ArrayList<>(healthyServices.size());

        healthyServices.forEach(serviceHealth -> {
            ServiceDefinition serviceDefinition =
                    convertToServiceDefinition(serviceHealth);
            serviceDefinitions.add(serviceDefinition);
        });

        return serviceDefinitions;
    }

    private ServiceDefinition convertToServiceDefinition(final ServiceHealth serviceHealth) {

        final String host = serviceHealth.getNode().getAddress();
        final int port = serviceHealth.getService().getPort();
        final String id = serviceHealth.getService().getId();
        final String name = serviceHealth.getService().getService();
        final ServiceDefinition serviceDefinition =
                new ServiceDefinition(HealthStatus.PASS, id, name, host, port);

        if (debug) logger.debug(sputs("convertToServiceDefinition \nserviceHealth",
                serviceHealth, "\nserviceDefinition" , serviceDefinition));

        return serviceDefinition;
    }



    private RequestOptions buildRequestOptions() {
        return  new RequestOptionsBuilder()
                .consistency(Consistency.CONSISTENT)
                .blockSeconds(longPollTimeSeconds, lastIndex.get()).build();
    }



    private List<ServiceHealth> getHealthyServices(final String serviceName
                                                   ) {
        Consul consul = consulThreadLocal.get();

        try {
            final ConsulResponse<List<ServiceHealth>> consulResponse = consul.health()
                    .getHealthyServices(serviceName, datacenter, tag, buildRequestOptions());


            this.lastIndex.set(consulResponse.getIndex());

            final List<ServiceHealth> healthyServices = consulResponse.getResponse();

            return healthyServices;

        } catch (Exception ex) {

            shutDownConsul(consul);
            Consul consulNew = Consul.consul(consulHost, consulPort);
            consulNew.start();
            consulThreadLocal.set(consulNew);
            return Collections.emptyList();
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




}

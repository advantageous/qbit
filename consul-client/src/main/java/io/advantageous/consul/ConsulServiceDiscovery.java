package io.advantageous.consul;

import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.Registration;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.consul.domain.Status;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.consul.domain.option.RequestOptionsBuilder;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.advantageous.boon.Boon.puts;

/**
 * Service Discovery using consul
 * Created by rhightower on 3/23/15.
 */
public class ConsulServiceDiscovery implements ServiceDiscovery{

    private final String consulHost;
    private final int consulPort;
    private final String datacenter;
    private final String tag;
    private final int longPollTimeSeconds;
    private final PeriodicScheduler periodicScheduler;
    private final BlockingQueue<String> doneQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<CheckIn> checkInsQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<ServiceDefinition> registerQueue = new LinkedTransferQueue<>();

    private final ServiceChangedEventChannel serviceChangedEventChannel;
    private AtomicInteger lastIndex = new AtomicInteger();
    private AtomicBoolean stop = new AtomicBoolean();
    private Set<String> serviceNames = new TreeSet<>();
    private final ExecutorService executorService;

    private final ConcurrentHashMap <String, ServicePool> servicePoolMap = new ConcurrentHashMap<>();



    public ConsulServiceDiscovery(
            final String consulHost,
            final int consulPort,
            final String datacenter,
            final String tag,
            final int longPollTimeSeconds,
            final PeriodicScheduler periodicScheduler,
            final ServiceChangedEventChannel serviceChangedEventChannel) {

        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.datacenter = datacenter;
        this.tag = tag;
        this.longPollTimeSeconds = longPollTimeSeconds;

        this.periodicScheduler =
                periodicScheduler==null ? QBit.factory().periodicScheduler() : periodicScheduler;

        this.serviceChangedEventChannel = serviceChangedEventChannel == null ?
                 serviceName -> {

                } : serviceChangedEventChannel;


        executorService = Executors.newFixedThreadPool(100);//Mostly sleeping threads doing long polls



    }


    @Override
    public ServiceDefinition registerService(String serviceName, int port) {
        watchService(serviceName);


        ServiceDefinition serviceDefinition =  new ServiceDefinition(HealthStatus.PASS,
                serviceName + "-" + UUID.randomUUID().toString(),
                serviceName, null, port);

        registerQueue.offer(serviceDefinition);

        return serviceDefinition;

    }

    @Override
    public void watchService(String serviceName) {
        if (!serviceNames.contains(serviceName)) {
            serviceNames.add(serviceName);
            doneQueue.offer(serviceName);
        }

    }

    static class CheckIn {
        final String serviceId;
        final HealthStatus healthStatus;

        CheckIn(String serviceId, HealthStatus healthStatus) {
            this.serviceId = serviceId;
            this.healthStatus = healthStatus;
        }
    }

    @Override
    public void checkIn(String serviceId, HealthStatus healthStatus) {

        checkInsQueue.offer(new CheckIn(serviceId, healthStatus));


    }

    public ServicePool servicePool(final String serviceName)  {
        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool==null) {
            servicePool = new ServicePool(serviceName, null);
            servicePoolMap.put(serviceName, servicePool);
        }
        return servicePool;
    }

    @Override
    public List<ServiceDefinition> loadServices(final String serviceName) {
        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool==null) {
            servicePool = new ServicePool(serviceName, null);
            servicePoolMap.put(serviceName, servicePool);
            watchService(serviceName);
            return Collections.emptyList();
        }
        return servicePool.services();
    }


    private RequestOptions buildRequestOptions() {
        return  new RequestOptionsBuilder()
                .consistency(Consistency.CONSISTENT)
                .blockSeconds(longPollTimeSeconds, lastIndex.get()).build();
    }

    @Override
    public void start() {
        this.periodicScheduler.repeat(() -> {
            try {
                monitor();
            } catch (InterruptedException e) {
                e.printStackTrace();//add logging
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    private List<ServiceHealth> getHealthyServices(final String serviceName) {
        Consul consul = Consul.consul(consulHost, consulPort);


        try {
            consul.start();
            final ConsulResponse<List<ServiceHealth>> consulResponse = consul.health()
                    .getHealthyServices(serviceName, datacenter, tag, buildRequestOptions());


            this.lastIndex.set(consulResponse.getIndex());

            final List<ServiceHealth> healthyServices = consulResponse.getResponse();

            return healthyServices;
        } finally {
            doneQueue.offer(serviceName);
            consul.stop();
        }
    }



    public void monitor() throws InterruptedException {

        while (!stop.get()) {

            String serviceName = doneQueue.poll(50, TimeUnit.MILLISECONDS);

            while (serviceName!=null) {

                final String serviceNameToFetch = serviceName;
                executorService.submit(() -> {
                    try {
                        puts("Fetching healthy nodes for", serviceNameToFetch);
                        final List<ServiceHealth> healthyServices = getHealthyServices(serviceNameToFetch);
                        puts("Fetching healthy nodes for", serviceNameToFetch, healthyServices.size());

                        populateServiceMap(serviceNameToFetch, healthyServices);
                    }catch (Exception ex) {
                        ex.printStackTrace(); //TODO log
                    }
                });
                serviceName = doneQueue.poll();
            }


            CheckIn checkIn = checkInsQueue.poll();
            ServiceDefinition serviceDefinition = registerQueue.poll();

            if (checkIn!=null || serviceDefinition!=null) {
                Consul consul = Consul.consul(consulHost, consulPort);
                try {
                    consul.start();


                    while(serviceDefinition!=null) {


                        //puts("REGISTER ", serviceDefinition);
                        consul.agent().registerService(serviceDefinition.getPort(),
                                serviceDefinition.getTimeToLive(),
                                serviceDefinition.getName(), serviceDefinition.getId(), tag);
                        serviceDefinition = registerQueue.poll();
                    }

                    while (checkIn != null) {
                        Status status = convertStatus(checkIn.healthStatus);

                        //puts("Checking in ", checkIn.healthStatus, checkIn.serviceId);
                        consul.agent().checkTtl(checkIn.serviceId, status, "" + checkIn.healthStatus);
                        checkIn = checkInsQueue.poll();
                    }


                } finally {
                    consul.stop();
                }
            }


        }
    }

    private Status convertStatus(HealthStatus healthStatus) {
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

    private void populateServiceMap(final String serviceName, final List<ServiceHealth> healthyServices) {
        final List<ServiceDefinition> serviceDefinitions = convertToServiceDefinitions(healthyServices);
        final ServicePool servicePool = servicePool(serviceName);
        if (servicePool.setHealthyNodes(serviceDefinitions)) {
            serviceChangedEventChannel.servicePoolChanged(serviceName);
        }
    }

    private List<ServiceDefinition> convertToServiceDefinitions(
            final List<ServiceHealth> healthyServices) {

        final List<ServiceDefinition> serviceDefinitions = new ArrayList<>(healthyServices.size());

        healthyServices.forEach(serviceHealth -> {
            ServiceDefinition serviceDefinition = convertToServiceDefinition(serviceHealth);
            serviceDefinitions.add(serviceDefinition);
        });

        return serviceDefinitions;
    }

    private ServiceDefinition convertToServiceDefinition(final ServiceHealth serviceHealth) {

        final String host = serviceHealth.getNode().getAddress();
        final int port = serviceHealth.getService().getPort();
        final String id = serviceHealth.getService().getId();
        final String name = serviceHealth.getService().getService();

        return new ServiceDefinition(HealthStatus.PASS, id, name, host, port);
    }


    @Override
    public void stop() {

        this.periodicScheduler.stop();
        this.stop.set(true);

    }
}

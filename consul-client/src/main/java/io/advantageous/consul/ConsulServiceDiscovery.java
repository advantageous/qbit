package io.advantageous.consul;

import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.consul.domain.option.RequestOptionsBuilder;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.HealthStatus;
import io.advantageous.qbit.service.discovery.ServiceDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private AtomicInteger lastIndex = new AtomicInteger();
    private AtomicBoolean stop = new AtomicBoolean();
    private Set<String> serviceNames = new TreeSet<>();
    private final ExecutorService executorService;

    //TODO create a set of services that takes a service name and a map


    public ConsulServiceDiscovery(
            final String consulHost,
            final int consulPort,
            final String datacenter,
            final String tag,
            final int longPollTimeSeconds,
            final PeriodicScheduler periodicScheduler
            ) {

        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.datacenter = datacenter;
        this.tag = tag;
        this.longPollTimeSeconds = longPollTimeSeconds;

        this.periodicScheduler =
                periodicScheduler==null ? QBit.factory().periodicScheduler() : periodicScheduler;


        executorService = Executors.newFixedThreadPool(100);//Mostly sleeping threads doing long polls



    }

    @Override
    public ServiceDefinition registerService(String serviceName, int port) {
        watchService(serviceName);
        return new ServiceDefinition(HealthStatus.PASS,
                serviceName + "." + UUID.randomUUID().toString(),
                serviceName, null, port);
    }

    @Override
    public void watchService(String serviceName) {
        if (!serviceNames.contains(serviceName)) {
            serviceNames.add(serviceName);
            doneQueue.offer(serviceName);
        }

    }

    @Override
    public void checkIn(String serviceId, HealthStatus healthStatus) {

    }

    @Override
    public List<ServiceDefinition> loadServices(final String serviceName) {
        watchService(serviceName);

        //TODO create a set of services that takes a service name and a map
        return null;
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
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        final List<ServiceHealth> healthyServices = getHealthyServices(serviceNameToFetch);
                        populateServiceMap(healthyServices);
                    }
                });
                serviceName = doneQueue.poll();
            }
        }
    }

    private void populateServiceMap(final List<ServiceHealth> healthyServices) {
        List<ServiceDefinition> serviceDefinitions = convertToServiceDefinitions(healthyServices);

        //TODO create a set of services that takes a service name and a map

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

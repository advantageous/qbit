package io.advantageous.qbit.service.discovery.impl;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service Discovery using consul
 * Created by rhightower on 3/23/15.
 */
public class ServiceDiscoveryImpl implements ServiceDiscovery{

    private final PeriodicScheduler periodicScheduler;
    private final BlockingQueue<String> doneQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<ServiceHealthCheckIn> checkInsQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<ServiceDefinition> registerQueue = new LinkedTransferQueue<>();

    private final ServiceChangedEventChannel serviceChangedEventChannel;
    private final ServicePoolListener servicePoolListener;
    private AtomicBoolean stop = new AtomicBoolean();
    private Set<String> serviceNames = new TreeSet<>();
    private final ExecutorService executorService;

    private final ConcurrentHashMap <String, ServicePool> servicePoolMap = new ConcurrentHashMap<>();

    private final ServiceDiscoveryProvider provider;






    public ServiceDiscoveryImpl(
            final PeriodicScheduler periodicScheduler,
            final ServiceChangedEventChannel serviceChangedEventChannel,
            final ServiceDiscoveryProvider provider,
            final  ServicePoolListener servicePoolListener,
            final ExecutorService executorService) {


        this.provider = provider;

        this.periodicScheduler =
                periodicScheduler==null ? QBit.factory().periodicScheduler() : periodicScheduler;

        this.serviceChangedEventChannel = serviceChangedEventChannel == null ?
                 serviceName -> {

                } : serviceChangedEventChannel;
        this.servicePoolListener = servicePoolListener == null ? serviceName -> {} : servicePoolListener;

        this.executorService = executorService==null ?
                Executors.newFixedThreadPool(100) :
                executorService;//Mostly sleeping threads doing long polls



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

    @Override
    public void checkIn(String serviceId, HealthStatus healthStatus) {

        checkInsQueue.offer(new ServiceHealthCheckIn(serviceId, healthStatus));


    }

    public ServicePool servicePool(final String serviceName)  {
        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool==null) {
            servicePool = new ServicePool(serviceName, this.servicePoolListener);
            servicePoolMap.put(serviceName, servicePool);
        }
        return servicePool;
    }

    @Override
    public List<ServiceDefinition> loadServices(final String serviceName) {
        ServicePool servicePool = servicePoolMap.get(serviceName);
        if (servicePool==null) {
            servicePool = new ServicePool(serviceName, this.servicePoolListener);
            servicePoolMap.put(serviceName, servicePool);
            watchService(serviceName);
            return Collections.emptyList();
        }
        return servicePool.services();
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




    public void monitor() throws InterruptedException {

        while (!stop.get()) {
            loadHealthyServices();
            provider.registerServices(registerQueue);
            provider.checkIn(checkInsQueue);
        }
    }

    private void loadHealthyServices() throws InterruptedException {
        String serviceName = doneQueue.poll(50, TimeUnit.MILLISECONDS);

        while (serviceName!=null) {

            final String serviceNameToFetch = serviceName;
            executorService.submit(() -> {
                try {

                    try {
                        final List<ServiceDefinition> healthyServices = provider.loadServices(serviceNameToFetch);
                        populateServiceMap(serviceNameToFetch, healthyServices);
                    } finally {
                        doneQueue.offer(serviceNameToFetch);
                    }
                }catch (Exception ex) {
                    ex.printStackTrace(); //TODO log
                }
            });
            serviceName = doneQueue.poll();
        }
    }


    private void populateServiceMap(final String serviceName, final List<ServiceDefinition> healthyServices) {
        final ServicePool servicePool = servicePool(serviceName);
        if (servicePool.setHealthyNodes(healthyServices)) {
            serviceChangedEventChannel.servicePoolChanged(serviceName);
        }
    }


    @Override
    public void stop() {

        this.periodicScheduler.stop();
        this.stop.set(true);

    }
}

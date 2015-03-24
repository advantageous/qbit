package io.advantageous.qbit.service.discovery.impl;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.util.ConcurrentHashSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.qbit.service.discovery.ServiceDefinition.serviceDefinition;
import static io.advantageous.qbit.service.discovery.ServiceDefinition.serviceDefinitions;
import static org.junit.Assert.*;

public class ServiceDiscoveryImplTest{


    ServiceDiscoveryImpl serviceDiscovery;

    AtomicInteger servicePoolChangedCalled;
    AtomicInteger serviceAdded;
    AtomicInteger serviceRemoved;
    AtomicReference<String> servicePoolChangedServiceName;
    AtomicReference<String> servicePoolChangedServiceNameFromListener;


    AtomicReference<List<ServiceDefinition>> healthyServices;

    ConcurrentHashSet<ServiceDefinition> registeredServiceDefinitions;


    ConcurrentHashSet<ServiceHealthCheckIn> healthCheckIns;


    ServiceChangedEventChannel eventChannel = new ServiceChangedEventChannel() {
        @Override
        public void servicePoolChanged(String serviceName) {

            servicePoolChangedCalled.incrementAndGet();
            servicePoolChangedServiceName.set(serviceName);
        }
    };

    ServicePoolListener servicePoolListener = new ServicePoolListener() {
        @Override
        public void servicePoolChanged(String serviceName) {
            servicePoolChangedServiceNameFromListener.set(serviceName);
            puts(serviceName);
        }

        @Override
        public void serviceAdded(String serviceName) {
            puts ("serviceAdded", serviceName, serviceAdded.incrementAndGet());



        }

        @Override
        public void serviceRemoved(String serviceName) {
            puts ("serviceRemoved", serviceName);


            serviceRemoved.incrementAndGet();
        }

        @Override
        public void servicesAdded(String serviceName, int count) {

            puts ("servicesAdded", serviceName, count);

        }

        @Override
        public void servicesRemoved(String serviceName, int count) {

            puts ("servicesRemoved", serviceName, count);

        }
    };


    private ServiceDiscoveryProvider provider = new ServiceDiscoveryProvider() {
        @Override
        public void registerServices(Queue<ServiceDefinition> registerQueue) {


            registeredServiceDefinitions.addAll(registerQueue);
        }

        @Override
        public void checkIn(Queue<ServiceHealthCheckIn> checkInsQueue) {
            healthCheckIns.addAll(checkInsQueue);

        }

        @Override
        public List<ServiceDefinition> loadServices(String serviceName) {
            Sys.sleep(500);

            if (healthyServices.get()!=null) {
                return healthyServices.get();
            }
            return Collections.emptyList();
        }
    };



    @Before
    public void setup() {


        servicePoolChangedCalled = new AtomicInteger();
        serviceAdded = new AtomicInteger();
        serviceRemoved = new AtomicInteger();
        servicePoolChangedServiceName = new AtomicReference<>();
        servicePoolChangedServiceNameFromListener = new AtomicReference<>();
        registeredServiceDefinitions = new ConcurrentHashSet<>(100);
        healthCheckIns = new ConcurrentHashSet<>(100);

        serviceDiscovery = new ServiceDiscoveryImpl(createPeriodicScheduler(10), eventChannel, provider, servicePoolListener, null);

        healthyServices = new AtomicReference<>();




        serviceDiscovery.start();


    }

    @Test
    public void testRegisterService() throws Exception {
        serviceDiscovery.registerService("fooBar", 9090);


        AtomicReference<ServiceDefinition> serviceDefinitionAtomicReference = new AtomicReference<>();

        for (int index=0; index< 10; index++) {
            Sys.sleep(100);
            registeredServiceDefinitions.forEach(serviceDefinition -> {
                if (serviceDefinition.getName().equals("fooBar")) {
                    puts(serviceDefinition);
                    serviceDefinitionAtomicReference.set(serviceDefinition);
                }
            });

            if (serviceDefinitionAtomicReference.get()!=null) break;
        }


        assertNotNull(serviceDefinitionAtomicReference.get());

    }


    @Test
    public void testHealthCheckIn() throws Exception {

        final ServiceDefinition serviceDefinition = serviceDiscovery.registerService("fooBar", 9090);

        serviceDiscovery.checkIn(serviceDefinition.getId(), HealthStatus.PASS);


        AtomicReference<ServiceHealthCheckIn> ref = new AtomicReference<>();

        for (int index=0; index< 10; index++) {
            Sys.sleep(100);
            healthCheckIns.forEach(healthCheckIn -> {
                if (healthCheckIn.getServiceId().startsWith("fooBar-")) {
                    puts(healthCheckIn);
                    ref.set(healthCheckIn);
                }
            });

            if (ref.get()!=null) break;
        }


        assertNotNull(ref.get());
        assertEquals(serviceDefinition.getId(), ref.get().getServiceId());

    }


    @Test
    public void testNewServices() throws Exception {

        String serviceName = "fooBar";

        ServiceDefinition serviceDefinition1 = serviceDefinition(serviceName, "host1");
        ServiceDefinition serviceDefinition2 = serviceDefinition(serviceName, "host2");
        ServiceDefinition serviceDefinition3 = serviceDefinition(serviceName, "host3");


        List<ServiceDefinition> fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition2,
                serviceDefinition3
        );
        healthyServices.set(fooServices);
        loadServices(serviceName);
        assertEquals(3, serviceAdded.get());
        assertEquals(0, serviceRemoved.get());



    }


    @Test
    public void testAddThenRemove() throws Exception {

        String serviceName = "fooBar";

        ServiceDefinition serviceDefinition1 = serviceDefinition(serviceName, "host1");
        ServiceDefinition serviceDefinition2 = serviceDefinition(serviceName, "host2");
        ServiceDefinition serviceDefinition3 = serviceDefinition(serviceName, "host3");
        ServiceDefinition serviceDefinition4 = serviceDefinition(serviceName, "host4");


        List<ServiceDefinition> fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition2,
                serviceDefinition3
        );
        healthyServices.set(fooServices);
        loadServices(serviceName);
        assertEquals(3, serviceAdded.get());
        assertEquals(0, serviceRemoved.get());



        /* Now remove one. */
        serviceAdded.set(0);
        Sys.sleep(100);

        fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition3
        );
        healthyServices.set(fooServices);
        loadServices(serviceName);

        assertEquals(1, serviceRemoved.get());
        assertEquals(0, serviceAdded.get());


        /* Now add the one we just removed back
        back and add another new one. */
        serviceAdded.set(0);
        serviceRemoved.set(0);
        Sys.sleep(100);

        fooServices = serviceDefinitions(
                serviceDefinition1,
                serviceDefinition2,
                serviceDefinition3,
                serviceDefinition4
        );
        healthyServices.set(fooServices);
        loadServices(serviceName);

        assertEquals(0, serviceRemoved.get());
        assertEquals(2, serviceAdded.get());




    }


    private void loadServices(String serviceName) {
        for (int index = 0; index< 10; index++) {
            Sys.sleep(1000);
            final List<ServiceDefinition> serviceDefinitions = serviceDiscovery.loadServices(serviceName);
            puts(serviceDefinitions);

            if (serviceDefinitions.size() > 0) {
                break;
            }
        }
        Sys.sleep(100);

    }

    @After
    public void tearDown() {
        serviceDiscovery.stop();
    }



    public PeriodicScheduler createPeriodicScheduler(int poolSize) {


        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(poolSize,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("PeriodicTasks");
                    return thread;
                });

        return new PeriodicScheduler() {
            @Override
            public ScheduledFuture repeat(Runnable runnable, int interval, TimeUnit timeUnit) {
                return scheduledExecutorService.scheduleAtFixedRate(runnable, interval, interval, timeUnit);
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
                scheduledExecutorService.shutdown();
            }
        };

    }


}
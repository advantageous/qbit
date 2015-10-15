package io.advantageous.qbit.service.discovery.impl;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;
import io.advantageous.qbit.service.discovery.ServicePoolListener;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.util.ConcurrentHashSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.service.discovery.EndpointDefinition.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceDiscoveryImplTest {


    ServiceDiscoveryImpl serviceDiscovery;

    AtomicInteger servicePoolChangedCalled;
    AtomicInteger serviceAdded;
    AtomicInteger serviceRemoved;
    AtomicReference<String> servicePoolChangedServiceName;
    AtomicReference<String> servicePoolChangedServiceNameFromListener;


    AtomicReference<List<EndpointDefinition>> healthyServices;

    ConcurrentHashSet<EndpointDefinition> registeredEndpointDefinitions;


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
        public void serviceAdded(String serviceName, EndpointDefinition endpointDefinition) {
            puts("serviceAdded", serviceName, serviceAdded.incrementAndGet());


        }

        @Override
        public void serviceRemoved(String serviceName, EndpointDefinition endpointDefinition) {
            puts("serviceRemoved", serviceName);


            serviceRemoved.incrementAndGet();
        }

        @Override
        public void servicesAdded(String serviceName, int count) {

            puts("servicesAdded", serviceName, count);

        }

        @Override
        public void servicesRemoved(String serviceName, int count) {

            puts("servicesRemoved", serviceName, count);

        }
    };


    private ServiceDiscoveryProvider provider = new ServiceDiscoveryProvider() {
        @Override
        public void registerServices(Queue<EndpointDefinition> registerQueue) {


            registeredEndpointDefinitions.addAll(registerQueue);
        }

        @Override
        public void checkIn(Queue<ServiceHealthCheckIn> checkInsQueue) {
            healthCheckIns.addAll(checkInsQueue);

        }

        @Override
        public List<EndpointDefinition> loadServices(String serviceName) {
            Sys.sleep(500);

            if (healthyServices.get() != null) {
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
        registeredEndpointDefinitions = new ConcurrentHashSet<>(100);
        healthCheckIns = new ConcurrentHashSet<>(100);

        serviceDiscovery = new ServiceDiscoveryImpl(createPeriodicScheduler(10), eventChannel, provider, null, servicePoolListener, null, 5, 5);

        healthyServices = new AtomicReference<>();


        serviceDiscovery.start();


    }

    @Test
    public void testRegisterService() throws Exception {
        serviceDiscovery.register("fooBar", 9090);


        AtomicReference<EndpointDefinition> serviceDefinitionAtomicReference = new AtomicReference<>();

        for (int index = 0; index < 10; index++) {
            Sys.sleep(1000);
            registeredEndpointDefinitions.forEach(serviceDefinition -> {
                if (serviceDefinition.getName().equals("fooBar")) {
                    puts(serviceDefinition);
                    serviceDefinitionAtomicReference.set(serviceDefinition);
                }
            });

            if (serviceDefinitionAtomicReference.get() != null) break;
        }


        assertNotNull(serviceDefinitionAtomicReference.get());

    }


    @Test
    public void testHealthCheckIn() throws Exception {

        final EndpointDefinition endpointDefinition = serviceDiscovery.register("fooBar", 9090);

        serviceDiscovery.checkIn(endpointDefinition.getId(), HealthStatus.PASS);


        AtomicReference<ServiceHealthCheckIn> ref = new AtomicReference<>();

        for (int index = 0; index < 10; index++) {
            Sys.sleep(1000);
            healthCheckIns.forEach(healthCheckIn -> {
                if (healthCheckIn.getServiceId().startsWith("fooBar-")) {
                    puts(healthCheckIn);
                    ref.set(healthCheckIn);
                }
            });

            if (ref.get() != null) break;
        }


        assertNotNull(ref.get());
        assertEquals(endpointDefinition.getId(), ref.get().getServiceId());

    }


    @Test
    public void testNewServices() throws Exception {

        String serviceName = "fooBar";

        EndpointDefinition endpointDefinition1 = serviceDefinition(serviceName, "host1");
        EndpointDefinition endpointDefinition2 = serviceDefinition(serviceName, "host2");
        EndpointDefinition endpointDefinition3 = serviceDefinition(serviceName, "host3");


        List<EndpointDefinition> fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition2,
                endpointDefinition3
        );
        healthyServices.set(fooServices);
        loadServices(serviceName);
        assertEquals(3, serviceAdded.get());
        assertEquals(0, serviceRemoved.get());

        assertEquals(serviceName, servicePoolChangedServiceName.get());
        assertEquals(serviceName, servicePoolChangedServiceNameFromListener.get());


    }


    @Test
    public void testAddThenRemove() throws Exception {

        String serviceName = "fooBar";

        EndpointDefinition endpointDefinition1 = serviceDefinitionWithId(serviceName, "host1", UUID.randomUUID().toString());
        EndpointDefinition endpointDefinition2 = serviceDefinitionWithId(serviceName, "host2", UUID.randomUUID().toString());
        EndpointDefinition endpointDefinition3 = serviceDefinitionWithId(serviceName, "host3", UUID.randomUUID().toString());
        EndpointDefinition endpointDefinition4 = serviceDefinitionWithId(serviceName, "host4", UUID.randomUUID().toString());


        List<EndpointDefinition> fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition2,
                endpointDefinition3
        );
        healthyServices.set(fooServices);
        loadServices(serviceName);
        Sys.sleep(2_000);
        assertEquals(3, serviceAdded.get());
        assertEquals(0, serviceRemoved.get());



        /* Now remove one. */
        serviceAdded.set(0);
        Sys.sleep(2_000);

        fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition3
        );
        healthyServices.set(fooServices);
        Sys.sleep(2_000);
        loadServices(serviceName);
        Sys.sleep(2_000);

        assertEquals(1, serviceRemoved.get());
        assertEquals(0, serviceAdded.get());


        /* Now add the one we just removed back
        back and add another new one. */
        serviceAdded.set(0);
        serviceRemoved.set(0);

        fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition2,
                endpointDefinition3,
                endpointDefinition4
        );
        healthyServices.set(fooServices);
        Sys.sleep(2_000);
        loadServices(serviceName);
        Sys.sleep(7_000);

        assertEquals(0, serviceRemoved.get());
        assertEquals(2, serviceAdded.get());


    }


    private void loadServices(String serviceName) {
        for (int index = 0; index < 10; index++) {
            Sys.sleep(1000);
            final List<EndpointDefinition> endpointDefinitions = serviceDiscovery.loadServices(serviceName);
            puts(endpointDefinitions);

            if (endpointDefinitions.size() > 0) {
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
package io.advantageous.qbit.service.discovery.impl;

import io.advantageous.boon.core.IO;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryFileSystemProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
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

/**
 * created by rick on 5/20/15.
 */
public class ServiceDiscoveryWithFileSystemTest {


    ServiceDiscovery serviceDiscovery;

    AtomicInteger servicePoolChangedCalled;
    AtomicInteger serviceAdded;
    AtomicInteger serviceRemoved;
    AtomicReference<String> servicePoolChangedServiceName;
    AtomicReference<String> servicePoolChangedServiceNameFromListener;

    File dir;


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


    @Before
    public void setup() throws Exception {

        dir = File.createTempFile("testSome", "testSome").getParentFile();


        servicePoolChangedCalled = new AtomicInteger();
        serviceAdded = new AtomicInteger();
        serviceRemoved = new AtomicInteger();
        servicePoolChangedServiceName = new AtomicReference<>();
        servicePoolChangedServiceNameFromListener = new AtomicReference<>();
        ServiceDiscoveryFileSystemProvider provider =
                new ServiceDiscoveryFileSystemProvider(dir, 50);


        serviceDiscovery = ServiceDiscoveryBuilder.serviceDiscoveryBuilder()
                .setPeriodicScheduler(createPeriodicScheduler(10))
                .setServiceChangedEventChannel(eventChannel)
                .setServicePoolListener(servicePoolListener)
                .setServiceDiscoveryProvider(provider)
                .setPollForServicesIntervalSeconds(1).build();


        serviceDiscovery.start();


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

        write(fooServices);

        Sys.sleep(3_000);

        loadServices(serviceName);

        Sys.sleep(3_000);

        assertEquals(serviceName, servicePoolChangedServiceName.get());
        assertEquals(serviceName, servicePoolChangedServiceNameFromListener.get());


    }

    private void write(List<EndpointDefinition> fooServices) throws Exception {
        JsonSerializer jsonSerializer = new JsonSerializerFactory().create();
        String json = jsonSerializer.serialize(fooServices).toString();

        File outputFile = new File(dir, "fooBar.json");

        IO.write(outputFile.toPath(), json);

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


        write(fooServices);

        loadServices(serviceName);



        /* Now remove one. */
        serviceAdded.set(0);
        Sys.sleep(100);

        fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition3
        );

        write(fooServices);

        Sys.sleep(2_000);
        loadServices(serviceName);

        assertEquals(1, serviceRemoved.get());
        assertEquals(0, serviceAdded.get());


        /* Now add the one we just removed back
        back and add another new one. */
        serviceAdded.set(0);
        serviceRemoved.set(0);
        Sys.sleep(100);

        fooServices = serviceDefinitions(
                endpointDefinition1,
                endpointDefinition2,
                endpointDefinition3,
                endpointDefinition4
        );

        write(fooServices);

        Sys.sleep(2_000);
        loadServices(serviceName);

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

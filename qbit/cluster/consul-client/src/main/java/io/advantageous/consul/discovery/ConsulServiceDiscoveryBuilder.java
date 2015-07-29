package io.advantageous.consul.discovery;

import io.advantageous.boon.core.IO;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.consul.discovery.spi.ConsulServiceDiscoveryProvider;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.ServicePoolListener;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryFileSystemProvider;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ConsulServiceDiscoveryBuilder
 * created by rhightower on 3/23/15.
 */
public class ConsulServiceDiscoveryBuilder {


    private String consulHost = "localhost";
    private int consulPort = 8500;
    private String datacenter = "dc1";
    private String tag;
    private int longPollTimeSeconds = 5;
    private PeriodicScheduler periodicScheduler;
    private ServiceChangedEventChannel serviceChangedEventChannel;
    private File backupDir;
    private ServicePoolListener servicePoolListener;
    private ExecutorService executorService;

    public static ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder() {
        return new ConsulServiceDiscoveryBuilder();
    }

    public File getBackupDir() {
        return backupDir;
    }

    public ConsulServiceDiscoveryBuilder setBackupDir(File backupDir) {
        this.backupDir = backupDir;
        return this;
    }

    public String getConsulHost() {
        return consulHost;
    }

    public ConsulServiceDiscoveryBuilder setConsulHost(String consulHost) {
        this.consulHost = consulHost;
        return this;
    }

    public int getConsulPort() {
        return consulPort;
    }

    public ConsulServiceDiscoveryBuilder setConsulPort(int consulPort) {
        this.consulPort = consulPort;
        return this;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public ConsulServiceDiscoveryBuilder setDatacenter(String datacenter) {
        this.datacenter = datacenter;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public ConsulServiceDiscoveryBuilder setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public int getLongPollTimeSeconds() {
        return longPollTimeSeconds;
    }

    public ConsulServiceDiscoveryBuilder setLongPollTimeSeconds(@SuppressWarnings("SameParameterValue") int longPollTimeSeconds) {
        this.longPollTimeSeconds = longPollTimeSeconds;
        return this;
    }

    public PeriodicScheduler getPeriodicScheduler() {
        return periodicScheduler;
    }

    public ConsulServiceDiscoveryBuilder setPeriodicScheduler(PeriodicScheduler periodicScheduler) {
        this.periodicScheduler = periodicScheduler;
        return this;
    }

    public ServiceChangedEventChannel getServiceChangedEventChannel() {
        return serviceChangedEventChannel;
    }

    public ConsulServiceDiscoveryBuilder setServiceChangedEventChannel(ServiceChangedEventChannel serviceChangedEventChannel) {
        this.serviceChangedEventChannel = serviceChangedEventChannel;
        return this;
    }

    public ServicePoolListener getServicePoolListener() {
        return servicePoolListener;
    }

    public ConsulServiceDiscoveryBuilder setServicePoolListener(ServicePoolListener servicePoolListener) {
        this.servicePoolListener = servicePoolListener;
        return this;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ConsulServiceDiscoveryBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public ServiceDiscoveryImpl build() {

        final ConsulServiceDiscoveryProvider consulServiceDiscoveryProvider =
                new ConsulServiceDiscoveryProvider(getConsulHost(), getConsulPort(), getDatacenter(), getTag(), getLongPollTimeSeconds());

        if (backupDir == null) {
            return new ServiceDiscoveryImpl(
                    getPeriodicScheduler(), getServiceChangedEventChannel(),
                    consulServiceDiscoveryProvider, null,
                    getServicePoolListener(),
                    getExecutorService(), 5, 5);
        } else {

            final AtomicReference<ServiceDiscovery> ref = new AtomicReference<>();
            final File dir = this.backupDir;
            ServiceDiscoveryProvider backup = new ServiceDiscoveryFileSystemProvider(backupDir, 1_000);
            ServicePoolListener poolListener = serviceName -> {
                ServiceDiscovery serviceDiscovery = ref.get();
                if (serviceDiscovery != null) {
                    List<EndpointDefinition> endpointDefinitions = serviceDiscovery.loadServices(serviceName);
                    JsonSerializer jsonSerializer = new JsonSerializerFactory().create();
                    String json = jsonSerializer.serialize(endpointDefinitions).toString();
                    File outputFile = new File(dir, serviceName + ".json");
                    IO.write(outputFile.toPath(), json);
                }
            };

            this.setServicePoolListener(poolListener);

            ServiceDiscoveryImpl serviceDiscovery = new ServiceDiscoveryImpl(
                    getPeriodicScheduler(), getServiceChangedEventChannel(),
                    consulServiceDiscoveryProvider, backup,
                    getServicePoolListener(),
                    getExecutorService(), 5, 5);

            ref.set(serviceDiscovery);

            return serviceDiscovery;

        }

    }
}

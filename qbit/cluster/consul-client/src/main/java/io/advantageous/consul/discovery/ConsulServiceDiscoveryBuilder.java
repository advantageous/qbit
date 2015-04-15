package io.advantageous.consul.discovery;

import io.advantageous.consul.discovery.spi.ConsulServiceDiscoveryProvider;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;
import io.advantageous.qbit.service.discovery.ServicePoolListener;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;

import java.util.concurrent.ExecutorService;

/**
 * ConsulServiceDiscoveryBuilder
 * Created by rhightower on 3/23/15.
 */
public class ConsulServiceDiscoveryBuilder {



    public static ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder () {
        return new ConsulServiceDiscoveryBuilder();
    }


    private String consulHost = "localhost";
    private int consulPort = 8500;
    private String datacenter;
    private String tag;
    private int longPollTimeSeconds=5;
    private PeriodicScheduler periodicScheduler;
    private ServiceChangedEventChannel serviceChangedEventChannel;

    private ServicePoolListener servicePoolListener;
    private ExecutorService executorService;

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

    public ConsulServiceDiscoveryBuilder setLongPollTimeSeconds(int longPollTimeSeconds) {
        this.longPollTimeSeconds = longPollTimeSeconds;
        return this;
    }

    public PeriodicScheduler getPeriodicScheduler() {
        return periodicScheduler;
    }

    public void setPeriodicScheduler(PeriodicScheduler periodicScheduler) {
        this.periodicScheduler = periodicScheduler;
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

        return new ServiceDiscoveryImpl(
                getPeriodicScheduler(), getServiceChangedEventChannel(),
                consulServiceDiscoveryProvider,
                getServicePoolListener(),
                getExecutorService());

    }
}

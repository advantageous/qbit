package io.advantageous.qbit.spring.config;

import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.eventbus.EventBusCluster;
import io.advantageous.qbit.eventbus.EventBusClusterBuilder;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.events.impl.EventConnectorHub;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.ServiceDiscoveryBuilder;
import io.advantageous.qbit.service.discovery.impl.ServiceDiscoveryImpl;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryFileSystemProvider;
import io.advantageous.qbit.spring.annotation.Clustered;
import io.advantageous.qbit.spring.annotation.QBitPublisher;
import io.advantageous.qbit.spring.properties.ConsulProperties;
import io.advantageous.qbit.spring.properties.EventBusProperties;
import io.advantageous.qbit.spring.properties.ServiceDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;

/**
 * Configuration for QBit clustering.  This includes the remote service bus, remote proxy creator and service discovery.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Configuration
@EnableConfigurationProperties({ConsulProperties.class, EventBusProperties.class,
        ServiceDiscoveryProperties.class})
public class ClusterConfiguration {

    private final Logger logger = LoggerFactory.getLogger(ClusterConfiguration.class);

    @Bean
    public EventConnectorHub eventConnectorHub() {
        return new EventConnectorHub();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDiscovery.class)
    public ServiceDiscovery serviceDiscovery(final ConsulProperties consulProperties,
                                             final @QBitPublisher
                                             ServiceChangedEventChannel servicePoolUpdateEventChannel,
                                             final ServiceDiscoveryProperties discoveryProperties) {

        if (!(consulProperties.getDatacenter() == null || consulProperties.getDatacenter().isEmpty())) {

            /** Depending on consul as the default or only seems like a mistake. */
            final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder();

            consulServiceDiscoveryBuilder
                    .setConsulHost(consulProperties.getHost())
                    .setConsulPort(consulProperties.getPort())
                    .setDatacenter(consulProperties.getDatacenter())
                    .setServiceChangedEventChannel(servicePoolUpdateEventChannel)
                    .build();


            if (consulProperties.getBackupDir() != null) {
                final File backupDir = new File(consulProperties.getBackupDir());
                if (!backupDir.exists()) {
                    if (!backupDir.mkdirs()) {
                        logger.error(String.format("Backup dir %s does not exist and can't be created", backupDir));
                    }
                }
                consulServiceDiscoveryBuilder.setBackupDir(backupDir);
            }

            final ServiceDiscoveryImpl serviceDiscovery = consulServiceDiscoveryBuilder.build();
            serviceDiscovery.start();
            return serviceDiscovery;
        } else {
            final ServiceDiscoveryBuilder serviceDiscoveryBuilder = ServiceDiscoveryBuilder.serviceDiscoveryBuilder()
                    .setServiceDiscoveryProvider(new ServiceDiscoveryFileSystemProvider(discoveryProperties.getDir(),
                            discoveryProperties.getCheckIntervalMS()));
            return serviceDiscoveryBuilder.build();
        }
    }

    @Bean
    public EventBusCluster eventBusCluster(final @Qualifier("clusteredEventManagerImpl") EventManager eventManager,
                                           final EventConnectorHub eventConnectorHub,
                                           final ServiceDiscovery serviceDiscovery,
                                           final EventBusProperties props) {

        logger.info("Binding event bus to " + props.getPort());
        final EventBusClusterBuilder clusterBuilder = EventBusClusterBuilder.eventBusClusterBuilder();
        clusterBuilder.setServiceDiscovery(serviceDiscovery);
        clusterBuilder.setReplicationPortLocal(props.getPort());
        clusterBuilder.setEventBusName(props.getName());
        clusterBuilder.setEventManager(eventManager);
        clusterBuilder.setEventConnectorHub(eventConnectorHub);
        clusterBuilder.setPeerCheckTimeUnit(TimeUnit.SECONDS);
        clusterBuilder.setPeerCheckTimeInterval(props.getPeriodicCheckInSeconds());
        clusterBuilder.setReplicationServerCheckInInterval(props.getTtl());
        clusterBuilder.setReplicationServerCheckInTimeUnit(TimeUnit.SECONDS);
        final EventBusCluster cluster = clusterBuilder.build();
        try {
            cluster.start();
        } catch (final Exception e) {
            logger.error("Failed to start cluster for event bus.  Make sure you can connect to consul.");
            logger.error("Consul is down.  The event bus predates ServiceDiscovery");
            logger.error("ServiceDiscovery will default to using JSON files for peer endpoint config.");
            logger.error("The stats count is using service discovery, event bus is not yet.");
        }
        return cluster;
    }

    /**
     * The actual service queue for the clustered event bus.
     * All events are sent to this queue first.
     *
     * @param eventBusCluster actual event manager impl no proxy
     * @return the service queue
     */
    @Bean
    public ServiceQueue clusteredEventManagerServiceQueue(final @Qualifier("eventBusCluster")
                                                          EventBusCluster eventBusCluster) {

        if (eventBusCluster == null) {
            return null;
        }
        return eventBusCluster.eventServiceQueue();
    }

    @Bean
    @Clustered
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventManager clusteredEventManager(final @Qualifier("clusteredEventManagerServiceQueue")
                                              ServiceQueue eventManagerServiceQueue) {

        return eventManagerServiceQueue.createProxy(EventManager.class);
    }

    /**
     * Wraps factory method call so we can provide another implementation of interface if needed.
     *
     * @param eventConnectorHub the cluster of event connectors
     * @return the event manager
     */
    @Bean
    public EventManager clusteredEventManagerImpl(final EventConnectorHub eventConnectorHub) {
        return EventManagerBuilder.eventManagerBuilder()
                .setEventConnector(eventConnectorHub)
                .setName("CLUSTERED_EVENT_MANAGER").build();
    }

    @Bean
    @QBitPublisher
    public ServiceChangedEventChannel servicePoolUpdateEventChannel(final EventBusProxyCreator eventBusProxyCreator) {
        return eventBusProxyCreator.createProxy(ServiceChangedEventChannel.class);
    }
}

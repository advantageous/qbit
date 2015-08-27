package io.advantageous.qbit.spring.config;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.admin.AdminBuilder;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.service.impl.BoonServiceMethodCallHandler;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.spring.ApplicationInitializer;
import io.advantageous.qbit.spring.ServiceQueueInitializer;
import io.advantageous.qbit.spring.properties.AppProperties;
import io.advantageous.qbit.spring.properties.RequestQueueProperties;
import io.advantageous.qbit.spring.properties.ResponseQueueProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import java.util.Optional;

/**
 * Main QBit configuration.  This sets up the event bus infrastructure and provides queue builders for the request and
 * response queues.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Configuration
@Import({ServiceQueueCreator.class})
@EnableConfigurationProperties({AppProperties.class, RequestQueueProperties.class, ResponseQueueProperties.class})
public class PlatformConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HealthServiceAsync healthServiceAsync() {
        return HealthServiceBuilder.healthServiceBuilder().buildHealthSystemReporter();
    }

    @Bean
    public ServiceEndpointServer adminServiceEndpointServer(final HealthServiceAsync healthServiceAsync,
                                                            final @Qualifier("qbitStatsCollector")
                                                            Optional<StatsCollector> statsCollector,
                                                            final AppProperties props) {

        final AdminBuilder adminBuilder = AdminBuilder.adminBuilder()
                .setPort(props.getAdminPort())
                .setHost(props.getAdminHost())
                .setMicroServiceName(props.getPrefix())
                .setHealthService(healthServiceAsync);

        if (statsCollector.isPresent()) {
            adminBuilder.registerJavaVMStatsJobEveryNSeconds(statsCollector.get(), props.getJvmStatsRefresh());
        }

        return adminBuilder.build().startServer();
    }

    @Bean
    public QueueBuilder requestQueueBuilder(final RequestQueueProperties properties) {

        return new QueueBuilder()
                .setBatchSize(properties.getBatchSize())
                .setSize(properties.getBatchCount())
                .setPollWait(properties.getPollWait());
    }

    @Bean
    public Queue<Response<Object>> sharedResponseQueue(final ResponseQueueProperties properties) {

        return new QueueBuilder()
                .setBatchSize(properties.getBatchSize())
                .setSize(properties.getBatchCount())
                .setPollWait(properties.getPollWait()).build();
    }

    /**
     * EventBusProxyCreator is used to create strongly typed event proxies.
     * This helps hide the fact that the service call is really a message for the event bus.
     * Wraps factory method call so we can provide another implementation of interface
     * if needed.
     *
     * @return the event bus creator utility
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventBusProxyCreator eventBusProxyCreator() {

        return QBit.factory().eventBusProxyCreator();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public BoonServiceMethodCallHandler dynamicInvokingBoonServiceMethodCallHandler() {
        return new BoonServiceMethodCallHandler(true);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ServiceBuilder serviceQueueBuilder() {
        return new ServiceBuilder();
    }

    @Bean
    public ServiceQueueInitializer serviceQueueInitializer() {
        return new ServiceQueueInitializer();
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationInitializer.class)
    public ApplicationInitializer defaultApplicationInitializer() {
        return new ApplicationInitializer() {
            @Override
            protected void initialize() {
            }
        };
    }

}

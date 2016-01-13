package io.advantageous.qbit.spring.config;

import io.advantageous.qbit.admin.AdminBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.spring.properties.AppProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Optional;

/**
 * Configuration for QBit internal admin and health services.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AdminConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HealthServiceAsync healthServiceAsync() {
        return HealthServiceBuilder.healthServiceBuilder().buildAndStart();
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

        if (statsCollector.isPresent() && props.getJvmStatsRefresh() > 0) {
            adminBuilder.registerJavaVMStatsJobEveryNSeconds(statsCollector.get(), props.getJvmStatsRefresh());
        }

        return adminBuilder.build().startServer();
    }
}

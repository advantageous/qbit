package io.advantageous.qbit.spring.config;

import com.rdio.api.proxy.config.properties.StatsdProperties;
import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.metrics.StatService;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.metrics.support.StatsDReplicatorBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.stats.StatsCollector;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

/**
 * Configuration for QBit stats.  The stats server collects and aggregates data for stats publication.
 *
 * @author gcc@rd.io (Geoff Chandler)
 * @author rick
 */
@Configuration
@EnableConfigurationProperties({StatsdProperties.class})
public class StatsConfiguration {

    @Bean
    public StatReplicator statsDReplicator(final StatsdProperties statsD) {

        return StatsDReplicatorBuilder.statsDReplicatorBuilder()
                .setBufferSize(statsD.getBufferSize())
                .setFlushRateIntervalMS(statsD.getFlushRateIntervalMS())
                .setHost(statsD.getHost())
                .setPort(statsD.getPort())
                .buildAndStart();
    }

    @Bean
    public ServiceQueue qbitStatsServiceQueue(final @Named("statsDReplicator") StatReplicator statReplicator) {

        return StatServiceBuilder.statServiceBuilder()
                .setTimeToLiveCheckInterval(1_000)
                .addReplicator(statReplicator)
                .buildServiceQueue()
                .startServiceQueue();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StatService qbitStatService(final @Named("qbitStatsServiceQueue") ServiceQueue qbitStatsServiceQueue) {

        return qbitStatsServiceQueue.createProxy(StatService.class);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StatsCollector qbitStatsCollector(final @Named("qbitStatsServiceQueue") ServiceQueue qbitStatsServiceQueue) {

        return qbitStatsServiceQueue.createProxy(StatsCollector.class);
    }
}

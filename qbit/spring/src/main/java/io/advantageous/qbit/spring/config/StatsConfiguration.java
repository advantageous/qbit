package io.advantageous.qbit.spring.config;

import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.metrics.StatService;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.metrics.support.StatsDReplicatorBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.service.stats.StatsCollectorBuffer;
import io.advantageous.qbit.spring.properties.StatsdProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Optional;

/**
 * Configuration for QBit stats.  The stats server collects and aggregates data for stats publication.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 * @author rick
 */
@Configuration
@EnableConfigurationProperties({StatsdProperties.class})
public class StatsConfiguration {

    @Bean
    public StatReplicator statsDReplicator(final StatsdProperties statsD) {

        if (statsD.getHost() != null && !statsD.getHost().isEmpty()) {
            return StatsDReplicatorBuilder.statsDReplicatorBuilder()
                    .setBufferSize(statsD.getBufferSize())
                    .setFlushRateIntervalMS(statsD.getFlushRateIntervalMS())
                    .setHost(statsD.getHost())
                    .setPort(statsD.getPort())
                    .buildAndStart();
        } else {
            return null;
        }
    }

    @Bean
    public ServiceQueue qbitStatsServiceQueue(final @Qualifier("statsDReplicator")
                                              Optional<StatReplicator> statReplicator) {

        final StatServiceBuilder statServiceBuilder = StatServiceBuilder.statServiceBuilder();
        if (statReplicator.isPresent()) {
            statServiceBuilder.addReplicator(statReplicator.get());
        }
        return statServiceBuilder.setTimeToLiveCheckInterval(1_000)
                .buildServiceQueue()
                .startServiceQueue();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StatService qbitStatService(final @Qualifier("qbitStatsServiceQueue") ServiceQueue qbitStatsServiceQueue) {

        return qbitStatsServiceQueue.createProxy(StatService.class);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StatsCollector qbitStatsCollector(final @Qualifier("qbitStatService")
                                             StatService qbitStatService) {

        return new StatsCollectorBuffer(qbitStatService);
    }

}

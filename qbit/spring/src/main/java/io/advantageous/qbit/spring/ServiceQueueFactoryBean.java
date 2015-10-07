package io.advantageous.qbit.spring;

import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.spring.properties.AppProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Spring factory bean that wraps the QBit ServiceBuilder.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Component
public class ServiceQueueFactoryBean extends ServiceBuilder implements FactoryBean<ServiceQueue> {

    @Autowired(required = false)
    @Qualifier("qbitStatsCollector")
    private StatsCollector statsCollector;

    @Autowired(required = false)
    private HealthServiceAsync healthServiceAsync;

    @Autowired(required = false)
    private AppProperties appProperties;

    private String beanName;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public ServiceQueue getObject() throws Exception {

        if (appProperties != null && healthServiceAsync != null) {
            final String longName = appProperties.getPrefix() + this.getBeanName();
            registerHealthChecksWithTTLInSeconds(healthServiceAsync, longName, appProperties.getHealthCheckTtlSeconds());
            final int flushTimeSeconds = appProperties.getStatsFlushSeconds();
            final int sampleEvery = appProperties.getSampleEvery();

            if (statsCollector != null)
                this.registerStatsCollections(longName, statsCollector, flushTimeSeconds, sampleEvery);
        }
        return super.build();
    }

    @Override
    public Class<?> getObjectType() {
        return ServiceQueue.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

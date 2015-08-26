package io.advantageous.qbit.spring;

import io.advantageous.qbit.spring.properties.AppProperties;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.stats.StatsCollector;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Spring factory bean that wraps the QBit ServiceBuilder.
 *
 * @author gcc@rd.io (Geoff Chandler)
 */
@Component
public class ServiceQueueFactoryBean extends ServiceBuilder implements FactoryBean<ServiceQueue> {

    @Autowired(required = false)
    @Named("qbitStatsCollector")
    private StatsCollector statsCollector;

    @Inject
    private HealthServiceAsync healthServiceAsync;

    @Inject
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

        final String longName = appProperties.getPrefix() + this.getBeanName();
        registerHealthChecksWithTTLInSeconds(healthServiceAsync, longName, appProperties.getHealthCheckTtlSeconds());
        final int flushTimeSeconds = appProperties.getStatsFlushSeconds();
        final int sampleEvery = appProperties.getSampleEvery();

        if (statsCollector != null)
            this.registerStatsCollections(longName, statsCollector, flushTimeSeconds, sampleEvery);

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

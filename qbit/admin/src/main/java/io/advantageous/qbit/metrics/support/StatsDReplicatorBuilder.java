package io.advantageous.qbit.metrics.support;


import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.queue.UnableToEnqueueHandler;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * created by rhightower on 5/22/15.
 */
public class StatsDReplicatorBuilder {

    public static final String STATSD_REPLICATOR_PROPS = "qbit.statsd.replicator.";
    private String host = "localhost";
    private int port = 8125;
    private boolean multiMetrics = true;
    private int bufferSize = 1500;
    private int flushRateIntervalMS = 1000;
    private ServiceBuilder serviceBuilder;
    private ServiceQueue serviceQueue;

    public StatsDReplicatorBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(STATSD_REPLICATOR_PROPS));
    }

    public StatsDReplicatorBuilder(PropertyResolver propertyResolver) {

        this.host = propertyResolver.getStringProperty("host", host);
        this.port = propertyResolver.getIntegerProperty("port", port);
        this.multiMetrics = propertyResolver.getBooleanProperty("multiMetrics", multiMetrics);
        this.bufferSize = propertyResolver.getIntegerProperty("bufferSize", bufferSize);
        this.flushRateIntervalMS = propertyResolver.getIntegerProperty("flushRateIntervalMS", flushRateIntervalMS);

    }

    public StatsDReplicatorBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(
                STATSD_REPLICATOR_PROPS, properties));
    }

    public static StatsDReplicatorBuilder statsDReplicatorBuilder() {
        return new StatsDReplicatorBuilder();
    }

    public ServiceBuilder getServiceBuilder() {

        if (serviceBuilder == null) {
            serviceBuilder = ServiceBuilder.serviceBuilder();
            return serviceBuilder;
        }
        return serviceBuilder.copy();
    }

    public void setServiceBuilder(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
    }

    public String getHost() {
        return host;
    }

    public StatsDReplicatorBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public StatsDReplicatorBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isMultiMetrics() {
        return multiMetrics;
    }

    public StatsDReplicatorBuilder setMultiMetrics(boolean multiMetrics) {
        this.multiMetrics = multiMetrics;
        return this;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public StatsDReplicatorBuilder setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public int getFlushRateIntervalMS() {
        return flushRateIntervalMS;
    }

    public StatsDReplicatorBuilder setFlushRateIntervalMS(int flushRateIntervalMS) {
        this.flushRateIntervalMS = flushRateIntervalMS;
        return this;
    }


    private void buildQueue() {

        final StatsDReplicator statsDReplicator = createStatsDReplicator();

        final ServiceBuilder serviceBuilder = this.getServiceBuilder();
        serviceBuilder.getRequestQueueBuilder().setUnableToEnqueueHandler(
                new UnableToEnqueueHandler() {
                    public boolean unableToEnqueue(BlockingQueue<Object> queue, String queueName, Object item) {

                        final Logger logger = LoggerFactory.getLogger(StatsDReplicator.class);

                        logger.error("Unable to send method call to StatsDReplicator " + queueName);
                        queue.clear();
                        return false;
                    }
                }
        );

        serviceBuilder.getResponseQueueBuilder().setUnableToEnqueueHandler(new UnableToEnqueueHandler() {


            public boolean unableToEnqueue(BlockingQueue<Object> queue, String queueName, Object item) {

                final Logger logger = LoggerFactory.getLogger(StatsDReplicator.class);


                logger.error("Unable to send response from method call from StatsDReplicator " + queueName);
                queue.clear();
                return false;
            }
        });
        serviceBuilder.setServiceObject(statsDReplicator);
        this.serviceQueue = serviceBuilder.buildAndStartAll();

    }

    public ServiceQueue getServiceQueue() {
        return serviceQueue;
    }

    public StatReplicator build() {

        buildQueue();
        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("UnnecessaryLocalVariable") final StatReplicator proxyWithAutoFlush =
                serviceQueue.createProxyWithAutoFlush(StatReplicator.class, 100, TimeUnit.MILLISECONDS);

        return proxyWithAutoFlush;
    }


    public StatReplicator buildAndStart() {
        buildQueue();
        final StatReplicator proxyWithAutoFlush =
                serviceQueue.createProxyWithAutoFlush(StatReplicator.class, 100, TimeUnit.MILLISECONDS);
        return proxyWithAutoFlush;
    }


    private StatsDReplicator createStatsDReplicator() {
        try {
            return new StatsDReplicator(getHost(),
                    getPort(), this.isMultiMetrics(),
                    this.getBufferSize(), this.getFlushRateIntervalMS());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}


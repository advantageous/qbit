package io.advantageous.qbit.admin;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.metrics.support.LocalStatsCollectorBuilder;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.system.QBitSystemManager;

/** This is a utility class for when you are running in a PaaS like Heroku or Docker.
 *  It also allows you to share stat, health and system manager setup.
 *
 **/
public class ManagedServiceBuilder {


    private QBitSystemManager systemManager;
    private StatServiceBuilder statServiceBuilder;
    private EndpointServerBuilder endpointServerBuilder;
    private LocalStatsCollectorBuilder localStatsCollectorBuilder;
    private HttpServerBuilder httpServerBuilder;
    private boolean enableLocalStats=true;
    private boolean enableStatsD=false;
    private boolean enableLocalHealth=true;
    private HealthServiceBuilder healthServiceBuilder;
    private HealthServiceAsync healthService;
    private boolean enableStats = true;
    private int sampleStatFlushRate = 5;
    private int checkTimingEveryXCalls = 100;


    public ManagedServiceBuilder setHealthServiceBuilder(final HealthServiceBuilder healthServiceBuilder) {
        this.healthServiceBuilder = healthServiceBuilder;
        return this;
    }

    public HealthServiceBuilder getHealthServiceBuilder() {

        if (healthServiceBuilder == null) {
            healthServiceBuilder = HealthServiceBuilder.healthServiceBuilder();
        }
        return healthServiceBuilder;
    }

    public static ManagedServiceBuilder managedServiceBuilder() {
        return new ManagedServiceBuilder();
    }

    public HttpServerBuilder getHttpServerBuilder() {
        if (httpServerBuilder == null) {
            httpServerBuilder = HttpServerBuilder.httpServerBuilder();

            String port = System.getenv("WEB_PORT");
            if (Str.isEmpty(port)) {
                port = System.getenv("PORT");
            }
            if (!Str.isEmpty(port)) {
                httpServerBuilder.setPort(Integer.parseInt(port));
            }
        }
        return httpServerBuilder;
    }

    public ManagedServiceBuilder setHttpServerBuilder(HttpServerBuilder httpServerBuilder) {
        this.httpServerBuilder = httpServerBuilder;
        return this;
    }

    public boolean isEnableStatsD() {
        return enableStatsD;
    }

    public ManagedServiceBuilder setEnableStatsD(boolean enableStatsD) {
        this.enableStatsD = enableStatsD;
        return this;
    }

    public LocalStatsCollectorBuilder getLocalStatsCollectorBuilder() {
        if (localStatsCollectorBuilder==null) {
            localStatsCollectorBuilder = LocalStatsCollectorBuilder.localStatsCollectorBuilder();
        }
        return localStatsCollectorBuilder;
    }

    public ManagedServiceBuilder setLocalStatsCollectorBuilder(LocalStatsCollectorBuilder localStatsCollectorBuilder) {
        this.localStatsCollectorBuilder = localStatsCollectorBuilder;
        return this;
    }

    public boolean isEnableLocalStats() {
        return enableLocalStats;
    }

    public ManagedServiceBuilder setEnableLocalStats(boolean enableLocalStats) {
        this.enableLocalStats = enableLocalStats;
        return this;
    }

    public boolean isEnableLocalHealth() {
        return enableLocalHealth;
    }

    public ManagedServiceBuilder setEnableLocalHealth(boolean enableLocalHealth) {
        this.enableLocalHealth = enableLocalHealth;
        return this;
    }

    public QBitSystemManager getSystemManager() {

        if (systemManager == null) {
            systemManager = new QBitSystemManager();
        }
        return systemManager;
    }

    public ManagedServiceBuilder setSystemManager(QBitSystemManager systemManager) {
        this.systemManager = systemManager;
        return this;
    }



    public StatServiceBuilder getStatServiceBuilder() {
        if (statServiceBuilder == null) {
            statServiceBuilder =  StatServiceBuilder.statServiceBuilder();

            if (enableLocalStats) {
                statServiceBuilder.addReplicator(getLocalStatsCollectorBuilder().buildAndStart());
            }

            statServiceBuilder.build();
            statServiceBuilder.buildServiceQueue().startCallBackHandler().start();

        }
        return statServiceBuilder;
    }

    public ManagedServiceBuilder setStatServiceBuilder(StatServiceBuilder statServiceBuilder) {

        this.statServiceBuilder = statServiceBuilder;
        return this;
    }

    public EndpointServerBuilder getEndpointServerBuilder() {
        if (endpointServerBuilder==null) {
            endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();
            endpointServerBuilder.setEnableHealthEndpoint(isEnableLocalHealth());
            endpointServerBuilder.setHealthService(getHealthService());
            endpointServerBuilder.setSystemManager(this.getSystemManager());
            endpointServerBuilder.setHttpServer(getHttpServerBuilder().build());
            endpointServerBuilder.setStatsFlushRateSeconds(getSampleStatFlushRate());
            endpointServerBuilder.setCheckTimingEveryXCalls(getCheckTimingEveryXCalls());



            if (isEnableStats()) {

                endpointServerBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollectorWithAutoFlush());
            }

            if (isEnableLocalStats()) {
                endpointServerBuilder.setEnableStatEndpoint(true);
                endpointServerBuilder.setStatsCollection(getLocalStatsCollectorBuilder().build());
            }

        }

        return endpointServerBuilder;
    }


    public EndpointServerBuilder createEndpointServerBuilder() {

        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();
        endpointServerBuilder.setEnableHealthEndpoint(isEnableLocalHealth());
        endpointServerBuilder.setSystemManager(this.getSystemManager());
        endpointServerBuilder.setHealthService(getHealthService());
        endpointServerBuilder.setStatsFlushRateSeconds(getSampleStatFlushRate());
        endpointServerBuilder.setCheckTimingEveryXCalls(getCheckTimingEveryXCalls());


        if (isEnableStats()) {

            endpointServerBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollectorWithAutoFlush());
        }

        if (isEnableLocalStats()) {
                endpointServerBuilder.setEnableStatEndpoint(true);
                endpointServerBuilder.setStatsCollection(getLocalStatsCollectorBuilder().build());
        }

        return endpointServerBuilder;
    }


    public ServiceBundleBuilder createServiceBundleBuilder() {

        ServiceBundleBuilder serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder();
        serviceBundleBuilder.setSystemManager(this.getSystemManager());
        serviceBundleBuilder.setHealthService(getHealthService());
        serviceBundleBuilder.setCheckTimingEveryXCalls(this.getCheckTimingEveryXCalls());
        serviceBundleBuilder.setStatsFlushRateSeconds(this.getSampleStatFlushRate());


        if (isEnableStats()) {
            serviceBundleBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollector());
        }

        return serviceBundleBuilder;
    }


    public ServiceBuilder createServiceBuilderForServiceObject(Object serviceObject) {

        ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setSystemManager(this.getSystemManager());

        final String bindStatHealthName =  AnnotationUtils.readServiceName(serviceObject);

        if (isEnableLocalHealth()) {
            serviceBuilder.registerHealthChecks(getHealthService(), bindStatHealthName);
        }


        if (isEnableStats()) {


            serviceBuilder.registerStatsCollections(bindStatHealthName,
                    getStatServiceBuilder().buildStatsCollector(), getSampleStatFlushRate(), getCheckTimingEveryXCalls());
        }

        return serviceBuilder;

    }

    public ManagedServiceBuilder setEndpointServerBuilder(EndpointServerBuilder endpointServerBuilder) {
        this.endpointServerBuilder = endpointServerBuilder;
        return this;
    }



    public HealthServiceAsync getHealthService() {

        if (healthServiceBuilder == null) {
            if (healthService == null) {
                HealthServiceBuilder builder = getHealthServiceBuilder();
                healthService = builder.setAutoFlush().buildAndStart();
            }

            return healthService;
        } else {
            return healthServiceBuilder.buildHealthSystemReporter();
        }
    }

    public ManagedServiceBuilder setHealthService(HealthServiceAsync healthServiceAsync) {
        this.healthService = healthServiceAsync;
        return this;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }


    public int getSampleStatFlushRate() {
        return sampleStatFlushRate;
    }

    public void setSampleStatFlushRate(int sampleStatFlushRate) {
        this.sampleStatFlushRate = sampleStatFlushRate;
    }

    public int getCheckTimingEveryXCalls() {
        return checkTimingEveryXCalls;
    }

    public void setCheckTimingEveryXCalls(int checkTimingEveryXCalls) {
        this.checkTimingEveryXCalls = checkTimingEveryXCalls;
    }
}

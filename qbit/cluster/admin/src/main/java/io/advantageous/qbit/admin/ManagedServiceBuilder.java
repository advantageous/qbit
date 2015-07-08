package io.advantageous.qbit.admin;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.metrics.support.LocalStatsCollectorBuilder;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.system.QBitSystemManager;

/** This is a utility class for when you are running in a PaaS like Heroku or Docker. */
public class ManagedServiceBuilder {


    private QBitSystemManager systemManager;
    private StatServiceBuilder statServiceBuilder;
    private EndpointServerBuilder endpointServerBuilder;
    private LocalStatsCollectorBuilder localStatsCollectorBuilder;
    private HttpServerBuilder httpServerBuilder;
    private boolean enableLocalStats=true;
    private boolean enableStatsD=false;
    private boolean enableLocalHealth=true;

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

    public void setLocalStatsCollectorBuilder(LocalStatsCollectorBuilder localStatsCollectorBuilder) {
        this.localStatsCollectorBuilder = localStatsCollectorBuilder;
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
            endpointServerBuilder.setSystemManager(this.getSystemManager());
            endpointServerBuilder.setHttpServer(getHttpServerBuilder().build());

            if (isEnableLocalStats()) {

                endpointServerBuilder.setEnableStatEndpoint(true);

                endpointServerBuilder.setStatsCollection(getLocalStatsCollectorBuilder().build());
                endpointServerBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollectorWithAutoFlush());
            }
        }

        return endpointServerBuilder;
    }

    public ManagedServiceBuilder setEndpointServerBuilder(EndpointServerBuilder endpointServerBuilder) {
        this.endpointServerBuilder = endpointServerBuilder;
        return this;
    }
}

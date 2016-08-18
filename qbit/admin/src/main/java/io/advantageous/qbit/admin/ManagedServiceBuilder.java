package io.advantageous.qbit.admin;

import io.advantageous.boon.core.Sets;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.BeforeMethodSentChain;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.http.interceptor.ForwardCallMethodInterceptor;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.logging.SetupMdcForHttpRequestInterceptor;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.metrics.support.LocalStatsCollectorBuilder;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.metrics.support.StatsDReplicatorBuilder;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.dns.DnsUtil;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.service.health.ServiceHealthManager;
import io.advantageous.qbit.service.impl.ServiceHealthManagerDefault;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.system.QBitSystemManager;

import java.net.URI;
import java.util.*;
import java.util.function.Supplier;

import static io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder;

/**
 * This is a utility class for when you are running in a PaaS like Heroku or Docker.
 * It also allows you to share stat, health and system manager setup.
 * <p>
 * The main end point port will be read from the environment variable {@code WEB_PORT}
 * and if not found then read from {@code PORT0}, and then lastly from {@code PORT}.
 * <p>
 * If statsD is enabled then this will look for the statsd port and host from {@code STATSD_PORT} and
 * {@code STATSD_HOST} environment
 * variables.
 * <p>
 * If you use the admin builder, the port for the admin will be from the environment variable
 * {@code QBIT_ADMIN_PORT}, then {@code ADMIN_PORT}, and then {@code PORT1}.
 * It can be overridden from system properties as well see AdminBuilder for more details.
 * <p>
 * Defaults for ports and hosts can be overridden by their respective builders.
 **/
@SuppressWarnings("unused")
public class ManagedServiceBuilder {


    /**
     * Holds the system manager used to register proper shutdown with JVM.
     */
    private QBitSystemManager systemManager;

    /**
     * Holds the stats service builder used to collect system stats.
     */
    private StatServiceBuilder statServiceBuilder;

    /**
     * EndpointServerBuilder used to hold the main EndpointServerBuilder.
     */
    private EndpointServerBuilder endpointServerBuilder;

    /**
     * Used to create LocalStatsCollection.
     */
    private LocalStatsCollectorBuilder localStatsCollectorBuilder;

    /**
     * Used to create the main http server.
     */
    private HttpServerBuilder httpServerBuilder;

    /**
     * Enables local stats collection.
     */
    private boolean enableLocalStats = true;

    /**
     * Enables sending stats to stats D.
     */
    private boolean enableStatsD = false;

    /**
     * Enables local health stats collection.
     */
    private boolean enableLocalHealth = true;

    /**
     * Used to create local health collector.
     */
    private HealthServiceBuilder healthServiceBuilder;

    /**
     * Health service used to collect health info from service queues.
     */
    private HealthServiceAsync healthService;

    /**
     * Enables the collection of stats.
     */
    private boolean enableStats = true;

    /**
     * Event manager for services, service queues, and end point servers.
     */
    private EventManager eventManager;

    /**
     * Factory to create QBit parts.
     */
    private Factory factory;

    /**
     * Builder to hold context information about endpoints.
     */
    private ContextMetaBuilder contextMetaBuilder;

    /**
     * Endpoint services that will be exposed through contextMetaBuilder.
     */
    private List<Object> endpointServices;

    /**
     * Endpoint services that will be exposed through contextMetaBuilder.
     */
    private Map<String, Object> endpointServiceMapWithAlias;


    /**
     * Endpoint services that will be exposed through contextMetaBuilder.
     */
    private List<ManagedServiceDefinition> managedServiceDefinitionList;

    /**
     * The builder for the admin.
     */
    private AdminBuilder adminBuilder;

    /**
     * StatsD replicator builder.
     */
    private StatsDReplicatorBuilder statsDReplicatorBuilder;


    /**
     * Service Discovery.
     */
    private ServiceDiscovery serviceDiscovery;

    /**
     * Service Discovery supplier.
     */
    private Supplier<ServiceDiscovery> serviceDiscoverySupplier;


    /**
     * Default Root URI.
     */
    private String rootURI = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".rootURI", "/services");

    /**
     * Default public host used for swagger.
     */
    private String publicHost = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".publicHost", "localhost");

    /**
     * Actual port.
     */
    private int port = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".port", EndpointServerBuilder.DEFAULT_PORT);

    /**
     * Public port used for swagger.
     */
    private int publicPort = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".publicPort", -1);


    /**
     * How often stats should be flushed.
     */
    private int sampleStatFlushRate = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".sampleStatFlushRate", 5);

    /**
     * How often timings should be collected defaults to every 100 calls.
     */
    private int checkTimingEveryXCalls = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".checkTimingEveryXCalls", 100);


    /**
     * Turn on Logging Mapped Diagnostic Context.
     */
    private boolean enableLoggingMappedDiagnosticContext = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".enableLoggingMappedDiagnosticContext", false);


    /**
     * Turn on Request chain construction so original request is sent to other downstream internal services.
     */
    private boolean enableRequestChain = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".enableRequestChain", false);

    /**
     * Used to track headers for MDC.
     */
    private Set<String> requestHeadersToTrackForMappedDiagnosticContext;

    /**
     * Create ManagedServiceBuilder which looks for MicroserviceConfig file.
     *
     * @param serviceName service name
     */
    public ManagedServiceBuilder(final String serviceName) {
        if (serviceName != null) {


            final MicroserviceConfig config = MicroserviceConfig.readConfig(serviceName);

            /** Configure ManagedServiceBuilder. */
            this.setRootURI(config.getRootURI());
            this.setPublicHost(config.getPublicHost());
            this.setPublicPort(config.getPublicPort());
            this.setPort(config.getPort());


            /** Configure ManagedServiceBuilder. */
            final ContextMetaBuilder contextMetaBuilder = this.getContextMetaBuilder();
            contextMetaBuilder.setLicenseName(config.getLicenseName());
            contextMetaBuilder.setLicenseURL(config.getLicenseURL());
            contextMetaBuilder.setContactURL(config.getContactURL());
            contextMetaBuilder.setTitle(config.getTitle());
            contextMetaBuilder.setVersion(config.getVersion());
            contextMetaBuilder.setContactName(config.getContactName());
            contextMetaBuilder.setContactEmail(config.getContactEmail());
            contextMetaBuilder.setDescription(config.getDescription());

            /** Configure statsD. */
            if (config.isStatsD()) {
                this.setEnableStatsD(true);
                this.getStatsDReplicatorBuilder().setHost(config.getStatsDHost());

                if (config.getStatsDPort() != -1) {
                    this.getStatsDReplicatorBuilder().setPort(config.getStatsDPort());
                }
            }

            this.setCheckTimingEveryXCalls(config.getCheckTimingEveryXCalls());
            this.setSampleStatFlushRate(config.getSampleStatFlushRate());

            this.setEnableLocalStats(config.isEnableLocalStats());
            this.setEnableStats(config.isEnableStats());
            this.setEnableLocalHealth(config.isEnableLocalHealth());
        }
    }

    public static ManagedServiceBuilder managedServiceBuilder() {
        return new ManagedServiceBuilder(null);
    }

    public static ManagedServiceBuilder managedServiceBuilder(final String serviceName) {
        return new ManagedServiceBuilder(serviceName);
    }


    private List<ManagedServiceDefinition> getManagedServiceDefinitionList() {
        if (managedServiceDefinitionList == null) {
            managedServiceDefinitionList = new ArrayList<>();
        }
        return managedServiceDefinitionList;
    }

    /**
     * Enable the logging diagnostic context
     *
     * @return this
     */
    public ManagedServiceBuilder enableLoggingMappedDiagnosticContext() {
        this.enableRequestChain = true;
        this.enableLoggingMappedDiagnosticContext = true;
        return this;
    }

    /**
     * Enable the logging diagnostic context
     *
     * @param requestHeaders request headers
     * @return this, fluent
     */
    public ManagedServiceBuilder enableLoggingMappedDiagnosticContext(final String... requestHeaders) {
        return enableLoggingMappedDiagnosticContext(Sets.set(requestHeaders));
    }

    /**
     * Enable the logging diagnostic context
     *
     * @param requestHeaders request headers to track in the MDC.
     * @return this
     */
    public ManagedServiceBuilder enableLoggingMappedDiagnosticContext(final Set<String> requestHeaders) {
        this.enableRequestChain = true;
        this.enableLoggingMappedDiagnosticContext = true;
        this.requestHeadersToTrackForMappedDiagnosticContext = Collections.unmodifiableSet(requestHeaders);
        return this;
    }

    /**
     * Enable the request chain.  There is overhead for this, but this allows REST and WebSocket
     * services to pass the originating request, methodCall, etc. to downstream services.
     * Where it will be available via the RequestContext.
     * A MethodCall, HttpRequest, WebSocketMessage are all Requests in QBit.
     *
     * @return this
     */
    public ManagedServiceBuilder enableRequestChain() {
        this.enableRequestChain = true;
        return this;
    }

    /**
     * @return logging MDC enabled or not
     */
    public boolean isEnableLoggingMappedDiagnosticContext() {
        return enableLoggingMappedDiagnosticContext;
    }

    /**
     * Enable or disable logging MDC.
     *
     * @param enableLoggingMappedDiagnosticContext enableLoggingMappedDiagnosticContext
     * @return this
     */
    public ManagedServiceBuilder setEnableLoggingMappedDiagnosticContext(
            final boolean enableLoggingMappedDiagnosticContext) {
        this.enableLoggingMappedDiagnosticContext = enableLoggingMappedDiagnosticContext;
        return this;
    }

    /**
     * Request call chain building enabled. This allows REST and WebSocket
     * services to pass the originating request, methodCall, etc. to downstream services.
     * Where it will be available via the RequestContext.
     * A MethodCall, HttpRequest, WebSocketMessage are all Requests in QBit.
     *
     * @return is call chain tracking enabled.
     */
    public boolean isEnableRequestChain() {
        return enableRequestChain;
    }

    /**
     * Enable or disable request chain.  There is overhead for this, but this allows REST and WebSocket
     * services to pass the originating request, methodCall, etc. to downstream services.
     * Where it will be available via the RequestContext.
     * A MethodCall, HttpRequest, WebSocketMessage are all Requests in QBit.
     *
     * @param enableRequestChain enableRequestChain
     * @return this
     */
    public ManagedServiceBuilder setEnableRequestChain(final boolean enableRequestChain) {
        this.enableRequestChain = enableRequestChain;
        return this;
    }

    /**
     * Get the public host for service meta generation (Swagger)
     *
     * @return public host
     */
    public String getPublicHost() {

        /** This works in a Heroku like environment. */
        if (System.getenv("PUBLIC_HOST") != null) {
            publicHost = System.getenv("PUBLIC_HOST");
        }

        /** This works in a mesosphere like environment. */
        if (System.getenv("HOST") != null) {
            publicHost = System.getenv("HOST");
        }

        return publicHost;
    }

    /**
     * Set the public host for service meta generation (Swagger)
     *
     * @param publicHost publicHost
     * @return this
     */
    public ManagedServiceBuilder setPublicHost(String publicHost) {
        this.publicHost = publicHost;
        return this;
    }


    /**
     * Creates a ServiceHealthManager.
     *
     * @return ServiceHealthManager
     */
    public ServiceHealthManager createServiceHealthManager() {
        return new ServiceHealthManagerDefault(null, null);
    }


    /**
     * Creates a ServiceHealthManager.
     *
     * @param failCallback called if the service reports a failure.
     * @return ServiceHealthManager
     */
    public ServiceHealthManager createServiceHealthManager(final Runnable failCallback) {
        return new ServiceHealthManagerDefault(failCallback, null);
    }


    /**
     * Creates a ServiceHealthManager.
     *
     * @param failCallback    called if the service reports a failure.
     * @param recoverCallback recoverCallback called if the service recovers from a failure.
     * @return ServiceHealthManager
     */
    public ServiceHealthManager createServiceHealthManager(final Runnable failCallback,
                                                           final Runnable recoverCallback) {
        return new ServiceHealthManagerDefault(failCallback, recoverCallback);
    }


    /**
     * Get the public port for service meta generation (Swagger)
     *
     * @return public port
     */
    public int getPublicPort() {
        if (publicPort == -1) {

            String sport = System.getenv("PUBLIC_WEB_PORT");

            if (!Str.isEmpty(sport)) {
                publicPort = Integer.parseInt(sport);
            }
        }

        if (publicPort == -1) {
            publicPort = getPort();
        }

        return publicPort;
    }

    /**
     * Set the public port for service meta generation (Swagger)
     *
     * @param publicPort publicPort
     * @return this
     */
    public ManagedServiceBuilder setPublicPort(int publicPort) {
        this.publicPort = publicPort;
        return this;
    }

    /**
     * Get the actual port to bind to.
     * <p>
     * Defaults to EndpointServerBuilder.DEFAULT_PORT.
     * <p>
     * Looks for PORT under PORT_WEB, PORT0, PORT.
     *
     * @return actual http port to bind to.
     */
    public int getPort() {

        if (port == EndpointServerBuilder.DEFAULT_PORT) {
            String sport = System.getenv("PORT_WEB");
            /** Looks up port for Mesoshpere and the like. */
            if (Str.isEmpty(sport)) {
                sport = System.getenv("PORT0");
            }
            if (Str.isEmpty(sport)) {
                sport = System.getenv("PORT");
            }
            if (!Str.isEmpty(sport)) {
                port = Integer.parseInt(sport);
            }
        }
        return port;
    }

    /**
     * Set the actual port to bind to.
     *
     * @param port port
     * @return this
     */
    public ManagedServiceBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Get the root URI
     *
     * @return root URI.
     */
    public String getRootURI() {
        return rootURI;
    }

    /**
     * Set rootURI root uri
     *
     * @param rootURI rootURI
     * @return this, fluent
     */
    public ManagedServiceBuilder setRootURI(String rootURI) {
        this.rootURI = rootURI;
        return this;
    }

    /**
     * Enable consul discovery
     *
     * @param dataCenter name of datacenter we are connecting to.
     * @return fluent, this
     */
    public ManagedServiceBuilder enableConsulServiceDiscovery(final String dataCenter) {
        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder();
        consulServiceDiscoveryBuilder.setDatacenter(dataCenter);
        serviceDiscoverySupplier = consulServiceDiscoveryBuilder::build;
        return this;
    }


    /**
     * Enable DNS discovery
     *
     * @return fluent, this
     */
    public ManagedServiceBuilder enableDNSDiscovery() {
        serviceDiscoverySupplier = DnsUtil::createDnsServiceDiscovery;
        return this;
    }

    /**
     * Enable consul discovery.
     *
     * @param dataCenter name of datacenter we are connecting to.
     * @param host       name of host we are connecting to
     * @return fluent, this
     */
    public ManagedServiceBuilder enableConsulServiceDiscovery(final String dataCenter, final String host) {
        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder();
        consulServiceDiscoveryBuilder.setDatacenter(dataCenter);
        consulServiceDiscoveryBuilder.setConsulHost(host);
        serviceDiscoverySupplier = consulServiceDiscoveryBuilder::build;
        return this;
    }

    /**
     * Enable consul discovery.
     *
     * @param dataCenter name of datacenter we are connecting to.
     * @param host       name of host we are connecting to
     * @param port       consul port
     * @return fluent, this
     **/
    public ManagedServiceBuilder enableConsulServiceDiscovery(final String dataCenter,
                                                              final String host,
                                                              final int port) {
        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder();
        consulServiceDiscoveryBuilder.setDatacenter(dataCenter);
        consulServiceDiscoveryBuilder.setConsulHost(host);
        consulServiceDiscoveryBuilder.setConsulPort(port);
        serviceDiscoverySupplier = consulServiceDiscoveryBuilder::build;
        return this;
    }

    public ManagedServiceBuilder enableConsulServiceDiscovery(final String dataCenter,
                                                              final URI uri) {
        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = consulServiceDiscoveryBuilder();
        consulServiceDiscoveryBuilder.setDatacenter(dataCenter);
        consulServiceDiscoveryBuilder.setConnectionUri(uri);
        serviceDiscoverySupplier = consulServiceDiscoveryBuilder::build;
        return this;
    }

    /**
     * Used to get access to the serviceDiscoverySupplier which could be null.
     *
     * @return Supplier
     */
    public Supplier<ServiceDiscovery> getServiceDiscoverySupplier() {
        return serviceDiscoverySupplier;
    }

    /**
     * Used to inject a service discovery supplier.
     *
     * @param serviceDiscoverySupplier serviceDiscoverySupplier
     * @return fluent, this
     */
    public ManagedServiceBuilder setServiceDiscoverySupplier(Supplier<ServiceDiscovery> serviceDiscoverySupplier) {
        this.serviceDiscoverySupplier = serviceDiscoverySupplier;
        return this;
    }

    /**
     * Used to get the service discovery.
     *
     * @return ServiceDiscovery
     */
    public ServiceDiscovery getServiceDiscovery() {
        if (serviceDiscovery == null) {
            if (serviceDiscoverySupplier != null) {
                return serviceDiscoverySupplier.get();
            }
        }
        return serviceDiscovery;
    }


    /**
     * Used to inject service discovery.
     *
     * @param serviceDiscovery service discovery
     * @return fluent, this
     */
    public ManagedServiceBuilder setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        return this;
    }

    /**
     * Used to get the StatsD replicator builder.
     * You can use this to configure StatsD.
     * You can also use environment variables {@code STATSD_PORT} and {@code STATSD_HOST}.
     *
     * @return stats replicator builder.
     */
    public StatsDReplicatorBuilder getStatsDReplicatorBuilder() {
        if (statsDReplicatorBuilder == null) {
            statsDReplicatorBuilder = StatsDReplicatorBuilder.statsDReplicatorBuilder();

            final String statsDPort = System.getenv("STATSD_PORT");
            if (statsDPort != null && !statsDPort.isEmpty()) {
                statsDReplicatorBuilder.setPort(Integer.parseInt(statsDPort));
            }

            final String statsDHost = System.getenv("STATSD_HOST");
            if (statsDHost != null && !statsDHost.isEmpty()) {
                statsDReplicatorBuilder.setHost(statsDHost);
            }

        }
        return statsDReplicatorBuilder;
    }

    /**
     * Sets the statsD replicator.
     *
     * @param statsDReplicatorBuilder statsDReplicatorBuilder
     * @return fluent, this
     */
    public ManagedServiceBuilder setStatsDReplicatorBuilder(
            final StatsDReplicatorBuilder statsDReplicatorBuilder) {
        this.statsDReplicatorBuilder = statsDReplicatorBuilder;
        return this;
    }

    /**
     * Finds the admin port.
     * Searches  under environment variables,
     * {@code QBIT_ADMIN_PORT}, {@code ADMIN_PORT}, {@code PORT1}.
     *
     * @return admin port
     */
    public String findAdminPort() {

        String qbitAdminPort = System.getenv("QBIT_ADMIN_PORT");

        if (Str.isEmpty(qbitAdminPort)) {
            qbitAdminPort = System.getenv("ADMIN_PORT");
        }

        /* Uses PORT1 for admin port for Mesosphere / Heroku like environments. */
        if (Str.isEmpty(qbitAdminPort)) {
            qbitAdminPort = System.getenv("PORT1");
        }
        return qbitAdminPort;
    }

    /**
     * Get the Admin builder.
     *
     * @return admin builder.
     */
    public AdminBuilder getAdminBuilder() {
        if (adminBuilder == null) {
            adminBuilder = AdminBuilder.adminBuilder();

            final String qbitAdminPort = findAdminPort();
            if (qbitAdminPort != null && !qbitAdminPort.isEmpty()) {
                adminBuilder.setPort(Integer.parseInt(qbitAdminPort));
            }
            adminBuilder.setContextBuilder(this.getContextMetaBuilder());
            adminBuilder.setHealthService(getHealthService());
            adminBuilder.registerJavaVMStatsJob(getStatServiceBuilder().buildStatsCollector());

        }
        return adminBuilder;
    }

    /**
     * @param adminBuilder adminBuilder
     * @return this, fluent
     */
    public ManagedServiceBuilder setAdminBuilder(AdminBuilder adminBuilder) {
        this.adminBuilder = adminBuilder;
        return this;
    }


    /**
     * Get context meta builder.
     *
     * @return context meta builder
     */
    public ContextMetaBuilder getContextMetaBuilder() {
        if (contextMetaBuilder == null) {
            contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
            contextMetaBuilder
                    .setHostAddress(this.getPublicHost() + ":" + this.getPublicPort())
                    .setRootURI(this.getRootURI());
        }
        return contextMetaBuilder;
    }

    /**
     * Get the context meta builder. Used for things like swagger generation.
     *
     * @param contextMetaBuilder contextMetaBuilder
     * @return this, fluent
     */
    public ManagedServiceBuilder setContextMetaBuilder(final ContextMetaBuilder contextMetaBuilder) {
        this.contextMetaBuilder = contextMetaBuilder;
        return this;
    }

    /**
     * Services with alias.
     *
     * @return map of services and their alias.
     */
    public Map<String, Object> getEndpointServiceMapWithAlias() {
        if (endpointServiceMapWithAlias == null) {
            endpointServiceMapWithAlias = new HashMap<>();
        }
        return endpointServiceMapWithAlias;
    }

    /**
     * Set in a map of alias
     *
     * @param endpointServiceMapWithAlias endpointServiceMapWithAlias
     * @return this, fluent
     */
    public ManagedServiceBuilder setEndpointServiceMapWithAlias(Map<String, Object> endpointServiceMapWithAlias) {
        this.endpointServiceMapWithAlias = endpointServiceMapWithAlias;
        return this;
    }

    /**
     * get the list of endpoint services
     *
     * @return services without alias
     */
    public List<Object> getEndpointServices() {
        if (endpointServices == null) {
            endpointServices = new ArrayList<>();
        }

        return endpointServices;
    }

    /**
     * Set in the list of services
     *
     * @param endpointServices endpoint services w/o alias
     * @return this, fluent
     */
    public ManagedServiceBuilder setEndpointServices(final List<Object> endpointServices) {
        this.endpointServices = endpointServices;
        return this;
    }


    /**
     * Add an endpoint with a managment bundle
     *
     * @param endpointService         endpoint service
     * @param serviceManagementBundle management bundle
     * @return this, fluent
     */
    public ManagedServiceBuilder addEndpointServiceWithServiceManagmentBundle(
            final Object endpointService, final ServiceManagementBundle serviceManagementBundle) {

        addEndpointServiceWithAliasAndQueueHandlerCallbacks(null, endpointService, new QueueCallBackHandler() {
            @Override
            public void queueProcess() {
                serviceManagementBundle.process();
            }
        });
        return this;
    }


    /**
     * Add an endpoint with a managment bundle and an alias
     *
     * @param alias                   alias
     * @param endpointService         endpoint service
     * @param serviceManagementBundle management bundle
     * @return this, fluent
     */
    public ManagedServiceBuilder addEndpointServiceWithAliasAndServiceManagmentBundle(
            final String alias,
            final Object endpointService, final ServiceManagementBundle serviceManagementBundle) {

        addEndpointServiceWithAliasAndQueueHandlerCallbacks(alias, endpointService, new QueueCallBackHandler() {
            @Override
            public void queueProcess() {
                serviceManagementBundle.process();
            }
        });
        return this;
    }

    /**
     * Add an endpoint service
     *
     * @param endpointService endpoint service
     * @return this, fluent
     */
    public ManagedServiceBuilder addEndpointService(final Object endpointService) {
        getContextMetaBuilder().addService(endpointService.getClass());
        getEndpointServices().add(endpointService);
        return this;
    }

    /**
     * Add an end point service with queue callback handlers
     *
     * @param endpointService       endpoint Service
     * @param queueCallBackHandlers queue callback handlers
     * @return this, fluent
     */
    public ManagedServiceBuilder addEndpointServiceWithQueueHandlerCallbacks(final Object endpointService,
                                                                             final QueueCallBackHandler...
                                                                                     queueCallBackHandlers) {
        getContextMetaBuilder().addService(endpointService.getClass());
        getManagedServiceDefinitionList().add(new ManagedServiceDefinition(null, endpointService,
                queueCallBackHandlers));
        return this;
    }

    /**
     * Add an end point service with queue callback handlers
     *
     * @param alias                 alias
     * @param endpointService       endpoint Service
     * @param queueCallBackHandlers queue callback handlers
     * @return this, fluent
     */
    public ManagedServiceBuilder addEndpointServiceWithAliasAndQueueHandlerCallbacks(final String alias,
                                                                                     final Object endpointService,
                                                                                     final QueueCallBackHandler...
                                                                                             queueCallBackHandlers) {
        getContextMetaBuilder().addService(endpointService.getClass());
        getManagedServiceDefinitionList().add(new ManagedServiceDefinition(alias, endpointService,
                queueCallBackHandlers));
        return this;
    }


    /**
     * Add endpoint service with alias
     *
     * @param alias           alias
     * @param endpointService endpoint service
     * @return this, fluent
     */
    public ManagedServiceBuilder addEndpointService(final String alias, final Object endpointService) {
        getContextMetaBuilder().addService(alias, endpointService.getClass());
        getEndpointServiceMapWithAlias().put(alias, endpointService);
        return this;
    }


    /**
     * Get the base QBit Factory
     *
     * @return qbit factory
     */
    public Factory getFactory() {
        if (factory == null) {
            factory = QBit.factory();
        }
        return factory;
    }

    /**
     * Set the QBit Factory
     *
     * @param factory factory
     * @return this, fluent
     */
    public ManagedServiceBuilder setFactory(final Factory factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Get event manager
     *
     * @return event manager
     */
    public EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = getFactory().systemEventManager();
        }
        return eventManager;
    }

    /**
     * Set event manager
     *
     * @param eventManager eventManager
     * @return this, fluent
     */
    public ManagedServiceBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    /**
     * @return HealthServiceBuilder
     */
    public HealthServiceBuilder getHealthServiceBuilder() {

        if (healthServiceBuilder == null) {
            healthServiceBuilder = HealthServiceBuilder.healthServiceBuilder();
        }
        return healthServiceBuilder;
    }

    /**
     * @param healthServiceBuilder healthServiceBuilder
     * @return this, fluent
     */
    public ManagedServiceBuilder setHealthServiceBuilder(final HealthServiceBuilder healthServiceBuilder) {
        this.healthServiceBuilder = healthServiceBuilder;
        return this;
    }

    /**
     * @return http server builder
     */
    public HttpServerBuilder getHttpServerBuilder() {
        if (httpServerBuilder == null) {
            httpServerBuilder = HttpServerBuilder.httpServerBuilder();

            int port = getPort();


            if (!Str.isEmpty(port)) {
                httpServerBuilder.setPort(port);
            }
        }
        return httpServerBuilder;
    }

    /**
     * @param httpServerBuilder httpServerBuilder
     * @return this, fluent
     */
    public ManagedServiceBuilder setHttpServerBuilder(HttpServerBuilder httpServerBuilder) {
        this.httpServerBuilder = httpServerBuilder;
        return this;
    }

    /**
     * @return is statsD enabled.
     */
    public boolean isEnableStatsD() {
        return enableStatsD;
    }

    /**
     * @param enableStatsD enable statsD
     * @return this, fluent
     */
    public ManagedServiceBuilder setEnableStatsD(boolean enableStatsD) {
        this.enableStatsD = enableStatsD;
        return this;
    }


    /**
     * Enable statsD using the URI specified.
     *
     * @param uri uri for stats D
     * @return this, fluent
     */
    public ManagedServiceBuilder enableStatsD(final URI uri) {
        this.enableStatsD = true;
        this.getStatsDReplicatorBuilder().setHost(uri.getHost()).setPort(uri.getPort());
        return this;
    }

    /**
     * Enable statsD using the URI specified.
     *
     * @param host host for statsD
     * @param port port for statsD
     * @return this, fluent
     */
    public ManagedServiceBuilder enableStatsD(final String host, final int port) {
        this.enableStatsD = true;
        this.getStatsDReplicatorBuilder().setHost(host).setPort(port);
        return this;
    }

    public LocalStatsCollectorBuilder getLocalStatsCollectorBuilder() {
        if (localStatsCollectorBuilder == null) {
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
            statServiceBuilder = StatServiceBuilder.statServiceBuilder();

            if (enableLocalStats) {
                statServiceBuilder.addReplicator(getLocalStatsCollectorBuilder().buildAndStart());
            }

            if (enableStatsD) {
                statServiceBuilder.addReplicator(getStatsDReplicatorBuilder().buildAndStart());
            }

            statServiceBuilder.build();
            statServiceBuilder.buildServiceQueueWithCallbackHandler()
                    .startCallBackHandler().start();

        }
        return statServiceBuilder;
    }

    public ManagedServiceBuilder setStatServiceBuilder(StatServiceBuilder statServiceBuilder) {

        this.statServiceBuilder = statServiceBuilder;
        return this;
    }


    public EndpointServerBuilder getEndpointServerBuilder() {
        if (endpointServerBuilder == null) {
            endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();
            endpointServerBuilder.setPort(this.getPort());
            endpointServerBuilder.setEnableHealthEndpoint(isEnableLocalHealth());
            endpointServerBuilder.setEnableStatEndpoint(isEnableLocalStats());

            endpointServerBuilder.setHealthService(getHealthService());
            endpointServerBuilder.setSystemManager(this.getSystemManager());
            endpointServerBuilder.setHttpServerBuilder(getHttpServerBuilder());
            endpointServerBuilder.setStatsFlushRateSeconds(getSampleStatFlushRate());
            endpointServerBuilder.setCheckTimingEveryXCalls(getCheckTimingEveryXCalls());
            endpointServerBuilder.setServiceDiscovery(getServiceDiscovery());
            endpointServerBuilder.setUri(getRootURI());
            endpointServerBuilder.setEventManager(this.getEventManager());


            configureEndpointServerBuilderForInterceptors(endpointServerBuilder);


            if (isEnableStats()) {

                endpointServerBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollectorWithAutoFlush());
            }

            if (isEnableLocalStats()) {
                endpointServerBuilder.setEnableStatEndpoint(true);
                endpointServerBuilder.setStatsCollection(getLocalStatsCollectorBuilder().build());
            }

            endpointServerBuilder.setupHealthAndStats(getHttpServerBuilder());

            if (endpointServices != null) {
                endpointServerBuilder.setServices(endpointServices);
            }

            if (endpointServiceMapWithAlias != null) {
                endpointServerBuilder.setServicesWithAlias(endpointServiceMapWithAlias);
            }

        }

        return endpointServerBuilder;
    }

    public ManagedServiceBuilder setEndpointServerBuilder(EndpointServerBuilder endpointServerBuilder) {
        this.endpointServerBuilder = endpointServerBuilder;
        return this;
    }

    private void configureEndpointServerBuilderForInterceptors(final EndpointServerBuilder endpointServerBuilder) {

        final Interceptors interceptors = configureInterceptors();
        if (interceptors.before.size() > 0) {
            endpointServerBuilder.setBeforeMethodCallOnServiceQueue(new BeforeMethodCallChain(interceptors.before));
        }
        if (interceptors.after.size() > 0) {
            endpointServerBuilder.setAfterMethodCallOnServiceQueue(new AfterMethodCallChain(interceptors.after));
        }
        if (interceptors.beforeSent.size() > 0) {
            endpointServerBuilder.setBeforeMethodSent(new BeforeMethodSentChain(interceptors.beforeSent));
        }
    }

    private void configureServiceBundleBuilderForInterceptors(final ServiceBundleBuilder serviceBundleBuilder) {

        final Interceptors interceptors = configureInterceptors();
        if (interceptors.before.size() > 0) {
            serviceBundleBuilder.setBeforeMethodCallOnServiceQueue(new BeforeMethodCallChain(interceptors.before));
        }
        if (interceptors.after.size() > 0) {
            serviceBundleBuilder.setAfterMethodCallOnServiceQueue(new AfterMethodCallChain(interceptors.after));
        }
        if (interceptors.beforeSent.size() > 0) {
            serviceBundleBuilder.setBeforeMethodSent(new BeforeMethodSentChain(interceptors.beforeSent));
        }
    }

    private void configureServiceBuilderForInterceptors(final ServiceBuilder serviceBuilder) {

        final Interceptors interceptors = configureInterceptors();
        if (interceptors.before.size() > 0) {
            serviceBuilder.setBeforeMethodCall(new BeforeMethodCallChain(interceptors.before));
        }
        if (interceptors.after.size() > 0) {
            serviceBuilder.setAfterMethodCall(new AfterMethodCallChain(interceptors.after));
        }
        if (interceptors.beforeSent.size() > 0) {
            serviceBuilder.setBeforeMethodSent(new BeforeMethodSentChain(interceptors.beforeSent));
        }
    }

    /**
     * Get the ServiceEndpointServer constructed with all of the service endpionts that
     * you registered
     *
     * @return new ServiceEndpointServer.
     */
    public ServiceEndpointServer getServiceEndpointServer() {

        final ServiceEndpointServer serviceEndpointServer = getEndpointServerBuilder().build();

        if (managedServiceDefinitionList != null) {
            managedServiceDefinitionList.forEach(serviceDef -> {
                if (serviceDef.getAlias() == null) {
                    serviceEndpointServer.addServiceWithQueueCallBackHandlers(serviceDef.getServiceObject(),
                            serviceDef.getQueueCallBackHandlers());
                } else {
                    serviceEndpointServer.addServiceObjectWithQueueCallBackHandlers(serviceDef.getAlias(),
                            serviceDef.getServiceObject(), serviceDef.getQueueCallBackHandlers());
                }
            });
        }
        return serviceEndpointServer;

    }

    /**
     * Starts up the application.
     * @return service endpoint server
     */
    public ServiceEndpointServer startApplication() {
        return getServiceEndpointServer().startServerAndWait();
    }


    /**
     * Configure a list of common interceptors.
     *
     * @return interceptors.
     */
    private Interceptors configureInterceptors() {
        Interceptors interceptors = new Interceptors();
        SetupMdcForHttpRequestInterceptor setupMdcForHttpRequestInterceptor;
        if (enableLoggingMappedDiagnosticContext) {
            enableRequestChain = true;
            if (requestHeadersToTrackForMappedDiagnosticContext != null &&
                    requestHeadersToTrackForMappedDiagnosticContext.size() > 0) {
                setupMdcForHttpRequestInterceptor =
                        new SetupMdcForHttpRequestInterceptor(requestHeadersToTrackForMappedDiagnosticContext);
            } else {
                setupMdcForHttpRequestInterceptor = new SetupMdcForHttpRequestInterceptor(Collections.emptySet());
            }
            interceptors.before.add(setupMdcForHttpRequestInterceptor);
            interceptors.after.add(setupMdcForHttpRequestInterceptor);
        }

        if (enableRequestChain) {
            final CaptureRequestInterceptor captureRequestInterceptor = new CaptureRequestInterceptor();
            interceptors.before.add(captureRequestInterceptor);
            interceptors.after.add(captureRequestInterceptor);
            interceptors.beforeSent.add(new ForwardCallMethodInterceptor(new RequestContext()));
        }
        return interceptors;
    }


    public EndpointServerBuilder createEndpointServerBuilder() {

        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();
        endpointServerBuilder.setEnableHealthEndpoint(isEnableLocalHealth());
        endpointServerBuilder.setSystemManager(this.getSystemManager());
        endpointServerBuilder.setHealthService(getHealthService());
        endpointServerBuilder.setStatsFlushRateSeconds(getSampleStatFlushRate());
        endpointServerBuilder.setCheckTimingEveryXCalls(getCheckTimingEveryXCalls());
        endpointServerBuilder.setServiceDiscovery(getServiceDiscovery());
        endpointServerBuilder.setEventManager(this.getEventManager());

        configureEndpointServerBuilderForInterceptors(endpointServerBuilder);


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
        serviceBundleBuilder.setEventManager(this.getEventManager());

        configureServiceBundleBuilderForInterceptors(serviceBundleBuilder);

        if (isEnableStats()) {
            serviceBundleBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollector());
        }

        return serviceBundleBuilder;
    }


    public ServiceBuilder createServiceBuilderForServiceObject(final Object serviceObject) {

        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setSystemManager(this.getSystemManager());
        serviceBuilder.setEventManager(this.getEventManager());

        serviceBuilder.setServiceObject(serviceObject);

        final String bindStatHealthName = AnnotationUtils.readServiceName(serviceObject);

        if (isEnableLocalHealth()) {
            serviceBuilder.registerHealthChecks(getHealthService(), bindStatHealthName);
        }

        configureServiceBuilderForInterceptors(serviceBuilder);

        if (isEnableStats()) {


            serviceBuilder.registerStatsCollections(bindStatHealthName,
                    getStatServiceBuilder().buildStatsCollector(), getSampleStatFlushRate(),
                    getCheckTimingEveryXCalls());
        }

        return serviceBuilder;

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

    public ManagedServiceBuilder setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
        return this;
    }

    public int getSampleStatFlushRate() {
        return sampleStatFlushRate;
    }

    public ManagedServiceBuilder setSampleStatFlushRate(int sampleStatFlushRate) {
        this.sampleStatFlushRate = sampleStatFlushRate;
        return this;
    }

    public int getCheckTimingEveryXCalls() {
        return checkTimingEveryXCalls;
    }

    public ManagedServiceBuilder setCheckTimingEveryXCalls(int checkTimingEveryXCalls) {
        this.checkTimingEveryXCalls = checkTimingEveryXCalls;
        return this;
    }

    /**
     * Sets up DNS based service discovery.
     *
     * @return fluent, this
     */
    public ManagedServiceBuilder useDnsServiceDiscovery() {
        this.setServiceDiscovery(DnsUtil.createDnsServiceDiscovery());
        return this;
    }

    /**
     * Create a new StatsCollector
     *
     * @return new stats collector for a single service.
     */
    public StatsCollector createStatsCollector() {
        return this.getStatServiceBuilder().buildStatsCollector();
    }

    /**
     * Hold lists of interceptors.
     */
    private static class Interceptors {
        List<BeforeMethodCall> before = new ArrayList<>();
        List<AfterMethodCall> after = new ArrayList<>();
        List<BeforeMethodSent> beforeSent = new ArrayList<>();
    }

}
